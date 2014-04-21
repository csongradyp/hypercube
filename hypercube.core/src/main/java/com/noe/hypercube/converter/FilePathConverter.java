package com.noe.hypercube.converter;


import java.io.File;
import java.io.IOException;

public interface FilePathConverter {

    String convertToLocalPath(File file) throws IOException;
    String convertToRemotePath(File file) throws IOException;

}
