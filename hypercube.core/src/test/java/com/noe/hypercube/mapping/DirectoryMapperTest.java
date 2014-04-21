package com.noe.hypercube.mapping;

import com.noe.hypercube.controller.MappingController;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.domain.TestMapping;
import com.noe.hypercube.mapping.collector.LocalDirectoryCollector;
import com.noe.hypercube.mapping.collector.RemoteDirectoryCollector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryMapperTest {

    private static final Path[] LOCAL_DIRS = new Path[]{ Paths.get("X:\\A\\B"), Paths.get("X:\\A"), Paths.get("Z:\\Y") };
    private static final Path[] EXPECTED_LOCAL_DIRS = new Path[]{ Paths.get("X:\\A\\B\\z")};
    private static final Path[] REMOTE_DIRS = new Path[]{ Paths.get("/x/y"), Paths.get("/g/h/i"), Paths.get("/a/b/c") };
    private static final Path[] EXPECTED_REMOTE_DIRS = new Path[]{ Paths.get("/x/y/C"), Paths.get("/g/h/i/B/C") };
    @Mock
    private MappingController mockMappingController;
    private List<MappingEntity> mappingEntities;

    private DirectoryMapper mapper;

    @Before
    public void setUp() {
        mapper = new TestMapper();
        mapper.setMappingController(mockMappingController);
        mapper.setRemoteDirectoryCollector(new RemoteDirectoryCollector());
        mapper.setLocalDirectoryCollector(new LocalDirectoryCollector());

        mappingEntities = createMappingEntity();
        when(mockMappingController.getMappings(TestMapping.class)).thenReturn(mappingEntities);
    }

    @Test
    public void getRemotesReturnsAllMappingsRelatedToTheGivenLocalFilePathAndSubpaths() {
        final String localPath = "X:\\A\\B\\C\\test.txt";

        final List list = mapper.getRemotes(localPath);

        Assert.assertFalse(list.isEmpty());
        Assert.assertArrayEquals(EXPECTED_REMOTE_DIRS, list.toArray());
    }

    @Test
    public void getLocalsReturnsAllMappingsRelatedToTheGivenRemoteFilePathAndSubpaths() {
        final String remotePath = "/x/y/z/remoteFile.txt";

        final List list = mapper.getLocals(remotePath);

        Assert.assertFalse(list.isEmpty());
        Assert.assertArrayEquals(EXPECTED_LOCAL_DIRS, list.toArray());
    }

    private List<MappingEntity> createMappingEntity() {
        ArrayList<MappingEntity> mappingEntities = new ArrayList<>();
        for(int i = 0; i < LOCAL_DIRS.length; i++) {
            mappingEntities.add(new TestMapping(LOCAL_DIRS[i], REMOTE_DIRS[i]));
        }
        return mappingEntities;
    }

}
