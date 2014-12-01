package com.noe.hypercube.domain.cloud;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.request.FileListRequest;
import com.noe.hypercube.event.domain.response.FileListResponse;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import net.engio.mbassy.listener.Handler;

@Named
public class CloudListAggregator implements EventHandler<FileListRequest> {

    @Inject
    private IAccountController accountController;

    public CloudListAggregator() {
        EventBus.subscribeToFileListRequest(this);
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileListRequest event) {
        if (event.isCloud()) {
            List<ServerEntry> cloudList = new ArrayList<>();
            final Collection<AccountBox> accountBoxes = accountController.getAll();
            for (AccountBox accountBox : accountBoxes) {
                try {
                    final List<ServerEntry> fileList = accountBox.getClient().getRootFileList();
                    cloudList.addAll(fileList);
                } catch (SynchronizationException e) {
                    e.printStackTrace();
                }
            }
            EventBus.publish(new FileListResponse(event.getTarget(), Cloud.getName(), event.getPreviousFolder(), Paths.get(""), cloudList, null));
        }
    }
}
