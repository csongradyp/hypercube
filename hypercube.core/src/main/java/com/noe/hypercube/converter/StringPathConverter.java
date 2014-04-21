package com.noe.hypercube.converter;

public interface StringPathConverter {

    String convertToRemotePath(String localPath);
    String convertToLocalPath(String remotePath);

}
