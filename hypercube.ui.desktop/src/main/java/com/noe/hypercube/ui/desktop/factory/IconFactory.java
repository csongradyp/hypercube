package com.noe.hypercube.ui.desktop.factory;


import com.noe.hypercube.ui.desktop.domain.IFile;
import javafx.scene.image.Image;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;

public final class IconFactory {

    private static final Image FOLDER_ICON = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/folder.png"));
    private static final Image FILE_ICON = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/file.png"));

    private static final Image HARD_DRIVE_ICON = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/hardDrive_16.png"));
    public static final Image OPTICAL_DRIVE_ICON = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/CD_16.png"));
    public static final Image USB_DRIVE_ICON = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/USB_16.png"));

    private IconFactory() {
    }

    public static Image getStorageIcon(Path rootPath) {
        Image hardDriveIcon = HARD_DRIVE_ICON;
        Image icon = hardDriveIcon;
        String driveType = FileSystemView.getFileSystemView().getSystemTypeDescription(rootPath.toFile());
        if (driveType.toLowerCase().contains("local")) {
            icon = hardDriveIcon;
        } else if (driveType.toLowerCase().contains("cd")) {
            icon = OPTICAL_DRIVE_ICON;
        } else if (driveType.toLowerCase().contains("removable")) {
            icon = USB_DRIVE_ICON;
        }
        return icon;
    }

    public static Image getFileIcon(IFile file) {
        if (file.isStepBack()) {
            return null;
        }
        if (file.isDirectory()) {
            return FOLDER_ICON;
        }
        return FILE_ICON;
    }
}
