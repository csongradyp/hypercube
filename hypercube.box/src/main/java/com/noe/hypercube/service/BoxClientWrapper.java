package com.noe.hypercube.service;


import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.*;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxJSONException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxPagingRequestObject;
import com.box.boxjavalibv2.utils.ISO8601DateParser;
import com.box.restclientv2.exceptions.BoxRestException;
import com.box.restclientv2.requestsbase.BoxDefaultRequestObject;
import com.box.restclientv2.requestsbase.BoxFileUploadRequestObject;
import com.noe.hypercube.BoxAuthentication;
import com.noe.hypercube.domain.AccountQuota;
import com.noe.hypercube.domain.BoxFileEntity;
import com.noe.hypercube.domain.BoxServerEntry;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

public class BoxClientWrapper extends Client<Box, BoxFileEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(BoxClientWrapper.class);

    private final BoxClient client;
    private final BoxDirectoryUtil directoryUtil;

    public BoxClientWrapper() {
        this.client = BoxAuthentication.create();
        directoryUtil = new BoxDirectoryUtil(client);
    }

    @Override
    protected boolean testConnectionActive() {
        try {
            return !getRootFileList().isEmpty();
        } catch (SynchronizationException e) {
            return false;
        }
    }

    @Override
    public String getAccountName() {
        return Box.name;
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
    public boolean exist(File fileToUpload, Path remotePath) {
        throw new UnsupportedOperationException("cannot check existence via path");
    }

    @Override
    public boolean exist(ServerEntry serverEntry) {
        BoxServerEntry boxServerEntry = (BoxServerEntry) serverEntry;
        try {
            final BoxItem item = client.getBoxItemsManager().getItem(boxServerEntry.getId(), null, BoxResourceType.FILE);
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

        return serverEntries;
    }

    @Override
    public void download(ServerEntry serverEntry, FileOutputStream outputStream) throws SynchronizationException {
        BoxServerEntry boxServerEntry = (BoxServerEntry) serverEntry;
        OutputStream[] o = {outputStream};
        try {
            client.getFilesManager().downloadFile(boxServerEntry.getId(), o, null, null);
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
    public ServerEntry download(String serverPath, FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException {
        return new BoxServerEntry("", false);
    }

    @Override
    public void delete(final Path remoteFilePath) throws SynchronizationException {
        throw new UnsupportedOperationException("Box support deleting by file id");
    }

    @Override
    public void delete(String remoteFileId) throws SynchronizationException {
        BoxDefaultRequestObject requestObj = new BoxDefaultRequestObject();
//        requestObj.getRequestExtras().setIfMatch(etag);
        try {
            client.getFilesManager().deleteFile(remoteFileId, requestObj);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        BoxFile bFile = null;
        try {
            BoxFileUploadRequestObject requestObj = BoxFileUploadRequestObject.uploadFileRequestObject("0", fileToUpload.toPath().getFileName().toString(), fileToUpload);
//            requestObj.setListener(listener);
            bFile = client.getFilesManager().uploadFile(requestObj);
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

        return new BoxServerEntry(directoryUtil.getFilePath(bFile), bFile.getSize().longValue(), bFile.getSequenceId(), getLastModified(bFile), false);
    }

    private Date getLastModified(final BoxItem boxItem) {
        try {
            return ISO8601DateParser.parse(boxItem.getModifiedAt());
        } catch (ParseException e) {
            return new Date();
        }
    }

    @Override
    public ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return new BoxServerEntry("", false);
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
            final List<BoxTypedObject> folderEntries = client.getFoldersManager().getFolderItems(foldersId, pagingRequestObject).getEntries();
            for (BoxTypedObject entry : folderEntries) {
                if (entry instanceof BoxItem) {
                    BoxItem boxItem = (BoxItem) entry;
                    fileList.add(new BoxServerEntry(directoryUtil.getFilePath(boxItem), boxItem.getSize().longValue(), boxItem.getSequenceId(), boxItem.dateModifiedAt(), directoryUtil.isFolder(boxItem)));
                }
            }
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
            BoxFolder boxFolder = client.getFoldersManager().getFolder("0", null);
            final BoxCollection itemCollection = boxFolder.getItemCollection();
            List<BoxTypedObject> folderEntries = itemCollection.getEntries();
            for (BoxTypedObject entry : folderEntries) {
                if (entry instanceof BoxItem) {
                    BoxItem boxItem = (BoxItem) entry;
                    final String name = boxItem.getName();
//                    final long size = boxItem.getSize().longValue();
                    final long size = 0L;
                    final String sequenceId = boxItem.getSequenceId();
                    final Date lastModified = getLastModified(boxItem);
                    fileList.add(new BoxServerEntry(name, size, sequenceId, lastModified, directoryUtil.isFolder(boxItem)));
                }
            }
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
