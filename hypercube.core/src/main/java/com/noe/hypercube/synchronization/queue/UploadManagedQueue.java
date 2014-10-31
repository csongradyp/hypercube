package com.noe.hypercube.synchronization.queue;

import com.noe.hypercube.domain.UploadEntity;
import java.io.File;

public class UploadManagedQueue extends ManagedQueue<UploadEntity, File> {

    @Override
    protected boolean exists(File file) {
        return file.exists();
    }
}
