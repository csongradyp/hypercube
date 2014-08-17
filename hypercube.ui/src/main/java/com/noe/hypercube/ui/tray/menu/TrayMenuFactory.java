package com.noe.hypercube.ui.tray.menu;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TrayMenuFactory {

    private static final Integer WINDOW_SIZE = 300;

    public static void showPopupMenuDialog(final Integer x, final Integer y, final Stage primaryView) {
        final TrayMenu trayMenu = new TrayMenu(primaryView);
        final JFXPanel fxPanel = new JFXPanel();
        final JDialog trayPopupMenu = new JDialog();
        fxPanel.setScene(new Scene(trayMenu, WINDOW_SIZE, WINDOW_SIZE));
        trayPopupMenu.add(fxPanel);
        trayPopupMenu.setAlwaysOnTop(true);
        trayPopupMenu.setUndecorated(true);
        trayPopupMenu.setBounds(x - WINDOW_SIZE, y - WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE);
        trayPopupMenu.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                trayPopupMenu.dispose();
            }
        });
        trayPopupMenu.setVisible(true);
    }

}
