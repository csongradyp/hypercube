package com.noe.hypercube.ui.tray.table;


import com.noe.hypercube.ui.data.EntryInfoHolder;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TableRowMouseListener implements MouseListener {

    private static final String DROPBOX = "Dropbox";
    private static final String GOOGLE_DRIVE = "Google Drive";

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Object source = e.getSource();
            JTable table = (JTable)source;
            int selectedRow = table.getSelectedRow();
            String tableName = table.getName();
            EntryInfoHolder holder;
            switch (tableName) {
                case DROPBOX:
//                    holder = (EntryInfoHolder) DbCache.getInstance().getDbxCacheEntries().get(selectedRow);
                    break;
                case GOOGLE_DRIVE:
//                    holder = (EntryInfoHolder) DbCache.getInstance().getDriveCacheEntries().get(selectedRow);
                    break;
                default:
//                    holder = (EntryInfoHolder) DbCache.getInstance().getDbxCacheEntries().get(selectedRow);
                    break;
            }
//            String localPath = holder.getLocalPath();
//            FileManagerUtil.openFileManager(localPath);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
