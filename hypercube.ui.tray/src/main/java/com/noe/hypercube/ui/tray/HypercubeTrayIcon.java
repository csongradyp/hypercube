package com.noe.hypercube.ui.tray;

import javax.swing.*;
import java.awt.*;

public class HypercubeTrayIcon {

    private static final SystemTray TRAY = SystemTray.getSystemTray();
    public static final String ERROR_TITLE = "ERROR";
    private HTrayIcon trayIcon;

    public HypercubeTrayIcon(HTrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    public void show() {
        if (SystemTray.isSupported()) {
            try {
                TRAY.add(trayIcon);
            } catch (AWTException e) {
                JOptionPane.showMessageDialog(new Frame(), "Tray icon can not be shown!", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(new Frame(), "Tray icon is not supported in your system!", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeTrayIcon() {
        TRAY.remove(trayIcon);
        trayIcon.dispose();
    }

}
