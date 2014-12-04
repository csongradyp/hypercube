package com.noe.hypercube.service;


import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.*;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxJSONException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxEventRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxFileRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxPagingRequestObject;
import com.box.boxjavalibv2.utils.ISO8601DateParser;
import com.box.restclientv2.exceptions.BoxRestException;
import com.box.restclientv2.requestsbase.BoxDefaultRequestObject;
import com.box.restclientv2.requestsbase.BoxFileUploadRequestObject;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxClientWrapper extends Client<Box, BoxClient, BoxFileEntity, BoxMapping> {

    private static final Logger LOG = LoggerFactory.getLogger(BoxClientWrapper.class);
    public static final int EVENT_CACHE_CAPACITY = 500;

    private BoxDirectoryUtil directoryUtil;
    private Long streamPosition;
    private CircularFifoQueue<String> lastRecentEvents;

    @Inject
    public BoxClientWrapper(final Authentication<BoxClient> boxAuthentication) {
        super(boxAuthentication);
//        streamPosition = 0L;
        streamPosition = (long) BoxEventRequestObject.STREAM_POSITION_NOW;
        lastRecentEvents = new CircularFifoQueue<>(EVENT_CACHE_CAPACITY);
    }

    @PostConstruct
    public void init() {
        directoryUtil = new BoxDirectoryUtil(getClient());
        getClient().setAutoRefreshOAuth(true);
        getClient().addOAuthRefreshListener(newAuthData -> storeNewTokens(newAuthData.getRefreshToken(), newAuthData.getAccessToken()));
    }

    @Override
    protected boolean testConnectionActive() {
        try {
            getClient().getAuthData().getExpiresIn();
            return true;
        } catch (AuthFatalFailureException e) {
            return false;
        }
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
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        } catch (SynchronizationException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean exist(final ServerEntry serverEntry) {
        BoxServerEntry boxServerEntry = (BoxServerEntry) serverEntry;
        try {
            final BoxItem item = getClient().getBoxItemsManager().getItem(boxServerEntry.getId(), null, BoxResourceType.FILE);
            return item.getItemStatus().equals("active");
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> serverEntries = new LinkedList<>();
        try {
            final BoxEventRequestObject eventsRequestObject1 = BoxEventRequestObject.getEventsRequestObject(streamPosition);
            eventsRequestObject1.setStreamType(BoxEventRequestObject.STREAM_TYPE_CHANGES);
            final BoxEventCollection boxEventCollection = getClient().getEventsManager().getEvents(eventsRequestObject1);
            final List<BoxTypedObject> entries = boxEventCollection.getEntries();
            for (BoxTypedObject eventObject : entries) {
                BoxEvent event = (BoxEvent) eventObject;
                final String eventId = event.getId();
                if(lastRecentEvents.parallelStream().noneMatch(storedId -> storedId.equals(eventId))) {
                    final BoxTypedObject source = event.getSource();
                    if(BoxFile.class.isAssignableFrom(source.getClass())) {
                        BoxFile bFile = (BoxFile) source;
                        serverEntries.add(new BoxServerEntry(directoryUtil.getFilePath(bFile), bFile.getId(), bFile.getSize().longValue(), bFile.getSequenceId(), getLastModified(bFile), false));
                    }
                }
                lastRecentEvents.add(eventId);
            }
            streamPosition = boxEventCollection.getNextStreamPosition();
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            throw new SynchronizationException("Box: Error occurred while getting changes", e);
        } catch (AuthFatalFailureException e) {
            throw new SynchronizationException("Box authentication error", e);
        }
        return serverEntries;
    }

    @Override
    public void download(final ServerEntry serverEntry, final FileOutputStream outputStream) throws SynchronizationException {
        final BoxServerEntry boxServerEntry = (BoxServerEntry) serverEntry;
        final OutputStream[] outputStreams = {outputStream};
        try {
            getClient().getFilesManager().downloadFile(boxServerEntry.getId(), outputStreams, null, null);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }

    }

    @Override
    public ServerEntry download(final String serverPath, final FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException {
        try {
            final String fileId = directoryUtil.getFileId(Paths.get(serverPath));
            final OutputStream[] outputStreams = {outputStream};
            getClient().getFilesManager().downloadFile(fileId, outputStreams, null, null);
            return new BoxServerEntry(serverPath, fileId, false);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        throw new SynchronizationException("Could not download file: Unexpected error");
    }

    @Override
    public void delete(final Path remoteFilePath) throws SynchronizationException {
        try {
            final String fileId = directoryUtil.getFileId(remoteFilePath);
            delete(fileId);
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(final String remoteFileId) throws SynchronizationException {
        try {
            BoxDefaultRequestObject requestObj = new BoxDefaultRequestObject();
//        requestObj.getRequestExtras().setIfMatch(etag);
            getClient().getFilesManager().deleteFile(remoteFileId, requestObj);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServerEntry uploadAsNew(final UploadEntity uploadEntity) throws SynchronizationException {
        BoxFile bFile = null;
        try {
            final String folderId = directoryUtil.createFoldersPath(uploadEntity.getRemoteFolder());
            BoxFileUploadRequestObject requestObj = BoxFileUploadRequestObject.uploadFileRequestObject(folderId, uploadEntity.getRemoteFilePath().getFileName().toString(), uploadEntity.getFile());
//            requestObj.setListener(listener);
            bFile = getClient().getFilesManager().uploadFile(requestObj);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxJSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return new BoxServerEntry(directoryUtil.getFilePath(bFile), bFile.getId(), bFile.getSize().longValue(), bFile.getSequenceId(), getLastModified(bFile), false);
    }

    @Override
    public ServerEntry uploadAsUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        BoxFile boxFile = null;
        try {
            final String fileId = directoryUtil.getFileId(uploadEntity.getRemoteFilePath());
            final BoxFileUploadRequestObject requestObj = BoxFileUploadRequestObject.uploadNewVersionRequestObject(fileId, uploadEntity.getFile());
            boxFile = getClient().getFilesManager().uploadFile(requestObj);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return new BoxServerEntry(directoryUtil.getFilePath(boxFile), boxFile.getId(), boxFile.getSize().longValue(), boxFile.getSequenceId(), getLastModified(boxFile), false);
    }

    @Override
    public FileEntity rename(FileEntity remoteFile, String newName) throws SynchronizationException {
        try {
            final BoxFileRequestObject requestObject = BoxFileRequestObject.getRequestObject();
            requestObject.setName(newName);
            final BoxFile boxFile = getClient().getFilesManager().updateFileInfo(remoteFile.getId(), requestObject);
            return new BoxFileEntity(remoteFile.getLocalPath(), remoteFile.getRemotePath(), boxFile.getSha1());
        } catch (BoxRestException | BoxServerException | AuthFatalFailureException | UnsupportedEncodingException e) {
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            throw new SynchronizationException(String.format("An error occurred while rename file %s", remoteFile.getRemotePath()), e);
        }
    }

    @Override
    public FileEntity rename(ServerEntry remoteFile, String newName) throws SynchronizationException {
        try {
            final BoxFileRequestObject requestObject = BoxFileRequestObject.getRequestObject();
            requestObject.setName(newName);
            final BoxFile boxFile = getClient().getFilesManager().updateFileInfo(remoteFile.getId(), requestObject);
            return new BoxFileEntity(null, remoteFile.getPath().toString(), boxFile.getSha1());
        } catch (BoxRestException | BoxServerException | AuthFatalFailureException | UnsupportedEncodingException e) {
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            throw new SynchronizationException(String.format("An error occurred while rename file %s", remoteFile.getPath()), e);
        }
    }

    private Date getLastModified(final BoxItem boxItem) {
        if (boxItem != null && boxItem.getModifiedAt() != null) {
            try {
                return ISO8601DateParser.parse(boxItem.getModifiedAt());
            } catch (ParseException e) {
                return new Date();
            }
        }
        return null;
    }

    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();

        BoxPagingRequestObject pagingRequestObject = BoxPagingRequestObject.pagingRequestObject(1000, 0);
        pagingRequestObject.getRequestExtras().addField(BoxFolder.FIELD_NAME);
        pagingRequestObject.getRequestExtras().addField(BoxFolder.FIELD_PATH_COLLECTION);
        pagingRequestObject.getRequestExtras().addField(BoxFolder.FIELD_SIZE);
        pagingRequestObject.getRequestExtras().addField(BoxFolder.FIELD_MODIFIED_AT);
        pagingRequestObject.getRequestExtras().addField(BoxFolder.FIELD_ETAG);

        try {
            final String foldersId = directoryUtil.getFoldersId(remoteFolder);
            final List<BoxTypedObject> folderEntries = getClient().getFoldersManager().getFolderItems(foldersId, pagingRequestObject).getEntries();
            folderEntries.stream().filter(entry -> entry instanceof BoxItem).forEach(entry -> {
                BoxItem boxItem = (BoxItem) entry;
                fileList.add(new BoxServerEntry(directoryUtil.getFilePath(boxItem), boxItem.getId(), boxItem.getSize().longValue(), boxItem.getSequenceId(), boxItem.dateModifiedAt(), directoryUtil.isFolder(boxItem)));
            });
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();

        try {
            BoxFolder boxFolder = getClient().getFoldersManager().getFolder("0", null);
            final BoxCollection itemCollection = boxFolder.getItemCollection();
            List<BoxTypedObject> folderEntries = itemCollection.getEntries();
            //                    final long size = boxItem.getSize().longValue();
            folderEntries.stream().filter(entry -> entry instanceof BoxItem).forEach(entry -> {
                BoxItem boxItem = (BoxItem) entry;
                final String id = entry.getId();
                final String name = boxItem.getName();
//                    final long size = boxItem.getSize().longValue();
                final long size = 0L;
                final String sequenceId = boxItem.getSequenceId();
                final Date lastModified = getLastModified(boxItem);
                fileList.add(new BoxServerEntry(name, id, size, sequenceId, lastModified, directoryUtil.isFolder(boxItem)));
            });
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    @Override
    public void createFolder(final Path folder) throws SynchronizationException {
        try {
            directoryUtil.createFoldersPath(folder);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AccountQuota getQuota() throws SynchronizationException {
        return new AccountQuota(0L, 0L);
    }
}
