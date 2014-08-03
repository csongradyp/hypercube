package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.ui.TrayMenu;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import sun.tools.jar.resources.jar_de;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class TrayMenuFactory {

    private static final Integer WINDOW_SIZE = 300;

    public static void showPopupMenuDialog(final Integer x, final Integer y, final Stage primaryView) {
        final TrayMenu trayMenu = new TrayMenu(primaryView);
        final JFXPanel fxPanel = new JFXPanel();
        final JDialog trayPopupMenu = new JDialog();
        fxPanel.setScene(new Scene(trayMenu, WINDOW_SIZE, WINDOW_SIZE ));
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
