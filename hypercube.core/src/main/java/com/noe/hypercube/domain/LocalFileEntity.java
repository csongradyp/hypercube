package com.noe.hypercube.domain;

import org.apache.commons.io.FileUtils;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class LocalFileEntity implements IEntity<String> {

    @Id
    private String localPath;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;
    private Long crc;

    public LocalFileEntity(final File file) {
        localPath = file.toPath().toString();
        lastModifiedDate = new Date(file.lastModified());
        getCrc(file);
    }

    private void getCrc(File file) {
        try {
            crc = FileUtils.checksumCRC32(file);
        } catch (IOException e) {
            crc = -1L;
        }
    }

    @Override
    public String getId() {
        return localPath;
    }

    public Long getCrc() {
        return crc;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getLocalPath() {
        return localPath;
    }
}
