package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.FileEventType;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.noe.hypercube.synchronization.Action.ADDED;
import static com.noe.hypercube.synchronization.Action.CHANGED;
import static com.noe.hypercube.synchronization.Action.IDENTICAL;

public class Downloader implements IDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    private final IPersistenceController persistenceController;
    private final IClient client;
    private final IMapper directoryMapper;
    private final FileEntityFactory entityFactory;
    private final BlockingQueue<ServerEntry> downloadQ;

    private AtomicBoolean stop = new AtomicBoolean(false);

    public Downloader(final IClient client, final IMapper directoryMapper, final FileEntityFactory entityFactory, final IPersistenceController persistenceController) {
        this.client = client;
        this.persistenceController = persistenceController;
        this.directoryMapper = directoryMapper;
        this.entityFactory = entityFactory;
        downloadQ = new LinkedBlockingDeque<>(50);
    }

    @Override
    public void download(ServerEntry entry) {
        downloadQ.add(entry);
    }

    @Override
    public void run() {
        while (!stop.get()) {
            ServerEntry entry;
            try {
                entry = downloadQ.take();
                process(entry);
            } catch (InterruptedException e) {
                LOG.error("{} download queue operation has been interrupted", client.getAccountName());
            }
        }
    }

    private void process(ServerEntry entry) {
        if (exist(entry)) {
            downloadFromServer(entry);
        } else {
            deleteLocalFile(entry);
        }
        logQueueEmpty();
    }

    private boolean exist(ServerEntry entry) {
        try {
            return client.exist(entry);
        } catch (SynchronizationException e) {
            LOG.error("{} - Error Occurred while getting information of file: {}", client.getAccountName(), entry.getPath());
        }
        return false;
    }

    private void logQueueEmpty() {
        if (downloadQ.isEmpty()) {
            LOG.info("{} download queue is empty. Waiting for changes from server", client.getAccountName());
        }
    }

    public void stop() {
        stop.set(true);
    }

    public void restart() {
        stop.set(false);
        run();
    }

    private Action getDeltaAction(ServerEntry entry, File localPath) {
        FileEntity fileEntity = persistenceController.get(localPath.toString(), client.getEntityType());
        if (fileEntity != null) {
            if (isDifferentRevision(entry, fileEntity)) {
                return CHANGED;
            }
            return IDENTICAL;
        }
        return ADDED;
    }

    private boolean isDifferentRevision(ServerEntry entry, FileEntity dbEntry) {
        return !dbEntry.getRevision().equals(entry.getRevision());
    }

    private void downloadFromServer(final ServerEntry entry) {
        if (entry.isFile()) {
            final List<Path> localPaths = directoryMapper.getLocals(entry.getPath());
            for (Path localPath : localPaths) {
                final File newLocalFile = createNewLocalFileReference(entry, localPath);
                final Action action = getDeltaAction(entry, newLocalFile);
                if (ADDED == action) {
                    publishEvent(entry, newLocalFile, FileEventType.NEW);
                    createDirsFor(newLocalFile);
                    download(entry, newLocalFile);
                } else if (CHANGED == action) {
                    publishEvent(entry, newLocalFile, FileEventType.UPDATED);
                    download(entry, newLocalFile);
                } else {
                    LOG.debug("{} is up to date", localPath);
                }
            }
        }
    }

    private File createNewLocalFileReference(final ServerEntry entry, final Path localPath) {
        File newLocalFile = new File(localPath.toString(), entry.getPath().getFileName().toString());
        if (isConflicted(newLocalFile)) {
            LOG.warn("{} Conflict {}", client.getAccountName(), newLocalFile);
            final String conflictedFileName = String.format("%s (%s)", entry.getPath().getFileName().toString(), client.getAccountName());
            LOG.info("{} already exists! File name updated to: {}", newLocalFile.toPath(), conflictedFileName);
            newLocalFile = new File(localPath.toString(), conflictedFileName);
        }
        return newLocalFile;
    }

    private boolean isConflicted(File newLocalFile) {
        return newLocalFile.exists() && isNotMapped(newLocalFile);
    }

    private boolean isNotMapped(File newLocalFile) {
        return persistenceController.get(newLocalFile.toPath().toString(), client.getEntityType()) == null;
    }

    private void publishEvent(ServerEntry entry, File newLocalFile, FileEventType eventType) {
        EventBus.publish(new FileEvent(entry.getPath(), newLocalFile.toPath(), eventType));
    }


    private void download(ServerEntry entry, File newLocalFile) {
        try (FileOutputStream outputStream = FileUtils.openOutputStream(newLocalFile)) {
            client.download(entry, outputStream);
            persist(entry, newLocalFile.toPath());
            LOG.info("{} Successfully downloaded {}", client.getAccountName(), newLocalFile.toPath());
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't write file {}", newLocalFile.toPath(), e);
        } catch (IOException e) {
            LOG.error("Error occurred while downloading file from {}", client.getAccountName(), e);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
        setLocalFileLastModifiedDate(entry, newLocalFile);
    }

    private void createDirsFor(File newLocalFile) {
        if (!newLocalFile.getParentFile().exists()) {
            boolean success = newLocalFile.getParentFile().mkdirs();
            if (!success) {
                LOG.error("Directory creation failed for {}", newLocalFile.getPath());
            }
        }
    }

    private void setLocalFileLastModifiedDate(ServerEntry entry, File newLocalFile) {
        long lastModified = entry.lastModified().getTime();
        boolean success = newLocalFile.setLastModified(lastModified);
        if (!success) {
            LOG.debug("Couldn't change attribute 'lastModified' of file {}", newLocalFile.getPath());
        }
    }

    private void persist(ServerEntry entry, Path localPath) {
        FileEntity fileEntity = entityFactory.createFileEntity(localPath.toString(), entry.getRevision(), entry.lastModified());
        persistenceController.save(fileEntity);
    }

    private void deleteLocalFile(ServerEntry entry) {
        List<Path> localPaths = directoryMapper.getLocals(entry.getPath());
        for (Path localPath : localPaths) {
            delete(localPath);
        }
    }

    private void delete(Path localPath) {
        File fileToDelete = localPath.toFile();
        if (fileToDelete.isFile()) {
            try {
                persistenceController.delete(localPath.toString(), client.getEntityType());
                FileUtils.forceDelete(fileToDelete);
                LOG.debug("Successfully deleted local file {}", localPath);
            } catch (IOException e) {
                LOG.error("Local file {} couldn't be deleted", localPath, e);
            }
        } else {
            LOG.error("Local file isn't deleted because it is a directory");
        }
    }
}
