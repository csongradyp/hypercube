package com.noe.hypercube.ui.desktop.tray;

import com.noe.hypercube.ui.desktop.bundle.ImageBundle;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class HypercubeTrayIcon {

    private static final SystemTray TRAY = SystemTray.getSystemTray();
    public static final String ERROR_TITLE = "ERROR";
    private HTrayIcon trayIcon;
    private boolean firstTime = true;

    public HypercubeTrayIcon(Stage stage) {
        if (SystemTray.isSupported()) {
            final ActionListener closeListener = event -> System.exit(0);
            final ActionListener showListener = event -> Platform.runLater(stage::show);
            final JPopupMenu popup = createPopupMenu(closeListener, showListener);
            final Image trayIconImage = ImageBundle.getRawImage("tray.default");
            trayIcon = new HTrayIcon(trayIconImage, popup);
            trayIcon.addActionListener(showListener);
            show();
        } else {
            JOptionPane.showMessageDialog(new Frame(), "Tray icon is not supported in your system!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void show() {
        final SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(this.trayIcon);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(new Frame(), "Tray icon can not be shown!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPopupMenu createPopupMenu(final ActionListener closeListener, final ActionListener showListener) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem showItem = new JMenuItem("Show");
        showItem.addActionListener(showListener);
        popup.add(showItem);

        JMenuItem closeItem = new JMenuItem("Close");
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
