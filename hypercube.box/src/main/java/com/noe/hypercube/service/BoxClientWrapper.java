package com.noe.hypercube.service;


import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.noe.hypercube.domain.AccountQuota;
import com.noe.hypercube.domain.BoxFileEntity;
import com.noe.hypercube.domain.BoxServerEntry;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BoxClientWrapper extends Client<Box, BoxFileEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(BoxClientWrapper.class);

    private final BoxClient client;
    private String cursor;

    public BoxClientWrapper(final BoxClient client) {
        this.client = client;
        cursor = null;
    }

    @Override
    protected boolean testConnectionActive() {
        try {
            return client.getAuthData() != null;
        } catch (AuthFatalFailureException e) {
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
        return exist("");
    }

    @Override
    public boolean exist(ServerEntry serverEntry) {
        return exist(serverEntry);
    }

    private boolean exist(String boxFilePath) {
        boolean exists = false;

        return exists;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> serverEntries = new LinkedList<>();

        return serverEntries;
    }

    @Override
    public void download(ServerEntry serverEntry, FileOutputStream outputStream) throws SynchronizationException {

    }

    @Override
    public ServerEntry download(String serverPath, FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException {
        return new BoxServerEntry("", false);
    }

    @Override
    public void delete(final Path remoteFilePath) throws SynchronizationException {

    }

    @Override
    public void delete(String remoteFileId) throws SynchronizationException {
        throw new UnsupportedOperationException("box does not populate file id");
    }

    @Override
    public ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return new BoxServerEntry("", false);
    }

    @Override
    public ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return new BoxServerEntry("", false);
    }

    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();

        return fileList;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();

        return fileList;
    }

    @Override
    public void createFolder(final Path folder) throws SynchronizationException {

    }

    @Override
    public AccountQuota getQuota() throws SynchronizationException {
        return new AccountQuota(0L, 0L);
    }
}
