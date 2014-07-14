package com.noe.hypercube.ui.factory;


import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.IFile;
import javafx.scene.image.Image;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;

public final class IconFactory {

    private static final String FOLDER_ICON = "thumb.folder";
    private static final String FILE_ICON = "thumb.file";
    private static final String HARD_DRIVE_ICON = "icon.drive.hdd";
    public static final String OPTICAL_DRIVE_ICON = "icon.drive.cd";
    public static final String USB_DRIVE_ICON = "icon.drive.usb";

    private IconFactory() {
    }

    public static Image getStorageIcon(Path rootPath) {
        Image hardDriveIcon = ImageBundle.getImage(HARD_DRIVE_ICON);
        Image icon = hardDriveIcon;
        String driveType = FileSystemView.getFileSystemView().getSystemTypeDescription(rootPath.toFile());
        if (driveType.toLowerCase().contains("local")) {
            icon = hardDriveIcon;
        } else if (driveType.toLowerCase().contains("cd")) {
            icon = ImageBundle.getImage(OPTICAL_DRIVE_ICON);
        } else if (driveType.toLowerCase().contains("removable")) {
            icon = ImageBundle.getImage(USB_DRIVE_ICON);
        }
        return icon;
    }

    public static Image getFileIcon(IFile file) {
        if (file.isStepBack()) {
            return null;
        }
        if (file.isDirectory()) {
            return ImageBundle.getImage(FOLDER_ICON);
        }
        return ImageBundle.getImage(FILE_ICON);
    }
}
