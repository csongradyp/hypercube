package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.*;
import com.noe.hypercube.event.domain.request.*;
import com.noe.hypercube.event.domain.response.FileListResponse;
import com.noe.hypercube.event.domain.response.MappingResponse;
import com.noe.hypercube.event.domain.response.QueueContentResponse;
import com.noe.hypercube.event.domain.type.StreamDirection;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

public final class EventBus {

    private static final EventBus instance = new EventBus();

    private final MBassador<StorageEvent> storageEventBus;
    private final MBassador<FileEvent> fileEventBus;
    private final MBassador<StateChangeEvent> stateEventBus;
    private final MBassador<FileListRequest> fileListRequestBus;
    private final MBassador<FileListResponse> fileListResponseBus;
    private final MBassador<UploadRequest> uploadRequestBus;
    private final MBassador<DownloadRequest> downloadRequestBus;
    private final MBassador<CreateFolderRequest> createFolderRequestBus;
    private final MBassador<DeleteRequest> deleteRequestBus;
    private final MBassador<MappingRequest> mappingRequestBus;
    private final MBassador<MappingResponse> mappingResponseBus;
    private final MBassador<JumpToFileEvent> jumpToFileBus;
    private final MBassador<QueueContentEvent> queueContentEventBus;

    private EventBus() {
        storageEventBus = new MBassador<>(BusConfiguration.Default());
        fileEventBus = new MBassador<>(BusConfiguration.Default());
        stateEventBus = new MBassador<>(BusConfiguration.Default());
        fileListRequestBus = new MBassador<>(BusConfiguration.Default());
        fileListResponseBus = new MBassador<>(BusConfiguration.Default());
        uploadRequestBus = new MBassador<>(BusConfiguration.Default());
        downloadRequestBus = new MBassador<>(BusConfiguration.Default());
        createFolderRequestBus = new MBassador<>(BusConfiguration.Default());
        deleteRequestBus = new MBassador<>(BusConfiguration.Default());
        mappingRequestBus = new MBassador<>(BusConfiguration.Default());
        mappingResponseBus = new MBassador<>(BusConfiguration.Default());
        jumpToFileBus = new MBassador<>(BusConfiguration.Default());
        queueContentEventBus = new MBassador<>(BusConfiguration.Default());
        registerShutdownHook(storageEventBus, fileEventBus, stateEventBus, fileListRequestBus,
                fileListResponseBus, uploadRequestBus, downloadRequestBus, createFolderRequestBus,
                deleteRequestBus, mappingRequestBus, mappingResponseBus, queueContentEventBus, jumpToFileBus);
    }

