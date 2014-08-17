package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.IFile;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;

public final class IconFactory {

    private static final String HARD_DRIVE_ICON = "icon.drive.hdd";
    public static final String OPTICAL_DRIVE_ICON = "icon.drive.cd";
    public static final String USB_DRIVE_ICON = "icon.drive.usb";
    public static final String ICON_SIZE = "14";
    public static final String FONT_SIZE = "12";

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

    public static Label getFileIcon(IFile file) {
        if (file.isStepBack()) {
            return AwesomeDude.createIconLabel(AwesomeIcon.REPLY, "[ . . ]", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
        }
        if (file.isDirectory()) {
            return AwesomeDude.createIconLabel(AwesomeIcon.FOLDER, file.getName(), "16", FONT_SIZE, ContentDisplay.LEFT);
        }
        return AwesomeDude.createIconLabel(AwesomeIcon.FILE_ALT, file.getName(), ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
    }
}
