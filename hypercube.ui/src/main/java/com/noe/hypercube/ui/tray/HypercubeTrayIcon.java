package com.noe.hypercube.ui.tray;

import com.noe.hypercube.ui.bundle.ImageBundle;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import static com.noe.hypercube.ui.tray.menu.TrayMenuFactory.showPopupMenuDialog;

public class HypercubeTrayIcon {
    private static final String TOOLTIP_TEXT = "HyperCube - Cloud Connected (v1.0)";
    private static final String TRAY_DEFAULT_IMAGE_KEY = "tray.default";
    private static final String ERROR_TITLE = "ERROR";
    private TrayIcon trayIcon;
    private boolean firstTime = true;

    public HypercubeTrayIcon(final Stage primaryStage) {
        if (SystemTray.isSupported()) {
            final Image trayIconImage = ImageBundle.getRawImage(TRAY_DEFAULT_IMAGE_KEY);
            trayIcon = new TrayIcon(trayIconImage, TOOLTIP_TEXT);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(event -> Platform.runLater(primaryStage::show));
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (MouseEvent.BUTTON3 == event.getButton()) {
                        Platform.runLater(() -> showPopupMenuDialog(event.getX(), event.getY(), primaryStage));
                    }
                }
            });
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

    public void hide(final ResourceBundle messageBundle) {
        if (firstTime) {
            trayIcon.displayMessage(messageBundle.getString("prompt.minimized.title"), messageBundle.getString("prompt.minimized"), TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

}
