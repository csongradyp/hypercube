package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.LocalFileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import com.noe.hypercube.service.TestAccount;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.presynchronization.util.PreSynchronizationSubmitManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FolderPreSynchronizerTest {

    private static final Path REMOTE_FOLDER = Paths.get("remote");
    private static final Path TARGET_FOLDER = Paths.get("d:\\test");
    @Mock
    private IPersistenceController mockPersistenceController;
    @Mock
    private IAccountController mockAccountController;
    @Mock
    private PreSynchronizationSubmitManager mockSubmitManager;

    private AccountBox<TestAccount, Object, TestEntity, TestMapping> accountBox;
    private FolderPreSynchronizer underTest;

    @Before
    public void setUp() throws Exception {
        accountBox = mock(AccountBox.class, RETURNS_DEEP_STUBS);
        underTest = new FolderPreSynchronizer(TARGET_FOLDER, mockPersistenceController, mockAccountController, mockSubmitManager);
        given(mockPersistenceController.getLocalFileEntity(any())).willReturn(new LocalFileEntity(Paths.get("c:\\Temp\\loc1.txt").toFile()));
    }

    @Test
    public void testRun() throws Exception {
        givenAccountBoxes();
        givenMappings();
        givenMappedFiles();
        given(accountBox.getClient().getAccountName()).willReturn("test");
        given(accountBox.getAccountType()).willReturn(TestAccount.class);
        given(accountBox.getClient().getMappingType()).willReturn(TestMapping.class);
        final ArrayList<Path> remoteFolders = new ArrayList<>();
        remoteFolders.add(REMOTE_FOLDER);
        given(mockPersistenceController.getRemoteFolder(TestMapping.class, TARGET_FOLDER)).willReturn(remoteFolders);
        givenLocalFiles();
        givenRemoteFileList();

        underTest.call();
    }

    private void givenMappedFile(final Map<String, List<FileEntity>> mappedFiles, final String localPath, final String remotePath, final Boolean changed) {
        final List<FileEntity> fileEntities = new ArrayList<>();
        fileEntities.add(new TestEntity(localPath, remotePath, new Date(), changed ? "changed" : "1"));
        mappedFiles.put(localPath, fileEntities);
    }

    private void givenMappedFiles() {
        final Map<String, List<FileEntity>> mappedFiles = new HashMap<>();
        givenMappedFile(mappedFiles, TARGET_FOLDER + "\\loc1.txt", "remote/r1.txt", false );
        givenMappedFile(mappedFiles, TARGET_FOLDER + "\\loc2.txt", "remote/r2.txt", true );
        given(mockPersistenceController.getMappedEntities(TARGET_FOLDER.toString())).willReturn(mappedFiles);
    }

    private void givenLocalFiles() {
        final List<File> localFiles = new ArrayList<>();
        localFiles.add(new File("loc1.txt"));
        localFiles.add(new File("loc2.txt"));
        localFiles.add(new File("loc3.txt"));
        localFiles.add(new File("loc4.txt"));
//        PowerMockito.when(FileUtils.listFiles(any(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)).thenReturn(localFiles);
//        PowerMockito.when(FileUtils.listFiles(targetFolder.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)).thenReturn(localFiles);
    }

    private void givenRemoteFileList() throws SynchronizationException {
        final List<ServerEntry> remoteFileList = new ArrayList<>();
        remoteFileList.add(new TestServerEntry("remote/r1.txt", "1", new Date(), false));
        remoteFileList.add(new TestServerEntry("remote/r2.txt", "1", new Date(), false));
        remoteFileList.add(new TestServerEntry("remote/r3.txt", "1", new Date(), false));
        remoteFileList.add(new TestServerEntry("remote/r4.txt", "1", new Date(), false));
        given(accountBox.getClient().getFileList(REMOTE_FOLDER)).willReturn(remoteFileList);
    }

    private void givenMappings() {
        final List<MappingEntity> mappingEntities = new ArrayList<>();
        mappingEntities.add(new TestMapping("local", "remote"));
        given(mockPersistenceController.getMappings(anyString())).willReturn(mappingEntities);
        given(mockAccountController.getAccountBox(TestAccount.class)).willReturn(accountBox);
    }

    private void givenAccountBoxes() {
        final List<AccountBox> accountBoxes = new ArrayList<>();
//        final AccountBox<TestAccount, TestEntity, TestMapping> accountBox = new AccountBox<>(null, null, null, mockPersistenceController);
        accountBoxes.add(accountBox);
        given(mockAccountController.getAll()).willReturn(accountBoxes);
    }
}
