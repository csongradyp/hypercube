package com.noe.hypercube.converter;


import com.noe.hypercube.cache.DirectoryBindCache;
import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.domain.DirectoryBind;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DropboxPathConverter implements PathConverter {

//    public static final String FILENAME_REGEX = "([a-zA-Z]:\\\\)?[^\\x00-\\x1F\"<>\\|:\\*\\?/]+\\.[a-zA-Z]{3,4}$";
    public static final String FILENAME_REGEX = "[ \\t()\\w,\\s-]+\\.[A-Za-z]{3,4}$";
    private String localBaseDir;
    private Dao<String, DirectoryBind> bindDao;

    public DropboxPathConverter(String localDir) {
        this.localBaseDir = localDir;
    }

    @Override
    public String convertToRemotePath(File file) throws IOException {
        String fileCanonicalPath = file.getCanonicalPath();
        return convertToRemotePath(fileCanonicalPath);
    }

    @Override
    public String convertToRemotePath(String fileCanonicalPath) {
//        String localDir = fileCanonicalPath.replaceAll("\\\\"+ FILENAME_REGEX, "");
//        String filename = fileCanonicalPath.replace(localDir, "");
//        String localSubDir = localDir.replace(localBaseDir, "");
//        localSubDir = localSubDir.replace("\\", "/");
//        String dropboxDir = getDropboxDir(localSubDir);
//        if(dropboxDir.isEmpty()) {
//            return null;
//        }
//        String dropboxPath = dropboxDir + filename;
//        return dropboxPath.replace("\\", "/");
        return "";
    }

//    private String getDropboxDir(String localSubDir) {
//        String dbxDir = bindDao.get(localSubDir).getRemoteDir();
//        if(dbxDir == null) {
//            dbxDir = "";
//        }
//        return  dbxDir;
//    }

    @Override
    public String convertToLocalPath(String dropboxPath) {
        String dropboxDir = dropboxPath.replaceAll("/"+ FILENAME_REGEX, "");
        String filename = dropboxPath.replaceAll(dropboxDir, "");
        String localSubDir = DirectoryBindCache.getInstance().getLocalDirs().get(dropboxDir); //getLocalSubDir(dropboxDir);
        String localPath = localBaseDir + localSubDir + filename;
        localPath = localPath.replace("/", "\\");
        return localPath;
    }

    private String getLocalSubDir(String dropboxDir) {
        Map<String,String> dbxLocalDirs = DirectoryBindCache.getInstance().getLocalDirs();
        for (Map.Entry<String, String> entry : dbxLocalDirs.entrySet()) {
            if(dropboxDir.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("Relevant Dropbox folder is not mapped!");
    }

    @Override
    public String convertToLocalPath(File file) throws IOException {
        String filePath = file.getCanonicalPath();
        return convertToLocalPath(filePath);
    }
}
