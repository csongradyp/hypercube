package com.noe.hypercube.converter;


import com.noe.hypercube.domain.TestMapping;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.noe.hypercube.converter.DirectoryConverter.convertToLocalPath;
import static com.noe.hypercube.converter.DirectoryConverter.convertToRemotePath;

public class DirectoryConverterTest {

    private Path localDir;
    private Path remoteDir;
    private Path localDirWithSubDirectories;
    private Path remoteDirWithSubDirectories;
    private TestMapping mapping;

    @Before
    public void setUp() {
        localDir = Paths.get("c:/a/b");
        localDirWithSubDirectories = Paths.get("c:/a/b/c/d");
        remoteDir = Paths.get("x/y/z");
        remoteDirWithSubDirectories = Paths.get("x/y/z/c/d");
        mapping = new TestMapping(localDir.toString(), remoteDir.toString());
    }

    @Test
    public void returnsMappedRemoteDirWhenGivenLocalDirIsEqualToTheMappedOne() {
        Path resultDir = convertToRemotePath(localDir, mapping);
        Assert.assertEquals(remoteDir, resultDir);
    }

    @Test
    public void returnsRemoteDirWithExtraSubDirsWhenGivenLocalDirHasMoreSubdirsThanTheMappedLocalDir() {
        Path resultDir = convertToRemotePath(localDirWithSubDirectories, mapping);
        Assert.assertEquals(remoteDirWithSubDirectories, resultDir);
    }

    @Test
    public void returnsMappedLocalDirWhenGivenRemoteDirIsEqualToTheMappedOne() {
        Path resultDir = convertToLocalPath(remoteDir, mapping);
        Assert.assertEquals(localDir, resultDir);
    }

    @Test
    public void returnsLocalDirWithExtraSubDirsWhenGivenRemoteDirHasMoreSubdirsThanTheMappedRemoteDir() {
        Path resultDir = convertToLocalPath(remoteDirWithSubDirectories, mapping);
        Assert.assertEquals(localDirWithSubDirectories, resultDir);
    }
}
