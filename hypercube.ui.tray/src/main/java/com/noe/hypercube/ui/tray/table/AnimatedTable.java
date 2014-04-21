package com.noe.hypercube.ui.tray.table;


import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class AnimatedTable  extends JPanel  implements TableModelListener {
    private static final int START_HEIGHT = 2;
    private static final int END_HEIGHT = 40;
    private static final int DELAY = 10;
    private final DefaultTableModel model = new HTableModel();
    private final JTable table = new JTable(model);

    public AnimatedTable(String tableName) {
        super(new BorderLayout());
        table.setName(tableName);
        table.setAutoCreateRowSorter(false);
        table.setRowHeight(END_HEIGHT);
        table.setShowVerticalLines(false);
        table.setTableHeader(null);
        table.setInheritsPopupMenu(true);
        table.setSelectionMode(SINGLE_SELECTION);
        table.addMouseListener(new TableRowMouseListener());
        model.addTableModelListener(this);
        JScrollPane scroll = new JScrollPane(table);
        setPreferredSize(new Dimension(320, 240));
        add(scroll);
    }

    public void animateAddRow(final int rowIndex) {
        (new Timer(DELAY, new ActionListener() {
            int height = START_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                if(height < END_HEIGHT) {
                    table.setRowHeight(rowIndex, height++);
                }else{
                    ((Timer)e.getSource()).stop();
                }
            }
        })).start();
    }

    public void animateRemoveRow(final int row) {
        final int[] selection = table.getSelectedRows();
        if(selection != null && selection.length > 0) {
            (new Timer(DELAY, new ActionListener() {
                int height = END_HEIGHT;
                @Override public void actionPerformed(ActionEvent e) {
                    height--;
                    if(height >START_HEIGHT) {
                        table.setRowHeight(row, height);
                    }else{
                        ((Timer)e.getSource()).stop();
                        model.removeRow(table.convertRowIndexToModel(row));
                    }
                }
            })).start();
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int type = e.getType();
        switch(type) {
            case TableModelEvent.INSERT :
                animateAddRow(row);
                break;
            case TableModelEvent.DELETE :
                animateRemoveRow(row);
                break;
        }
    }

    public DefaultTableModel getModel() {
        return model;
    }

}
