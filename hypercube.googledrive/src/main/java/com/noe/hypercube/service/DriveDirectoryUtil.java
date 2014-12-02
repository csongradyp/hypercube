package com.noe.hypercube.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.apache.log4j.Logger;

public class DriveDirectoryUtil {

    private static final Logger LOG = Logger.getLogger(DriveDirectoryUtil.class);
    public static final String ROOT_DIRECTORY = "root";
    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private final Drive client;

    public DriveDirectoryUtil(final Drive client) {
        this.client = client;
    }

    public boolean isFolder(final File file) {
        return file.getMimeType().contains(FOLDER_MIME_TYPE);
    }

    public String getId(final Path remotePath) throws SynchronizationException {
        final String[] pathParts = getPathParts(remotePath.toString());
        return getId(pathParts);
    }

    private String[] getPathParts(final String remotePath) {
        String path = remotePath;
        if (path.startsWith("\\") || path.startsWith("/")) {
            path = path.substring(1);
        }
        return path.replace("\\", "/").split("/");
    }

    public String getId(final String... pathParts) throws SynchronizationException {
        String id = ROOT_DIRECTORY;
        for (String folderName : pathParts) {
            final File folder = getExistingFolder(folderName, id);
            id = folder.getId();
        }
        return id;
    }

    private File getExistingFolder(final String folderName, final String id) throws SynchronizationException {
        try {
//            final FileList fileList = client.files().list().setQ(String.format("title = '%s' AND mimeType = '%s' AND %s in parents AND trashed = false", folderName, FOLDER_MIME_TYPE, id)).execute();
            final String query = String.format("title = '%s' AND mimeType = '%s' AND trashed = false", folderName, FOLDER_MIME_TYPE);
            final FileList fileList = client.files().list().setQ(query).execute();
            final List<File> items = fileList.getItems();
            final Optional<File> folder = items.parallelStream().filter(file -> id.equals(ROOT_DIRECTORY) ? file.getParents().get(0).getIsRoot() : id.equals(file.getParents().get(0).getId())).findAny();
            if (folder.isPresent()) {
                return folder.get();
            }
        } catch (IOException e) {
            throw new SynchronizationException(String.format("Google Drive: folder/file does not exist %s", folderName), e);
        }
        throw new SynchronizationException(String.format("Google Drive: folder/file does not exist %s", folderName));
    }

    public String getPath(final File file) {
        String path = "";
        ParentReference parentReference = file.getParents().get(0);
        while (!parentReference.getIsRoot()) {
            final File folder;
            try {
                folder = client.files().get(parentReference.getId()).execute();
                parentReference = folder.getParents().get(0);
                path = folder.getTitle() +  "/" + path;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "/" + path + file.getTitle();
    }


    /**
     * @param title    the title (name) of the folder (the one you search for)
     * @param parentId the parent Id of this folder (use root) if the folder is in the main directory of google drive
     * @return google drive file object
     * @throws java.io.IOException
     */
    private File getExistsFolder(String title, String parentId) throws IOException {
        Drive.Files.List request = client.files().list();
        String query = String.format("mimeType = '%s' AND trashed = false AND title = '%s' AND '%s' in parents", FOLDER_MIME_TYPE, title, parentId);
        LOG.debug("isFolderExists(): Query= " + query);
        request = request.setQ(query);
        FileList files = request.execute();
        LOG.debug("isFolderExists(): List Size =" + files.getItems().size());

        File folder = null;
        if (!files.getItems().isEmpty()) //if the size is zero, then the folder doesn't exist
        {
            //since google drive allows to have multiple folders with the same title (name) we select the first file in the list to return
            folder = files.getItems().get(0);
        }
        return folder;
    }

    /**
     * @param title               the folder's title
     * @param listParentReference the list of parents references where you want the folder to be created,
     *                            if you have more than one parent references, then a folder will be created in each one of them
     * @return google drive file object
     * @throws java.io.IOException
     */
    private File createFolder(String title, List<ParentReference> listParentReference) throws IOException {
        File body = new File();
        body.setTitle(title);
        body.setParents(listParentReference);
        body.setMimeType(FOLDER_MIME_TYPE);
        return client.files().insert(body).execute();
    }

    /**
     * @param directories list of folders directories
     *                    i.e. if your path like this folder1/folder2/folder3 then pass them in this order createFoldersPath(service, folder1, folder2, folder3)
     * @return parent reference of the last added folder in case you want to use it to create a file inside this folder.
     * @throws java.io.IOException
     */
    public List<ParentReference> createFoldersPath(String... directories) throws IOException {
        return createFoldersPath(Arrays.asList(directories));
    }

    public List<ParentReference> createFoldersPath(Iterable<String> directories) throws IOException {
        List<ParentReference> listParentReference = new ArrayList<>();
        File folder = null;
        for (String dirName : directories) {
            String parentId = getParentId(folder);
            folder = getExistsFolder(dirName, parentId);
            if (!isExistingFolder(folder)) {
                folder = createFolder(dirName, listParentReference);
            }
            listParentReference.clear();
            listParentReference.add(new ParentReference().setId(folder.getId()));
        }
        return listParentReference;
    }

    private boolean isExistingFolder(File file) {
        return file != null;
    }

    private String getParentId(File file) {
        String parentId = ROOT_DIRECTORY;
        if (file != null) {
            parentId = file.getId();
        }
        return parentId;
    }
}
