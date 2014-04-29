package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static java.lang.String.format;

public class Downloader implements IDownloader {

    private static final Logger LOG = Logger.getLogger(Downloader.class);

    private final IPersistenceController persistenceController;
    private final IClient client;
    private final DirectoryMapper directoryMapper;
    private final FileEntityFactory entityFactory;
    private final BlockingQueue<ServerEntry> downloadQ;

    private boolean stop = false;

     public Downloader(IClient client, DirectoryMapper directoryMapper, FileEntityFactory entityFactory, IPersistenceController persistenceController) {
         this.client = client;
         this.persistenceController = persistenceController;
         this.directoryMapper = directoryMapper;
         this.entityFactory = entityFactory;
         downloadQ = new LinkedBlockingDeque<>(20);
    }

    @Override
    public void download(ServerEntry entry) {
        downloadQ.add(entry);
    }

    @Override
    public void run() {
        while(!stop) {
            ServerEntry entry = downloadQ.poll();
            if (client.exist(entry)) {
                downloadFromServer(entry);
            }
            else {
                deleteLocalFile(entry);
            }
        }
    }

    public void stop() {
        stop = true;
    }

    public void restart() {
        stop = false;
        run();
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
                    try (FileOutputStream outputStream = FileUtils.openOutputStream(newLocalFile)) {
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
