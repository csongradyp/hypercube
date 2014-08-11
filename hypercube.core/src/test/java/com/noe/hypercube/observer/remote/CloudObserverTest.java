package com.noe.hypercube.observer.remote;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.service.TestAccount;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CloudObserverTest {

    @Mock
    private IPersistenceController mockPersistence;
    @Mock
    private IClient<TestAccount, TestEntity> mockClient;
    @Mock
    private IMapper<TestAccount, TestMapping> mockMapper;
    @Mock
    private IDownloader mockDownloader;
    @Mock
    private FileEntityFactory<TestAccount, TestEntity> mockFactory;
    @Mock
    private AccountBox<TestAccount, TestEntity, TestMapping> mockAccountBox;

    private CloudObserver<TestAccount, TestEntity> underTest;

    private final ServerEntry relevantFile = new TestServerEntry("X/Y/relevantFile.txt", "dummyRevision");
    private final ServerEntry notRelevantSubfolderFile = new TestServerEntry("X/Z/willNotDownloaded.txt", "dummyRevision2");
    private final ServerEntry notRelevantParentFolderFile = new TestServerEntry("X/willNotDownloaded2.txt", "dummyRevision3");

    @Before
    public void setUp() {
        given(mockAccountBox.getClient()).willReturn(mockClient);
        given(mockAccountBox.getDownloader()).willReturn(mockDownloader);
        given(mockAccountBox.getEntityFactory()).willReturn(mockFactory);
        given(mockAccountBox.getMapper()).willReturn(mockMapper);
        given(mockAccountBox.getAccountType()).willReturn(TestAccount.class);
        given(mockMapper.getMappingClass()).willReturn(TestMapping.class);
        underTest = new CloudObserver<>(mockAccountBox, mockPersistence);
    }

    @Test
    public void downloadsFileWhenChangedFileOnServerIsInWatchedDirectory() throws SynchronizationException {
        final Collection<ServerEntry> changes = givenChangesOnServer();
        given(mockClient.getChanges()).willReturn(changes);
        given(mockPersistence.getMappings(any())).willReturn(givenMappings());

        underTest.run();

        verify(mockClient, times(1)).getChanges();
        verify(mockDownloader, times(1)).download(relevantFile);
        verify(mockDownloader, never()).download(notRelevantParentFolderFile);
        verify(mockDownloader, never()).download(notRelevantSubfolderFile);
    }

    private Collection<ServerEntry> givenChangesOnServer() {
        final Collection<ServerEntry> changes = new ArrayList<>();
        changes.add(relevantFile);
        changes.add(notRelevantParentFolderFile);
        changes.add(notRelevantSubfolderFile);
        return changes;
    }

    private Collection<MappingEntity> givenMappings() {
        final Collection<MappingEntity> testMappings = new ArrayList<>();
        testMappings.add(new TestMapping("A/B/", "X/Y"));
        return testMappings;
    }
}
