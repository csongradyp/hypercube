package com.noe.hypercube.synchronization.downstream;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxDelta;
import com.dropbox.core.DbxException;
import com.noe.hypercube.cache.DirectoryBindCache;
import com.noe.hypercube.converter.DropboxPathConverter;
import com.noe.hypercube.converter.PathConverter;
import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.FileEntity;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import static java.lang.String.format;

public class DbxSyncTask implements DownstreamSynchronizer {

    private static final Logger LOG = Logger.getLogger(DbxSyncTask.class);

    private final DbxClient client;
    private final Dao<String, DbxFileEntity> dao;
    private final PathConverter pathConverter;
    private String lastCursor;

    public DbxSyncTask(DbxClient client, Dao<String, DbxFileEntity> dao, DropboxPathConverter pathConverter) {
        this.client = client;
        this.dao = dao;
        this.pathConverter = pathConverter;
    }

    @Override
    public void run() {
        try {
            DbxDelta<com.dropbox.core.DbxEntry> delta = client.getDelta(lastCursor);
            lastCursor = delta.cursor;
            if (delta.entries != null && !delta.entries.isEmpty()) {
                LOG.debug(format("Detected %d changes to process ...", delta.entries.size()));
                synchronizeFilesFromServer(delta);
            }
        } catch (DbxException e) {
            LOG.error("Error occurred while synchronize with Dropbox", e);
        }
    }

    public void synchronizeFilesFromServer(DbxDelta<com.dropbox.core.DbxEntry> delta) {
        try {
            do {
                Synchronize(delta);
                delta = client.getDelta(lastCursor);
            } while (delta.hasMore);
        } catch (DbxException e) {
            LOG.error("Error occured while syncronize with Dropbox", e);
        }
    }

    private void Synchronize(DbxDelta<com.dropbox.core.DbxEntry> delta) throws DbxException {
        for (DbxDelta.Entry<com.dropbox.core.DbxEntry> entry : delta.entries) {
            if(isMappedDirectoryContent(entry)) {
                LOG.debug("Mapped directory found");
                if (entry.metadata != null) {
                    if (!isUntouched(entry)) {
                        try {
                            downloadFile(client, entry);
                        } catch (IOException e) {
                            LOG.error("Error occured while downloading file from Dropbox", e);
                        }
                    }
                }
                else {
                    deleteLocalFile(pathConverter.convertToLocalPath(entry.lcPath));
                }
            }
            else {
                LOG.debug("File not Mapped - will not process " + entry.lcPath);
            }
        }
    }

    private boolean isMappedDirectoryContent(DbxDelta.Entry<com.dropbox.core.DbxEntry> entry) {
        Collection<String> dbxDirs = DirectoryBindCache.getInstance().getRemoteDirs().values();
        for (String dbxDir : dbxDirs) {
            if(entry.lcPath.contains(dbxDir.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isUntouched(DbxDelta.Entry<com.dropbox.core.DbxEntry> entry){
        String localPath = pathConverter.convertToLocalPath(entry.lcPath);
        FileEntity dbEntry = dao.findById(localPath);
        return dbEntry != null &&  isSameRevision(entry, dbEntry);
    }

    private boolean isSameRevision(DbxDelta.Entry<com.dropbox.core.DbxEntry> entry, FileEntity dbEntry) {
        return dbEntry.getRevision().equals(entry.metadata.asFile().rev);
    }

    private void downloadFile(DbxClient client, DbxDelta.Entry<com.dropbox.core.DbxEntry> entry) throws DbxException, IOException {
        if (entry.metadata.isFile()) {
            String localPath = pathConverter.convertToLocalPath(entry.metadata.path);
            File newLocalFile = new File(localPath);
            if (entry.metadata.isFolder()) {
                boolean success = newLocalFile.mkdirs();
                if (!success) {
                    LOG.debug(format("Didn't create any intermediary directories for %s", localPath));
                }
            } else {
                // if the parent directory doesn't exist create all intermediary directories ...
                if (!newLocalFile.getParentFile().exists()) {
                    newLocalFile.getParentFile().mkdirs();
                }

                try (FileOutputStream os = new FileOutputStream(newLocalFile)) {
                    client.getFile(entry.metadata.path, null, os);
                    saveToDb(entry, localPath);
                    LOG.debug(format("Successfully downloaded file %s", localPath));
                } catch (FileNotFoundException e) {
                    throw new DbxException("Couldn't write file '" + localPath + "'", e);
                }

                long lastModified = entry.metadata.asFile().lastModified.getTime();
                boolean success = newLocalFile.setLastModified(lastModified);
                if (!success) {
                    LOG.debug(format("Couldn't change attribute 'lastModified' of file %s", localPath));
                }
            }
        }
    }

    private void saveToDb(DbxDelta.Entry<com.dropbox.core.DbxEntry> entry, String localPath) {
        DbxFileEntity dbxFileEntry = new DbxFileEntity(localPath, entry.metadata.asFile().rev, entry.metadata.asFile().lastModified);
        dao.persist(dbxFileEntry);
//        DbCache.getInstance().add(dbxFileEntry, Action.ADDED);
    }

    private void deleteLocalFile(String localPath) {
        File fileToDelete = new File(localPath);
        if (!fileToDelete.isDirectory()) {
            try {
                DbxFileEntity dbEntry = dao.findById(localPath);
//                DbCache.getInstance().add(dbEntry, Action.REMOVED);
                dao.remove(localPath);
                FileUtils.forceDelete(fileToDelete);
                LOG.debug(format("Successfully deleted local file %s", localPath));
            } catch (IOException e) {
                LOG.error(format("Local file %s couldn't be deleted", localPath) ,e);
            }
        } else {
            LOG.error("Local file isn't deleted because it is a directory");
        }
    }

}
