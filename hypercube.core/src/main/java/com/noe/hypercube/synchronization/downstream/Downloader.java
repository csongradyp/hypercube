package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.Action;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.FileActionType;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.persistence.FileEntityFactory;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.LocalFileEntity;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.noe.hypercube.Action.*;


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
    public void run() {
        while (!stop.get()) {
            final ServerEntry entry = getNext();
            LOG.info("{} downloader: {} was taken from queue", entry.getAccount(), entry.getPath() == null ? entry.getId() : entry.getPath());
            LOG.debug("Download queue: {}", downloadQ);
            try {
                if (client.exist(entry)) {
                    downloadFromServer(entry);
                } else {
                    deleteLocalFile(entry);
                }
            } catch (SynchronizationException e) {
                EventBus.publishDownloadFailed(new FileEvent(client.getAccountName(), e.getRelatedFile(), entry.getPath(), FileActionType.ADDED));
            }
            logQueueEmpty();
        }
    }

    @Override
    public void download(final ServerEntry entry) {
        downloadQ.add(entry);
        LOG.info("{} file: {} has been added to the download queue", entry.getAccount(), entry.getPath() == null ? entry.getId() : entry.getPath());
    }

    @Override
    public void download(final Path serverPath, final Path localFolder) throws SynchronizationException {
        File newLocalFile = Paths.get(localFolder.toString(), serverPath.getFileName().toString()).toFile();
        try (FileOutputStream outputStream = FileUtils.openOutputStream(newLocalFile)) {
            final ServerEntry serverEntry = client.download(serverPath.toString(), outputStream);
            persist(serverEntry, newLocalFile.toPath());
            LOG.info("{} Successfully downloaded {}", client.getAccountName(), newLocalFile.toPath());
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't write file {}", newLocalFile.toPath(), e);
        } catch (IOException e) {
            LOG.error("Error occurred while downloading file from {}", client.getAccountName(), e);
        } catch (SynchronizationException e) {
            e.setRelatedFile(newLocalFile.toPath());
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private ServerEntry getNext() {
        ServerEntry entry = null;
        try {
            entry = downloadQ.take();
        } catch (InterruptedException e) {
            LOG.error(String.format("%s download queue operation has been interrupted", client.getAccountName()));
        }
        return entry;
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

    private Action getDeltaAction(final ServerEntry entry, final File localPath) {
        FileEntity fileEntity = persistenceController.get(localPath.toString(), client.getEntityType());
        if (fileEntity != null) {
            if (isDifferentRevision(entry, fileEntity)) {
                return CHANGED;
            }
            return IDENTICAL;
        }
        return ADDED;
    }

    private boolean isDifferentRevision(final ServerEntry entry, final FileEntity dbEntry) {
        return !dbEntry.getRevision().equals(entry.getRevision());
    }

    private File createNewLocalFileReference(final ServerEntry entry, final Path localPath) {
        File newLocalFile = new File(localPath.toString(), entry.getPath().getFileName().toString());
        if (isConflicted(newLocalFile)) {
            LOG.warn("{} Conflict {}", client.getAccountName(), newLocalFile);
            final String conflictedFileName = FileConflictNamingUtil.resolveFileName(entry.getPath(), entry.getAccount());
            LOG.info("{} already exists! File name updated to: {}", newLocalFile.toPath(), conflictedFileName);
            newLocalFile = new File(localPath.toString(), conflictedFileName);
        }
        return newLocalFile;
    }


    private boolean isConflicted(final File newLocalFile) {
        return newLocalFile.exists() && isNotMapped(newLocalFile);
    }

    private boolean isNotMapped(final File newLocalFile) {
        return persistenceController.get(newLocalFile.toPath().toString(), client.getEntityType()) == null;
    }

    private void downloadFromServer(final ServerEntry entry) {
        if (entry.isFile()) {
            final List<Path> localPaths = directoryMapper.getLocals(entry.getPath());
            for (Path localPath : localPaths) {
                final File newLocalFile = createNewLocalFileReference(entry, localPath);
                final Action action = getDeltaAction(entry, newLocalFile);
                final String accountName = client.getAccountName();
                if (ADDED == action) {
                    final FileEvent event = new FileEvent(accountName, entry.getPath(), newLocalFile.toPath(), FileActionType.ADDED);
                    EventBus.publishDownloadStart(event);
                    createDirsFor(newLocalFile);
                    download(entry, newLocalFile);
                    EventBus.publishDownloadFinished(event);
                } else if (CHANGED == action) {
                    final FileEvent event = new FileEvent(accountName, entry.getPath(), newLocalFile.toPath(), FileActionType.UPDATED);
                    EventBus.publishDownloadStart(event);
                    download(entry, newLocalFile);
                    EventBus.publishDownloadFinished(event);
                } else {
                    LOG.debug("{} is up to date", localPath);
                }
            }
        }
    }


    private void createDirsFor(final File newLocalFile) {
        if (!newLocalFile.getParentFile().exists()) {
            boolean success = newLocalFile.getParentFile().mkdirs();
            if (!success) {
                LOG.error("Directory creation failed for {}", newLocalFile.getPath());
            }
        }
    }

    private void download(final ServerEntry entry, final File newLocalFile) {
        Long checksum = -1L;
        Path tempFile = null;
        if(newLocalFile.exists()) {
            try {
                checksum = FileUtils.checksumCRC32(newLocalFile);
                final Path folder = getOrCreateFolderFor(newLocalFile);
                tempFile = Files.createTempFile(folder, "", ".hyperTmp");
                downloadToTempFile(entry, newLocalFile, tempFile.toFile());
            } catch (IOException e) {
                LOG.error("Error occurred while creating temp file for {}", newLocalFile);
            }
        }
        try {
            if (tempFile != null && (!newLocalFile.exists() || FileUtils.checksumCRC32(newLocalFile) == checksum)) {
                Files.move(tempFile, newLocalFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadToTempFile(final ServerEntry entry, final File newLocalFile, final File tempFile) {
        try {
            FileOutputStream outputStream = FileUtils.openOutputStream(tempFile);
            client.download(entry, outputStream);
            FileUtils.moveFile(tempFile, newLocalFile);
            persist(entry, newLocalFile.toPath());
            LOG.info("{} Successfully downloaded {}", client.getAccountName(), newLocalFile.toPath());
        } catch (FileNotFoundException e) {
            LOG.error("{}: Couldn't write file {}", client.getAccountName(), newLocalFile.toPath(), e);
        } catch (IOException e) {
            LOG.error("Error occurred while downloading file from {}", client.getAccountName(), e);
        } catch (SynchronizationException e) {
            e.setRelatedFile(newLocalFile.toPath());
            LOG.error(e.getMessage(), e);
        }
    }

    private static Path getOrCreateFolderFor(final File newLocalFile) throws IOException {
        final Path folder = newLocalFile.toPath().getParent();
        if (!folder.toFile().exists()) {
            Files.createDirectory(folder);
        }
        return folder;
    }

    private void persist(final ServerEntry entry, final Path localPath) {
        FileEntity fileEntity = entityFactory.createFileEntity(localPath.toString(), entry.getPath().toString(), entry.getRevision(), entry.lastModified());
        persistenceController.save(fileEntity);
        persistenceController.save(new LocalFileEntity(localPath.toFile()));
    }

    private void deleteLocalFile(final ServerEntry entry) {
        List<Path> localDirs = directoryMapper.getLocals(entry.getPath());
        for (Path localDir : localDirs) {
            final Path localFile = Paths.get(localDir.toString(), entry.getPath().getFileName().toString());
            delete(localFile, entry);
        }
    }

    private void delete(final Path localFile, final ServerEntry entry) {
        File fileToDelete = localFile.toFile();
        if (fileToDelete.isFile()) {
            try {
                persistenceController.delete(localFile.toString(), client.getEntityType());
                FileUtils.forceDelete(fileToDelete);
                EventBus.publishDownloadFinished(new FileEvent(client.getAccountName(), localFile, entry.getPath(), FileActionType.DELETED));
                LOG.debug("Successfully deleted local file {}", localFile);
            } catch (IOException e) {
                LOG.error("Local file {} couldn't be deleted", localFile, e);
            }
        } else {
            LOG.error("Local file isn't deleted because it is a directory");
        }
    }
}
