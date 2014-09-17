package com.noe.hypercube.service;

import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxItem;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxFolderRequestObject;
import com.box.restclientv2.exceptions.BoxRestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoxDirectoryUtil {

    private static final String ROOT_DIRECTORY = "0";

    private final BoxClient client;

    public BoxDirectoryUtil(BoxClient client) {
        this.client = client;
    }

    public String createFoldersPath(String... directories) throws IOException {
        List<String> subDirs = new ArrayList<>();
        Collections.addAll(subDirs, directories);

        String parentId = ROOT_DIRECTORY;
        for (String dirName : directories) {
            BoxTypedObject existingFolder = getExistsFolder(dirName, parentId);
            if (existingFolder == null) {
                existingFolder = createFolder(dirName, parentId);
            }
            subDirs.remove(0);
            parentId = existingFolder.getId();
        }
        return parentId;
    }

    private BoxTypedObject createFolder(final String dirName, final String parentId) {
        try {
            final BoxFolderRequestObject folderRequestObject = BoxFolderRequestObject.createFolderRequestObject(dirName, parentId);
            return client.getFoldersManager().createFolder(folderRequestObject);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BoxTypedObject getExistsFolder(String dirName, String parentId) {
            try {
                final BoxFolder boxFolder = client.getFoldersManager().getFolder(parentId, null);
                List<BoxTypedObject> folderEntries = boxFolder.getItemCollection().getEntries();
                for (BoxTypedObject folderEntry : folderEntries) {
                    if (folderEntry instanceof BoxItem) {
                        if(((BoxItem) folderEntry).getName().equals(dirName)){
                            return folderEntry;
                        }
                    }
                }
            } catch (BoxRestException e) {
                e.printStackTrace();
            } catch (BoxServerException e) {
                e.printStackTrace();
            } catch (AuthFatalFailureException e) {
                e.printStackTrace();
            }
        return null;
    }
}
