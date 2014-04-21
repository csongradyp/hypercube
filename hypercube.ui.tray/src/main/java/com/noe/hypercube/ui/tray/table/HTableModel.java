package com.noe.hypercube.ui.tray.table;

import javax.swing.table.DefaultTableModel;

public class HTableModel extends DefaultTableModel {

    public HTableModel() {
        super(0,3);
    }

    @Override public Class<?> getColumnClass(int column) {
        switch(column) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            default:
                return super.getColumnClass(column);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column){
        return false;
    }
}
