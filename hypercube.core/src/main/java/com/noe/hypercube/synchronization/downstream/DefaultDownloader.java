package com.noe.hypercube.synchronization.downstream;


import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

public abstract class DefaultDownloader implements IDownloader {

    private static final Logger LOG = Logger.getLogger(DefaultDownloader.class);

    private final IClient client;
    private final IMapper<? extends Account, ? extends MappingEntity> directoryMapper;
    @Inject
    private IPersistenceController persistenceController;

    protected DefaultDownloader(IClient client, IMapper<? extends Account, ? extends MappingEntity> directoryMapper, IPersistenceController persistenceController) {
        this.client = client;
        this.directoryMapper = directoryMapper;
        this.persistenceController = persistenceController;
    }

    protected DefaultDownloader(IClient client, IMapper<? extends Account, ? extends MappingEntity> directoryMapper) {
        this.client = client;
        this.directoryMapper = directoryMapper;
    }

    @Override
    public void download(ServerEntry entry) {
        synchronize(entry);
    }

    @Override
    public void run() {
        try {
            Collection<ServerEntry> delta = client.getChanges();
            if (delta != null && !delta.isEmpty()) {
                LOG.debug(format("Detected %d changes to process ...", delta.size()));
                synchronize(delta);
            }
        } catch (Exception e) {
            LOG.error("Error occurred while synchronize with " + client.getAccountName(), e);
        }
    }

    private void synchronize(Collection<ServerEntry> delta) throws Exception {
        for (ServerEntry entry : delta) {
            synchronize(entry);
        }
    }

    private void synchronize(ServerEntry entry) {
        if(isMapped(entry)) {
            LOG.debug("Mapped content found");
            if (client.exist(entry)) {
                downloadFromServer(entry);
            }
            else {
                deleteLocalFile(entry);
            }
        }
        else {
            LOG.debug("File not Mapped - will not process " + entry.getPath());
        }
    }

    private boolean isMapped(ServerEntry entry) {
        Collection<MappingEntity> mappings = persistenceController.getMappings(directoryMapper.getMappingClass());
        for (MappingEntity mapping : mappings) {
            if(entry.getPath().toString().contains(mapping.getRemoteDir().toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    private boolean isNew(ServerEntry entry, Path localPath){
        FileEntity fileEntity = persistenceController.get(localPath.toString(), client.getEntityType());
        return fileEntity != null && !isSameRevision(entry, fileEntity);
    }

    private boolean isSameRevision(ServerEntry entry, FileEntity dbEntry) {
        return dbEntry.getRevision().equals(entry.getRevision());
    }

    private void downloadFromServer(ServerEntry entry) {
        if (entry.isFile()) {
            List<Path> localPaths = directoryMapper.getLocals(entry.getPath());
            for(Path localPath : localPaths) {
                if(isNew(entry, localPath)) {
                    File newLocalFile = localPath.toFile();
                    createDirsFor(newLocalFile);
                    try (FileOutputStream outputStream = new FileOutputStream(newLocalFile)) {
                        client.download(entry, outputStream);
                        persist(entry, localPath);
                        LOG.debug("Successfully downloaded file " + localPath);
                    } catch (FileNotFoundException e) {
                        LOG.error("Couldn't write file '" + localPath + "'", e);
                    } catch (IOException e) {
                        LOG.error("Error occurred while downloading file from " + client.getAccountName(), e);
                    } catch (SynchronizationException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    setLocalFileLastModifiedDate(entry, newLocalFile);
                }
            }
        }
    }

    private void downloadFile(IClient client, ServerEntry entry) throws Exception {
        if (entry.isFile()) {
            List<Path> localPaths = directoryMapper.getLocals(entry.getPath());
            for(Path localPath : localPaths) {
                if(isNew(entry, localPath)) {

                    File newLocalFile = localPath.toFile();
                    createDirsFor(newLocalFile);

                    try (FileOutputStream outputStream = new FileOutputStream(newLocalFile)) {
                        client.download(entry, outputStream);
                        persist(entry, localPath);
                        LOG.debug(format("Successfully downloaded file %s", localPath));
                    } catch (FileNotFoundException e) {
                        throw new Exception("Couldn't write file '" + localPath + "'", e);
                    }
                    setLocalFileLastModifiedDate(entry, newLocalFile);
                }
            }
        }
    }

    private void createDirsFor(File newLocalFile) {
        if (!newLocalFile.getParentFile().exists()) {
            newLocalFile.getParentFile().mkdirs();
        }
    }
    private void setLocalFileLastModifiedDate(ServerEntry entry, File newLocalFile) {
        long lastModified = entry.lastModified().getTime();
        boolean success = newLocalFile.setLastModified(lastModified);
        if (!success) {
            LOG.debug(format("Couldn't change attribute 'lastModified' of file %s", newLocalFile.getPath()));
        }
    }

    private void persist(ServerEntry entry, Path localPath) {
        FileEntity fileEntity = createFileEntity(localPath.toString(), entry.getRevision(), entry.lastModified());
        persistenceController.save(fileEntity);
    }

    protected abstract FileEntity createFileEntity(String localPath, String revision, Date date);


    private void deleteLocalFile(ServerEntry entry) {
        List<Path> localPaths = directoryMapper.getLocals(entry.getPath());
        for (Path localPath : localPaths) {
            delete(localPath);
        }
    }
    private void delete(Path localPath) {
        File fileToDelete = localPath.toFile();
        if (!fileToDelete.isDirectory()) {
            try {
                persistenceController.delete(localPath.toString(), client.getEntityType());
                FileUtils.forceDelete(fileToDelete);
                LOG.debug(format("Successfully deleted local file %s", localPath));
            } catch (IOException e) {
                LOG.error(format("Local file %s couldn't be deleted", localPath) ,e);
            }
        } else {
            LOG.error("Local file isn't deleted because it is a directory");
        }
    }
}
