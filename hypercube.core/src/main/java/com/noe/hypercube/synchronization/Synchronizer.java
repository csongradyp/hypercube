package com.noe.hypercube.synchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.persistence.domain.MappingEntity;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.request.MappingRequest;
import com.noe.hypercube.event.domain.response.MappingResponse;
import com.noe.hypercube.observer.local.LocalFileMonitor;
import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.observer.local.LocalObserverFactory;
import com.noe.hypercube.observer.remote.CloudMonitor;
import com.noe.hypercube.observer.remote.CloudObserver;
import com.noe.hypercube.observer.remote.CloudObserverFactory;
import com.noe.hypercube.synchronization.presynchronization.IPreSynchronizer;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;
import net.engio.mbassy.listener.Handler;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.noe.hypercube.event.domain.MappingEvent.Action.ADD;
import static com.noe.hypercube.event.domain.MappingEvent.Action.REMOVE;

@Named
public class Synchronizer implements EventHandler<MappingRequest> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Synchronizer.class);

    @Inject
    private LocalFileMonitor fileMonitor;
    @Inject
    private CloudMonitor cloudMonitor;
    @Inject
    private LocalObserverFactory localObserverFactory;
    @Inject
    private CloudObserverFactory cloudObserverFactory;
    @Inject
    private PreSynchronizerFactory preSynchronizerFactory;
    @Inject
    private IAccountController accountController;
    @Inject
    private IPersistenceController persistenceController;

    private ExecutorService executorService;
    private ExecutorService presynchronizationExecutorService;

    private List<LocalFileObserver> localObservers;
    private Collection<CloudObserver> cloudObservers;
    private Collection<IPreSynchronizer> preSynchronizers;

    @PostConstruct
    public void init() {
        cloudObservers = cloudObserverFactory.create();
        localObservers = localObserverFactory.create();
        preSynchronizers = preSynchronizerFactory.create(localObservers);
        executorService = Executors.newFixedThreadPool(10);
        EventBus.subscribeToMappingRequest(this);
    }

    public void start() {
        submitDownloaders();
        submitUploaders();
        preSynchronize();
        fileMonitor.addObservers(localObservers);
        cloudMonitor.addObservers(cloudObservers);
        cloudMonitor.start();
        fileMonitor.start();
        LOG.info("Synchronization has been fully started");
    }

    private void preSynchronize() {
        LOG.info("PreSynchronization has been started");
        presynchronizationExecutorService = Executors.newFixedThreadPool(preSynchronizers.size() + 1);
        for (IPreSynchronizer preSynchronizer : preSynchronizers) {
            presynchronizationExecutorService.submit(preSynchronizer);
            LOG.info("PreSynchronizer submitted for folder {}", preSynchronizer.getTargetFolder());
        }
        try {
            final List<Future<Boolean>> futures = presynchronizationExecutorService.invokeAll(preSynchronizers);
            for (Future<Boolean> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        LOG.info("PreSynchronization has been finished");
    }

    private void submitDownloaders() {
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            executorService.submit(accountBox.getDownloader());
            LOG.info("Downloader submitted for account: {}", accountBox.getClient().getAccountName());
        }
    }

    private void submitUploaders() {
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            executorService.submit(accountBox.getUploader());
            LOG.info("Uploader submitted for account: {}", accountBox.getClient().getAccountName());
        }
    }

    public void shutdown() {
        executorService.shutdown();
        LOG.info("Synchronization has been shutted down");
    }

    public LocalFileMonitor getFileMonitor() {
        return fileMonitor;
    }

    public CloudMonitor getCloudMonitor() {
        return cloudMonitor;
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final MappingRequest event) {
        if (ADD == event.getAction()) {
            final Path localFolder = event.getLocalFolder();
            final MappingResponse mappingResponse = new MappingResponse(localFolder);
            final Map<String, Path> remoteFolders = event.getRemoteFolders();
            for (Map.Entry<String, Path> remoteMapping : remoteFolders.entrySet()) {
                final AccountBox accountBox = accountController.getAccountBox(remoteMapping.getKey());
                final MappingEntity mapping = accountBox.getMapper().createMapping();
                final String account = remoteMapping.getKey();
                final Path remoteFolder = remoteMapping.getValue();
                mapping.setLocalDir(localFolder.toString());
                mapping.setRemoteDir(remoteFolder.toString());
                persistenceController.addMapping(mapping);
                mappingResponse.addRemoteFolder(account, remoteFolder);
            }
            final IPreSynchronizer preSynchronizer = preSynchronizerFactory.create(localFolder);
            final Future<Boolean> submit = presynchronizationExecutorService.submit(preSynchronizer);
            try {
                submit.get();
                submitMapping(localFolder, remoteFolders);
                EventBus.publish(mappingResponse);
            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            if (REMOVE == event.getAction()) {
                removeMapping(event);
            }
        }
    }

    private void removeMapping(final MappingRequest event) {
        fileMonitor.removeObserver(event.getLocalFolder());
        final Map<String, Path> remoteFolders = event.getRemoteFolders();
        for (String account : remoteFolders.keySet()) {
            final AccountBox accountBox = accountController.getAccountBox(account);
            final Collection mappings = persistenceController.getMappings(accountBox.getMapper().getMappingClass());
            final Optional<MappingEntity> removableMapping = mappings.stream()
                    .filter((Predicate<MappingEntity>) (MappingEntity mappingEntity) -> Paths.get(mappingEntity.getRemoteDir()).equals(event.getRemoteFolders().get(account))
                            && Paths.get(mappingEntity.getLocalDir()).equals(event.getLocalFolder()))
                    .findAny();
            cloudMonitor.removeObserver(accountBox.getAccountType(), remoteFolders.get(account));
            if (removableMapping.isPresent()) {
                final MappingEntity mappingToRemove = removableMapping.get();
                final String id = mappingToRemove.getId();
                persistenceController.removeMapping(id, mappingToRemove.getClass());
            }
        }
        final Collection<MappingEntity> allMappings = persistenceController.getAllMappings();
        System.out.println(allMappings);
    }

    private void submitMapping(Path localFolder, Map<String, Path> remoteFolders) {
        final LocalFileObserver fileObserver = localObserverFactory.createFileObserver(localFolder);
        fileMonitor.addObserver(fileObserver);
        addToCloudObserver(remoteFolders);
    }

    private void addToCloudObserver(final Map<String, Path> remoteFolders) {
        for (Map.Entry<String, Path> remoteMapping : remoteFolders.entrySet()) {
            final AccountBox accountBox = accountController.getAccountBox(remoteMapping.getKey());
            final Path remoteFolder = remoteMapping.getValue();
            cloudMonitor.addTargetFolder(accountBox.getAccountType(), remoteFolder);
        }
    }
}
