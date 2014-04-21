package com.noe.hypercube.ui.util;


import javax.swing.*;
import java.io.IOException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

public final class FileManagerUtil {

    public static final String OS_NAME_PROPERTY_KEY = "os.name";
    public static final String WINDOWS = "Windows";
    public static final String WINDOWS_COMMAND = "explorer.exe /select,";
    public static final String LINUX = "Linux";
    public static final String LINUX_COMMAND = "xdg-open ";
    public static final String MAC_COMMAND = "usr/bin/open ";
    public static final String MAC = "Mac";
    public static final String ERROR_TITLE = "ERROR";
    public static final String CANNOT_OPEN_ERROR_MESSAGE = "Cannot open File Manager";

    private FileManagerUtil() {
    }

    public static void openFileManager(String path) {
        String os = System.getProperty(OS_NAME_PROPERTY_KEY);
        if (os.contains(WINDOWS)) {
            executeFileManager(WINDOWS_COMMAND + path);
        } else if (os.contains(LINUX)) {
            executeFileManager(LINUX_COMMAND + path);
        } else if ( os.contains(MAC)){
            executeFileManager(MAC_COMMAND + path);
        }
    }

    private static void executeFileManager(String command) {
        try {
            Runtime.getRuntime().exec(command);
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(null, CANNOT_OPEN_ERROR_MESSAGE, ERROR_TITLE, ERROR_MESSAGE);
        }
    }
}
