package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.IFile;
import com.noe.hypercube.ui.domain.LocalFile;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import sun.swing.ImageIconUIResource;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
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
        final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        String driveType = fileSystemView.getSystemTypeDescription(rootPath.toFile());
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
            return createFolderIcon(file);
        }
        return createFileIcon(file);
    }

    private static Label createFolderIcon(IFile file) {
        final ImageView folderIcon = new ImageView(ImageBundle.getImage("thumb.folder"));
        if(file.isShared()) {
            final Label sharedIcon = AwesomeDude.createIconLabel(AwesomeIcon.SHARE_ALT, file.getName(), "12", FONT_SIZE, ContentDisplay.GRAPHIC_ONLY);
            final StackPane graphic = new StackPane(folderIcon, sharedIcon);
            return new Label(file.getName(), graphic);
        }
        return new Label(file.getName(), folderIcon);
    }

    private static Label createFileIcon(IFile file) {
        if(isLocalFile(file)) {
            final Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file.getPath().toFile());
            final ImageView systemIcon;
            if(isSpecialImage(icon)) {
                ImageIconUIResource toolkitImage = (ImageIconUIResource) icon;
                systemIcon = new ImageView(SwingFXUtils.toFXImage(toBufferedImage(toolkitImage.getImage()), null));
            } else {
                final ImageIcon imageIcon = (ImageIcon) icon;
                systemIcon = new ImageView(SwingFXUtils.toFXImage((BufferedImage)imageIcon.getImage(), null));
            }
            return new Label(file.getName(), systemIcon);
        }
        return AwesomeDude.createIconLabel(AwesomeIcon.FILE_ALT, file.getName(), ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
    }

    private static boolean isLocalFile(IFile selectedFile) {
        return LocalFile.class.isAssignableFrom(selectedFile.getClass());
    }

    private static boolean isSpecialImage(Icon icon) {
        return ImageIconUIResource.class.isAssignableFrom(icon.getClass());
    }

    public static BufferedImage toBufferedImage(java.awt.Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bufferedImage;
    }

}
