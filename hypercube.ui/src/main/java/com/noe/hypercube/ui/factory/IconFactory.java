package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.file.IFile;
import com.noe.hypercube.ui.domain.file.LocalFile;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.AwesomeIconsStack;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import sun.swing.ImageIconUIResource;

public final class IconFactory {

    private static final String HARD_DRIVE_ICON = "icon.drive.hdd";
    public static final String OPTICAL_DRIVE_ICON = "icon.drive.cd";
    public static final String USB_DRIVE_ICON = "icon.drive.usb";
    public static final String ICON_SIZE = "14";
    public static final String FONT_SIZE = "12";

    private IconFactory() {
    }

    public static Image getStorageIcon(final Path rootPath) {
        Image hardDriveIcon = ImageBundle.getImage(HARD_DRIVE_ICON);
        Image icon = hardDriveIcon;
        final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        String driveType = fileSystemView.getSystemTypeDescription(rootPath.toFile());
        if (driveType.toLowerCase().contains("local")) {
            icon = hardDriveIcon;
        } else if (driveType.toLowerCase().contains("cd") || driveType.toLowerCase().contains("opti")) {
            icon = ImageBundle.getImage(OPTICAL_DRIVE_ICON);
            //TODO provide language independent solution
        } else if (driveType.toLowerCase().contains("removable") || driveType.toLowerCase().contains("cser")) {
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

    private static Label createFolderIcon(final IFile file) {
        final de.jensd.fx.fontawesome.Icon folder = new de.jensd.fx.fontawesome.Icon(AwesomeIcon.FOLDER, "16", "", "folder");
        final AwesomeIconsStack awesomeIconsStack = AwesomeIconsStack.create().add(folder);
        if (file.isShared()) {
            final de.jensd.fx.fontawesome.Icon shared = new de.jensd.fx.fontawesome.Icon(AwesomeIcon.SHARE_ALT, "10", "", "");
            awesomeIconsStack.add(shared);
        }
        awesomeIconsStack.autosize();
        awesomeIconsStack.setAlignment(Pos.BOTTOM_LEFT);
        return new Label(file.getName(), awesomeIconsStack);
    }

    private static Label createFileIcon(final IFile file) {
        if (isLocalFile(file)) {
            final Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file.getPath().toFile());
            final ImageView systemIcon;
            if (isSpecialImage(icon)) {
                ImageIconUIResource toolkitImage = (ImageIconUIResource) icon;
                systemIcon = new ImageView(SwingFXUtils.toFXImage(toBufferedImage(toolkitImage.getImage()), null));
            } else {
                final ImageIcon imageIcon = (ImageIcon) icon;
                systemIcon = new ImageView(SwingFXUtils.toFXImage((BufferedImage) imageIcon.getImage(), null));
            }
            return new Label(file.getName(), systemIcon);
        }
        return AwesomeDude.createIconLabel(AwesomeIcon.FILE_ALT, file.getName(), ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
    }

    private static boolean isLocalFile(final IFile selectedFile) {
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
