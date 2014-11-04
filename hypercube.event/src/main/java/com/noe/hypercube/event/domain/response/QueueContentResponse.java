package com.noe.hypercube.event.domain.response;

import com.noe.hypercube.domain.IStreamEntry;
import com.noe.hypercube.event.domain.QueueContentEvent;
import com.noe.hypercube.event.domain.type.QueueType;

import java.util.Collection;

public class QueueContentResponse extends QueueContentEvent {

    private final Collection<IStreamEntry> downloads;
    private final Collection<IStreamEntry> uploads;

    public QueueContentResponse(final QueueType type, final String account, final Collection<IStreamEntry> downloads, final Collection<IStreamEntry> uploads) {
        super(type, account);
        this.downloads = downloads;
        this.uploads = uploads;
    }

    public Collection<IStreamEntry> getDownloads() {
        return downloads;
    }

    public Collection<IStreamEntry> getUploads() {
        return uploads;
    }
}
