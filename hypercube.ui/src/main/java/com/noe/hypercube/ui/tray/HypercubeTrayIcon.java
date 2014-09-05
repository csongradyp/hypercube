package com.noe.hypercube.ui.tray;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.FileEventType;
import com.noe.hypercube.ui.bundle.ImageBundle;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.engio.mbassy.listener.Handler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import static com.noe.hypercube.ui.tray.menu.TrayMenuFactory.showPopupMenuDialog;

public class HypercubeTrayIcon implements EventHandler<FileEvent> {
    private static final String TOOLTIP_TEXT = "HyperCube - Cloud Connected (v1.0)";
    private static final String TRAY_DEFAULT_IMAGE_KEY = "tray.default";
    private static final String TRAY_SYNC_IMAGE_KEY = "tray.synchronizing";
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
            EventBus.subscribeToFileEvent(this);
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

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(FileEvent event) {
        final FileEventType eventType = event.getEventType();
        final Image image = getIcon(eventType);
        if(!trayIcon.getImage().equals(image)) {
            trayIcon.setImage(image);
        }
    }

    private Image getIcon(FileEventType eventType) {
        Image image = null;
        if (FileEventType.STARTED == eventType) {
             image = ImageBundle.getRawImage(TRAY_SYNC_IMAGE_KEY);
        } else if (FileEventType.FINISHED == eventType) {
            image = getDefaultIcon();
        }
        return image == null ? getDefaultIcon() : image;
    }

    private Image getDefaultIcon() {
        return ImageBundle.getRawImage(TRAY_DEFAULT_IMAGE_KEY);
    }
}
