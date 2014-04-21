package com.noe.hypercube.ui.tray;

import com.noe.hypercube.ui.tray.table.AnimatedTable;

import javax.swing.*;
import java.awt.*;

public class HPopupMenu extends JPopupMenu {

    private static final String DROPBOX = "Dropbox";
    private static final String DROPBOX_TOOLTIP = "Dropbox files";
    private static final String DROPBOX_ICON_PATH = "images/dropbox-icon.png";
    private static final String GOOGLE_DRIVE = "Google Drive";
    private static final String GOOGLE_DRIVE_TOOLTIP = "Google Drive files";
    private static final String GOOGLE_DRIVE_ICON_PATH = "images/Google-Drive-icon.png";
    private static final int PANEL_SIZE = 500;
    private static final String HYPER_CUBE = "HyperCube";

    public HPopupMenu() {
        super(HYPER_CUBE);
        setOpaque(false);
        setBackground(Color.lightGray);
        setSize(PANEL_SIZE, PANEL_SIZE);
        JComponent tablePanel = createContentTablePanel();
        add(tablePanel);
        pack();
    }

    private JComponent createContentTablePanel() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setSize(500, 500);
        tabbedPane.addTab(DROPBOX, new ImageIcon(DROPBOX_ICON_PATH), createDropboxPanel(), DROPBOX_TOOLTIP);
        tabbedPane.addTab(GOOGLE_DRIVE, new ImageIcon(GOOGLE_DRIVE_ICON_PATH), createDrivePanel(), GOOGLE_DRIVE_TOOLTIP);
//        JPanel jplInnerPanel3 = createInnerPanel();
//        jtbExample.addTab("ALL", null, jplInnerPanel3, "All synched files");
        tabbedPane.setSelectedIndex(0);
       return tabbedPane;
    }

    private JPanel createDropboxPanel() {
        AnimatedTable animatedTable = new AnimatedTable(DROPBOX);
//        DbCache.getInstance().getDbxListener().setTable(animatedTable);
        return createInnerPanel(animatedTable);
    }

    private JPanel createDrivePanel() {
        AnimatedTable animatedTable = new AnimatedTable(GOOGLE_DRIVE);
//        DbCache.getInstance().getDriveListener().setTable(animatedTable);
        return createInnerPanel(animatedTable);
    }

    protected JPanel createInnerPanel(AnimatedTable animatedTable) {
        final JPanel jplPanel = new JPanel();
        jplPanel.setSize(300, 40);
        jplPanel.add(animatedTable);
        return jplPanel;
    }

}
