package com.noe.hypercube.ui.tray;

import com.noe.hypercube.ui.bundle.ImageBundle;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class HypercubeTrayIcon {
    private static final String TRAY_DEFAULT_IMAGE_KEY = "tray.default";
    private static final String ERROR_TITLE = "ERROR";
    private HTrayIcon trayIcon;
    private boolean firstTime = true;

    public HypercubeTrayIcon(Stage stage) {
        if (SystemTray.isSupported()) {
            final ActionListener closeListener = event -> System.exit(0);
            final ActionListener showListener = event -> Platform.runLater(stage::show);
            final PopupMenu popup = createPopupMenu(closeListener, showListener);
            final Image trayIconImage = ImageBundle.getRawImage(TRAY_DEFAULT_IMAGE_KEY);
            trayIcon = new HTrayIcon(trayIconImage, popup);
            trayIcon.addActionListener(showListener);
            show();
        } else {
            JOptionPane.showMessageDialog(new Frame(), "Tray icon is not supported in your system!", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void show() {
        final SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(new Frame(), "Tray icon can not be shown!", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    private PopupMenu createPopupMenu(final ActionListener closeListener, final ActionListener showListener) {
        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(showListener);
        popup.add(showItem);

        MenuItem closeItem = new MenuItem("Close");
        closeItem.addActionListener(closeListener);
        popup.add(closeItem);
        return popup;
    }

    public void hide() {
        if (firstTime) {
            trayIcon.displayMessage("HyperCube is minimized", "All services will be run in background", TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

}
