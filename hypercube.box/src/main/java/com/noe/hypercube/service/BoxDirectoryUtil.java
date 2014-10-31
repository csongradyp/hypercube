package com.noe.hypercube.service;

import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.BoxCollection;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxItem;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxFolderRequestObject;
import com.box.restclientv2.exceptions.BoxRestException;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoxDirectoryUtil {

    private static final String ROOT_DIRECTORY = "0";

    private final BoxClient client;

    public BoxDirectoryUtil(BoxClient client) {
        this.client = client;
    }

    public boolean isFolder(final BoxItem boxItem) {
        return boxItem.getType().equals("folder");
    }

    public String getFilePath(final BoxItem bFile) {
        String folderPath = "";

        final BoxCollection pathCollection = bFile.getPathCollection();
        final ArrayList<BoxTypedObject> entries = pathCollection.getEntries();

        if (pathCollection.getTotalCount() > 0) {
            folderPath = "/";
            for (BoxTypedObject entry : entries) {
                final BoxFolder folder = (BoxFolder) entry;
                if (!folder.getId().equals("0")) {
                    final String folderName = folder.getName();
                    folderPath += folderName;
                }
            }
        }
        return folderPath + "/" + bFile.getName();
    }


    public String getFoldersId(final Path remoteFolder) throws BoxServerException, AuthFatalFailureException, BoxRestException, SynchronizationException {
        final String[] folders = getPathParts(remoteFolder.toString());
        return getId(folders);
    }

    public String getFileId(Path remoteFilePath) throws BoxServerException, BoxRestException, AuthFatalFailureException, SynchronizationException {
        final String[] pathParts = getPathParts(remoteFilePath.toString());
        return getId(pathParts);
    }

    public String getId(final String... pathParts) throws BoxServerException, BoxRestException, AuthFatalFailureException, SynchronizationException {
        final List<String> subPaths = new ArrayList<>();
        Collections.addAll(subPaths, pathParts);

        String id = ROOT_DIRECTORY;
        for (String dirName : pathParts) {
            BoxTypedObject existing = getExisting(dirName, id);
            if (existing == null) {
                throw new SynchronizationException("Box folder/file does not exist");
            }
            subPaths.remove(0);
            id = existing.getId();
        }
        return id;
    }

    private String[] getPathParts(final String remotePath) {
        String path = remotePath;
        if (path.startsWith("\\") || path.startsWith("/")) {
            path = path.substring(1);
        }
        return path.replace("\\", "/").split("/");
    }

    public String createFoldersPath(final Path folder) throws BoxServerException, AuthFatalFailureException, BoxRestException {
        return createFoldersPath(getPathParts(folder.toString()));
    }

    public String createFoldersPath(final String... directories) throws BoxServerException, BoxRestException, AuthFatalFailureException {
        List<String> subDirs = new ArrayList<>();
        Collections.addAll(subDirs, directories);

        String folderId = ROOT_DIRECTORY;
        for (String dirName : directories) {
            BoxTypedObject existingFolder = getExisting(dirName, folderId);
            if (existingFolder == null) {
                existingFolder = createFolder(dirName, folderId);
            }
            subDirs.remove(0);
            folderId = existingFolder.getId();
        }
        return folderId;
    }

    private BoxTypedObject createFolder(final String dirName, final String parentId) throws BoxServerException, AuthFatalFailureException, BoxRestException {
        final BoxFolderRequestObject folderRequestObject = BoxFolderRequestObject.createFolderRequestObject(dirName, parentId);
        return client.getFoldersManager().createFolder(folderRequestObject);
    }

    private BoxTypedObject getExisting(final String searchedName, final String parentId) throws BoxServerException, AuthFatalFailureException, BoxRestException {
        final BoxFolder boxFolder = client.getFoldersManager().getFolder(parentId, null);
        final List<BoxTypedObject> folderEntries = boxFolder.getItemCollection().getEntries();
        for (final BoxTypedObject folderEntry : folderEntries) {
            if (folderEntry instanceof BoxItem) {
                if (((BoxItem) folderEntry).getName().equals(searchedName)) {
                    return folderEntry;
                }
            }
        }
        return null;
    }

}