    private void registerShutdownHook(final MBassador<?>... mBassadors) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (MBassador<?> eventBus : mBassadors) {
                eventBus.shutdown();
            }
        }));
    }

    public static void publishDownloadSubmit(final FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.DOWN);
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishDownloadStart(FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.DOWN);
        fileEvent.setStarted();
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishDownloadFinished(FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.DOWN);
        fileEvent.setFinished();
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishDownloadFailed(FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.DOWN);
        fileEvent.setFailed();
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishUploadSubmit(final FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.UP);
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishUploadStart(FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.UP);
        fileEvent.setStarted();
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishUploadFinished(FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.UP);
        fileEvent.setFinished();
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publishUploadFailed(FileEvent fileEvent) {
        fileEvent.setDirection(StreamDirection.UP);
        fileEvent.setFailed();
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publish(StorageEvent storageEvent) {
        instance.storageEventBus.publish(storageEvent);
    }

    public static void publish(StateChangeEvent stateChangeEvent) {
        instance.stateEventBus.publishAsync(stateChangeEvent);
    }

    public static void publish(FileListRequest fileListRequest) {
        instance.fileListRequestBus.publishAsync(fileListRequest);
    }

    public static void publish(FileListResponse fileListResponse) {
        instance.fileListResponseBus.publishAsync(fileListResponse);
    }

    public static void publish(UploadRequest uploadRequest) {
        instance.uploadRequestBus.publish(uploadRequest);
    }

    public static void publish(DownloadRequest downloadRequest) {
        instance.downloadRequestBus.publish(downloadRequest);
    }

    public static void publish(CreateFolderRequest createFolderRequest) {
        instance.createFolderRequestBus.publish(createFolderRequest);
    }

    public static void publish(DeleteRequest deleteRequest) {
        instance.deleteRequestBus.publish(deleteRequest);
    }

    public static void publish(MappingRequest mappingRequest) {
        instance.mappingRequestBus.publish(mappingRequest);
    }

    public static void subscribeToMappingRequest(final EventHandler<MappingRequest> handler) {
        instance.mappingRequestBus.subscribe(handler);
    }

    public static void publish(final MappingResponse mappingResponse) {
        instance.mappingResponseBus.publish(mappingResponse);
    }

    public static void subscribeToMappingResponse(final EventHandler<MappingResponse> handler) {
        instance.mappingResponseBus.subscribe(handler);
    }

    public static void publish(final QueueContentRequest queueContentRequest) {
        instance.queueContentEventBus.publishAsync(queueContentRequest);
    }

    public static void subscribeToQueueContentRequest(EventHandler<QueueContentEvent> handler) {
        instance.queueContentEventBus.subscribe(handler);
    }

    public static void publish(QueueContentResponse queueContentResponse) {
        instance.queueContentEventBus.publishAsync(queueContentResponse);
    }

    public static void subscribeToQueueContentResponse(EventHandler<? extends QueueContentEvent> handler) {
        instance.queueContentEventBus.subscribe(handler);
    }

    public static void subscribeToJumpToFileEvent(final EventHandler<JumpToFileEvent> handler) {
        instance.jumpToFileBus.subscribe(handler);
    }

    public static void publish(final JumpToFileEvent jumpToFileEvent) {
        instance.jumpToFileBus.publish(jumpToFileEvent);
    }

    public static void subscribeToDeleteRequest(FileEventHandler handler) {
        instance.deleteRequestBus.subscribe(handler);
    }

    public static void subscribeToUploadRequest(FileEventHandler handler) {
        instance.uploadRequestBus.subscribe(handler);
    }

    public static void subscribeToDownloadRequest(FileEventHandler handler) {
        instance.downloadRequestBus.subscribe(handler);
    }

    public static void subscribeToCreateFolderRequest(FileEventHandler handler) {
        instance.createFolderRequestBus.subscribe(handler);
    }

    public static void subscribeToFileListRequest(FileEventHandler handler) {
        instance.fileListRequestBus.subscribe(handler);
    }

    public static void subscribeToFileListResponse(EventHandler<FileListResponse> handler) {
        instance.fileListResponseBus.subscribe(handler);
    }

    public static void subscribeToStateEvent(EventHandler<StateChangeEvent> handler) {
        instance.stateEventBus.subscribe(handler);
    }

    public static void subscribeToFileEvent(EventHandler<FileEvent> handler) {
        instance.fileEventBus.subscribe(handler);
    }

    public static void subscribeToStorageEvent(EventHandler<StorageEvent> handler) {
        instance.storageEventBus.subscribe(handler);
    }

    public static void unsubscribeToFileEvent(EventHandler<FileEvent> handler) {
        instance.fileEventBus.unsubscribe(handler);
    }

    public static void unsubscribeToStorageEvent(EventHandler<StorageEvent> handler) {
        instance.storageEventBus.unsubscribe(handler);
    }

    public static void unsubscribeToFileListResponse(EventHandler<FileListResponse> handler) {
        instance.fileListResponseBus.unsubscribe(handler);
    }

    public static void unsubscribeToFileListRequest(FileEventHandler handler) {
        instance.fileListRequestBus.unsubscribe(handler);
    }

    public static void unsubscribeToUploadRequest(FileEventHandler handler) {
        instance.uploadRequestBus.unsubscribe(handler);
    }

    public static void unsubscribeToDownloadRequest(FileEventHandler handler) {
        instance.downloadRequestBus.unsubscribe(handler);
    }

    public static void unsubscribeToCreateFolderRequest(FileEventHandler handler) {
        instance.createFolderRequestBus.unsubscribe(handler);
    }

    public static void unsubscribeToDeleteRequest(FileEventHandler handler) {
        instance.deleteRequestBus.unsubscribe(handler);
    }
}
