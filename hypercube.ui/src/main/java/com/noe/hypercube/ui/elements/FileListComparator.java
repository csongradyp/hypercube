package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.domain.file.IFile;

import java.text.Collator;
import java.util.Comparator;

public class FileListComparator implements Comparator<IFile> {

    @Override
    public int compare(IFile first, IFile second) {
        final boolean secondIsDirectory = second.isDirectory();
        final boolean firstIsDirectory = first.isDirectory();
        if(first.isStepBack() || second.isStepBack()) {
            return Integer.MIN_VALUE;
        }
        if(!isSameFileType(secondIsDirectory, firstIsDirectory)) {
            if(firstIsDirectory) {
                return -1;
            } else {
                return 1;
            }
        }
        return Collator.getInstance().compare(first.getName(), second.getName());
    }

    private boolean isSameFileType(boolean secondIsFolder, boolean firstIsFolder) {
        return isFolder(secondIsFolder, firstIsFolder) || isFile(secondIsFolder, firstIsFolder);
    }

    private boolean isFile(final boolean secondIsFolder, final boolean firstIsFolder) {
        return !firstIsFolder && !secondIsFolder;
    }

    private boolean isFolder(final boolean secondIsDirectory, final boolean firstIsDirectory) {
        return firstIsDirectory && secondIsDirectory;
    }
}
