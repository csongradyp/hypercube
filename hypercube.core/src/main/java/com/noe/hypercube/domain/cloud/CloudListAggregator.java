package com.noe.hypercube.domain.cloud;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.request.CloudFileListRequest;
import com.noe.hypercube.event.domain.request.FileListRequest;
import com.noe.hypercube.event.domain.response.CloudFileListResponse;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;

@Named
public class CloudListAggregator implements EventHandler<CloudFileListRequest> {

    @Inject
    private IAccountController accountController;

    public CloudListAggregator() {
        EventBus.subscribeToFileListRequest(this);
    }

    @Override
//    @Handler(filters = {@Filter(CloudRequestFilter.class)})
    @Handler(rejectSubtypes = true)
    public void onEvent(final CloudFileListRequest event) {
        if (event.isCloud()) {
            final Collection<String> processedAccounts = new ArrayList<>();
            List<ServerEntry> cloudList = new ArrayList<>();
            if (isRootListRequest(event)) {
                sendAllAccountFileLists(event, processedAccounts, cloudList);
            } else {
                sendRequestedFileLists(event, processedAccounts, cloudList);
            }
        }
    }

    private boolean isRootListRequest(final CloudFileListRequest event) {
        return event.getRequests() == null || event.getRequests().isEmpty();
    }

    private void sendAllAccountFileLists(final CloudFileListRequest event, final Collection<String> processedAccounts, final List<ServerEntry> cloudList) {
        final Collection<AccountBox> accountBoxes = accountController.getAllAttached();
        for (AccountBox accountBox : accountBoxes) {
            try {
                final List<ServerEntry> fileList = accountBox.getClient().getRootFileList();
                cloudList.addAll(fileList);
                processedAccounts.add(accountBox.getClient().getAccountName());
            } catch (SynchronizationException e) {
                e.printStackTrace();
            }
            EventBus.publish(new CloudFileListResponse(event.getTarget(), processedAccounts, event.getPreviousFolder(), Paths.get(""), cloudList, null));
        }
    }

    private void sendRequestedFileLists(final CloudFileListRequest event, final Collection<String> processedAccounts, final List<ServerEntry> cloudList) {
        final Collection<FileListRequest> requests = event.getRequests();
        for (FileListRequest request : requests) {
            final AccountBox accountBox = accountController.getAccountBox(request.getAccount());
            try {
                final List<ServerEntry> fileList = accountBox.getClient().getFileList(request.getFolder());
                cloudList.addAll(fileList);
                processedAccounts.add(accountBox.getClient().getAccountName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        EventBus.publish(new CloudFileListResponse(event.getTarget(), processedAccounts, event.getPreviousFolder(), event.getFolder(), cloudList, null));
    }

    public static final class CloudRequestFilter implements IMessageFilter<CloudFileListRequest> {

        @Override
        public boolean accepts(CloudFileListRequest message, MessageHandler metadata) {
            return message.isCloud();
        }
    }
}
