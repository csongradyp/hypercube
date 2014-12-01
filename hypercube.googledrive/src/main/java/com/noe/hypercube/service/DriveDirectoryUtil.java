package com.noe.hypercube.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

public class DriveDirectoryUtil {

    private static final Logger LOG = Logger.getLogger(DriveDirectoryUtil.class);
    public static final String ROOT_DIRECTORY = "root";
    private final Drive client;

    public DriveDirectoryUtil(Drive client) {
        this.client = client;
    }

    private void getPath(com.google.api.services.drive.model.File file, ArrayList<String> path) throws IOException {
//        System.out.println(path + " - " + file.getTitle() + " is current file with id=" + file.getId());
        List<ParentReference> parents = file.getParents();
//        for(int i=0;i<parents.size();i++){
        for (ParentReference parent : parents) {
//            File parent = Download.printFile(client, parents.get(i).getId());
            File parentFolder = client.files().get(parent.getId()).execute();
            if (!parentFolder.getTitle().equals("root")) {
                path.add(parentFolder.getTitle());
                getPath(parentFolder, path);
            }
        }
    }

    public String getPathString(File file) {
        String pathString = "";
        ArrayList<String> path = new ArrayList<>();
        try {
            getPath(file, path);
            for (int i = path.size() - 1; i >= 0; i--) {
                pathString += path.get(i) + "/";

            }
            return pathString;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public String getPath(File file) throws IOException {
        return getPathString(file);
    }

    private String getPath(File file, String path) throws IOException {
        // TODO get path for file
        LOG.debug(path + " - " + file.getTitle() + " is current file with id=" + file.getId());
        List<ParentReference> parents = file.getParents();

        ParentReference parent = parents.get(0);
        String mimeType = file.getMimeType();
        LOG.debug("parentId=" + parent.getId() + " mimeType=" + mimeType);
        String ID = parent.getId();
        if (mimeType.equals("application/vnd.google-apps.folder")) {
            ID = file.getId();
        }

        if (parent.getIsRoot()) {
            return "/" + file.getTitle();
        } else {
            Drive.Files.List request = client.files().list();
            String query = "mimeType='application/vnd.google-apps.folder' AND trashed=false AND '" + ID + "' in parents";
            LOG.debug("isFolderExists(): Query= " + query);
            request = request.setQ(query);
            FileList folderList = request.execute();
            File folder;
            if (folderList.getItems() == null || folderList.getItems().isEmpty()) {
                return "/" + file.getTitle();
            } else {
                folder = folderList.getItems().get(0);
            }
            return getPath(folder, "/" + folder.getTitle() + path);
        }
    }

    /**
     * @param title    the title (name) of the folder (the one you search for)
     * @param parentId the parent Id of this folder (use root) if the folder is in the main directory of google drive
     * @return google drive file object
     * @throws java.io.IOException
     */
    private File getExistsFolder(String title, String parentId) throws IOException {
        Drive.Files.List request = client.files().list();
        String query = "mimeType='application/vnd.google-apps.folder' AND trashed=false AND title='" + title + "' AND '" + parentId + "' in parents";
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
        body.setMimeType("application/vnd.google-apps.folder");
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
