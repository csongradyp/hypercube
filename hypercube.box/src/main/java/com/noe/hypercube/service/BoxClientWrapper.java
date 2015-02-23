package com.noe.hypercube.service;


import com.box.sdk.*;
import com.box.sdk.EventListener;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxClientWrapper extends Client<Box, BoxAPIConnection, BoxFileEntity, BoxMapping> {

    private static final Logger LOG = LoggerFactory.getLogger(BoxClientWrapper.class);
    public static final int EVENT_CACHE_CAPACITY = 512;

    private BoxDirectoryUtil directoryUtil;
    private CircularFifoQueue<String> lastRecentEvents;

    private EventStream eventStream;

    private final Collection<ServerEntry> changes = new CopyOnWriteArrayList<>();

    @Inject
    public BoxClientWrapper(final Authentication<BoxAPIConnection> boxAuthentication) {
        super(boxAuthentication);
        lastRecentEvents = new CircularFifoQueue<>(EVENT_CACHE_CAPACITY);
        setOnAccountAttached(boxClientWrapper -> directoryUtil = new BoxDirectoryUtil(getClient()));
    }

    @PostConstruct
    public void beginPollChanges() {
        eventStream = new EventStream(getClient());
        eventStream.addListener(new EventListener() {
            @Override
            public void onEvent(BoxEvent event) {
                final String eventId = event.getID();
                if (lastRecentEvents.parallelStream().noneMatch(storedId -> storedId.equals(eventId))) {
                    final BoxResource source = event.getSourceInfo().getResource();
                    if (BoxFile.class.isAssignableFrom(source.getClass())) {
                        BoxFile bFile = (BoxFile) source;
                        changes.add(new BoxServerEntry(directoryUtil.getFilePath(bFile.getInfo()), bFile.getID(), bFile.getInfo().getSize(), bFile.getInfo().getVersionNumber(), bFile.getInfo().getContentModifiedAt(), false));
                    } else {
                        BoxFolder bFolder = (BoxFolder) source;
                        changes.add(new BoxServerEntry(directoryUtil.getFilePath(bFolder.getInfo()), bFolder.getID(), bFolder.getInfo().getSize(), null, bFolder.getInfo().getContentModifiedAt(), true));
                    }
                }
                lastRecentEvents.add(eventId);
            }

            @Override
            public void onNextPosition(long l) {
            }

            @Override
            public boolean onException(Throwable throwable) {
                LOG.error("Box: Error occurred while getting changes", throwable);
                return false;
            }
        });
        try {
            eventStream.start();
        } catch (Exception e) {
            LOG.error("Box: ", e);
        }
    }

    @PreDestroy
    public void closeEventStream() {
        eventStream.stop();
    }

    @Override
    protected boolean testConnection() {
        // TODO
        return true;
    }

    @Override
    public String getAccountName() {
        return Box.getName();
    }

    @Override
    public Class<Box> getAccountType() {
        return Box.class;
    }

    @Override
    public Class<BoxFileEntity> getEntityType() {
        return BoxFileEntity.class;
    }

    @Override
    public Class<BoxMapping> getMappingType() {
        return BoxMapping.class;
    }

    @Override
    public boolean exist(final UploadEntity uploadEntity) {
        try {
            return directoryUtil.getFileId(uploadEntity.getRemoteFilePath()) != null;
        } catch (SynchronizationException e) {
            return false;
        }
    }

    @Override
    public boolean exist(final ServerEntry serverEntry) {
        if (serverEntry.isFile()) {
            BoxFile file = new BoxFile(getClient(), serverEntry.getId());
            return file.getInfo().getItemStatus().equals("active");
        } else {
            BoxFolder folder = new BoxFolder(getClient(), serverEntry.getId());
            return folder.getInfo().getItemStatus().equals("active");
        }
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        final Collection<ServerEntry> serverEntries = new LinkedList<>();
        serverEntries.addAll(changes);
        changes.clear();
        return serverEntries;
    }

    @Override
    public void download(final ServerEntry serverEntry, final FileOutputStream outputStream) throws SynchronizationException {
        new BoxFile(getClient(), serverEntry.getId()).download(outputStream);
    }

    @Override
    public ServerEntry download(final String serverPath, final FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException {
        final String fileId = directoryUtil.getFileId(Paths.get(serverPath));
        new BoxFile(getClient(), fileId).download(outputStream);
        return new BoxServerEntry(serverPath, fileId, false);
    }

    @Override
    public void delete(final Path remoteFilePath) throws SynchronizationException {
        final String fileId = directoryUtil.getFileId(remoteFilePath);
        new BoxFile(getClient(), fileId).delete();
    }

    @Override
    public void delete(final String remoteFileId) throws SynchronizationException {
        new BoxFile(getClient(), remoteFileId).delete();
    }

    @Override
    public ServerEntry uploadAsNew(final UploadEntity uploadEntity) throws SynchronizationException {
        final String folderId = directoryUtil.createFoldersPath(uploadEntity.getRemoteFolder());
        BoxFolder folder = new BoxFolder(getClient(), folderId);
        try (final FileInputStream fileInputStream = FileUtils.openInputStream(uploadEntity.getFile())) {
            final BoxFile.Info bFile = folder.uploadFile(fileInputStream, uploadEntity.getFile().getName());
            return new BoxServerEntry(directoryUtil.getFilePath(bFile), bFile.getID(), bFile.getSize(), bFile.getSequenceID(), bFile.getModifiedAt(), false);
        } catch (IOException e) {
            throw new SynchronizationException("Box: Error while uploading file ", e);
        }
    }

    @Override
    public ServerEntry uploadAsUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        BoxFile boxFile = null;
        final String fileId = directoryUtil.getFileId(uploadEntity.getRemoteFilePath());
        try (final FileInputStream fileInputStream = FileUtils.openInputStream(uploadEntity.getFile())) {
            BoxFile file = new BoxFile(getClient(), fileId);
            file.uploadVersion(fileInputStream);
            return new BoxServerEntry(directoryUtil.getFilePath(boxFile.getInfo()), boxFile.getID(), boxFile.getInfo().getSize(), boxFile.getInfo().getSequenceID(), boxFile.getInfo().getModifiedAt(), false);
        } catch (IOException e) {
            throw new SynchronizationException("Box: Error while uploading file ", e);
        }
    }

    @Override
    public FileEntity rename(FileEntity remoteFile, String newName) throws SynchronizationException {
        BoxFile file = new BoxFile(getClient(), remoteFile.getId());
        BoxFile.Info info = file.new Info();
        info.setName(newName);
        file.updateInfo(info);
        return new BoxFileEntity(remoteFile.getLocalPath(), remoteFile.getRemotePath(), file.getInfo().getVersionNumber());
    }

    @Override
    public FileEntity rename(ServerEntry remoteFile, String newName) throws SynchronizationException {
        BoxFile file = new BoxFile(getClient(), remoteFile.getId());
        BoxFile.Info info = file.new Info();
        info.setName(newName);
        file.updateInfo(info);
        return new BoxFileEntity(null, remoteFile.getPath().toString(), file.getInfo().getVersionNumber());
    }

    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();

        final String foldersId = directoryUtil.getFoldersId(remoteFolder);
        final BoxFolder boxFolder = new BoxFolder(getClient(), foldersId);
        final Iterable<BoxItem.Info> children = boxFolder.getChildren();
        children.forEach(boxItem -> {
            fileList.add(new BoxServerEntry(directoryUtil.getFilePath(boxItem), boxItem.getID(), boxItem.getSize(), boxItem.getSequenceID(), boxItem.getContentModifiedAt(), directoryUtil.isFolder(boxItem)));
        });
        return fileList;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();
        BoxFolder rootFolder = BoxFolder.getRootFolder(getClient());
        final Iterable<BoxItem.Info> children = rootFolder.getChildren();
        children.forEach(boxItem -> {
            final String id = boxItem.getID();
            final String name = boxItem.getName();
            final long size = boxItem.getSize();
            final String sequenceId = boxItem.getSequenceID();
            final Date lastModified = boxItem.getModifiedAt();
            fileList.add(new BoxServerEntry(name, id, size, sequenceId, lastModified, directoryUtil.isFolder(boxItem)));
        });
        return fileList;
    }

    @Override
    public void createFolder(final Path folder) throws SynchronizationException {
        directoryUtil.createFoldersPath(folder);
    }

    @Override
    public AccountQuota getQuota() throws SynchronizationException {
        return new AccountQuota(0L, 0L);
    }
}
