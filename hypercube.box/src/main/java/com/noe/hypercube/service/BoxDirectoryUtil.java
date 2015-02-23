package com.noe.hypercube.service;


import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoxDirectoryUtil {

    private static final String ROOT_DIRECTORY = "0";

    private final BoxAPIConnection client;

    public BoxDirectoryUtil(final BoxAPIConnection client) {
        this.client = client;
    }

    public boolean isFolder(final BoxItem boxItem) {
        return BoxFolder.class.isAssignableFrom(boxItem.getClass());
    }

    public boolean isFolder(BoxItem.Info boxItemInfo) {
        final String description = boxItemInfo.getDescription();
        final List<String> tags = boxItemInfo.getTags();
        return false;
    }

    public String getFilePath(final BoxItem.Info bFile) {
        String folderPath = "";

        final List<BoxFolder> folders = bFile.getPathCollection();

        for (BoxFolder folder : folders) {
            if (!folder.getInfo().getID().equals("0")) {
                final String folderName = folder.getInfo().getName();
                folderPath += folderName;
            }
        }
        return folderPath + "/" + bFile.getName();
    }

    public String getFoldersId(final Path remoteFolder) throws SynchronizationException {
        final String[] folders = getPathParts(remoteFolder.toString());
        return getId(folders);
    }

    public String getFileId(Path remoteFilePath) throws SynchronizationException {
        final String[] pathParts = getPathParts(remoteFilePath.toString());
        return getId(pathParts);
    }

    public String getId(final String... pathParts) throws SynchronizationException {
        String id = ROOT_DIRECTORY;
        for (String dirName : pathParts) {
            final BoxItem.Info existing = getExisting(dirName, id);
            if (existing == null) {
                throw new SynchronizationException("Box folder/file does not exist");
            }
            id = existing.getID();
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

    public String createFoldersPath(final Path folder) {
        return createFoldersPath(getPathParts(folder.toString()));
    }

    public String createFoldersPath(final String... directories) {
        final List<String> subDirs = new ArrayList<>();
        Collections.addAll(subDirs, directories);

        String folderId = ROOT_DIRECTORY;
        for (String dirName : directories) {
            BoxItem.Info existingFolder = getExisting(dirName, folderId);
            if (existingFolder == null) {
                existingFolder = new BoxFolder(client, folderId).createFolder(dirName);
            }
            subDirs.remove(0);
            folderId = existingFolder.getID();
        }
        return folderId;
    }

    private BoxItem.Info getExisting(final String searchedName, final String parentId) {
        final Iterable<BoxItem.Info> folderEntries = new BoxFolder(client, parentId).getChildren();
        for (final BoxItem.Info folderEntry : folderEntries) {
            if (folderEntry.getName().equals(searchedName)) {
                return folderEntry;
            }
        }
        return null;
    }
}
