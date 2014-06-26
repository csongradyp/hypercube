package com.noe.hypercube.ui.desktop.factory;


import javafx.scene.image.Image;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;

public final class IconFactory {

    private IconFactory() { }

    public static Image getStorageIcon(Path rootPath) {
        Image hardDriveIcon = new Image(IconFactory.class.getClassLoader().getResourceAsStream( "images/hardDrive_16.png" ));
        Image icon = hardDriveIcon;
        String driveType = FileSystemView.getFileSystemView().getSystemTypeDescription( rootPath.toFile() );
        if(driveType.toLowerCase().contains( "local" )) {
            icon = hardDriveIcon;
        }
        else if(driveType.toLowerCase().contains( "cd" )) {
            icon = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/CD_16.png"));
        }
        else if(driveType.toLowerCase().contains( "removable" )) {
            icon = new Image(IconFactory.class.getClassLoader().getResourceAsStream("images/USB_16.png"));
        }
        return icon;
    }

}
