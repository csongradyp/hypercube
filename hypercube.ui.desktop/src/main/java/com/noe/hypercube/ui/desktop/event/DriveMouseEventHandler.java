package com.noe.hypercube.ui.desktop.event;

import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;

import static com.noe.hypercube.ui.desktop.util.NavigationUtil.navigateTo;

public class DriveMouseEventHandler implements EventHandler<MouseEvent> {

    private BreadCrumbBar<String> breadcrumb;
    private TableView<File> table;

    public DriveMouseEventHandler(TableView<File> table, BreadCrumbBar<String> breadcrumb ) {
        this.breadcrumb = breadcrumb;
        this.table = table;
    }

    @Override public void handle( MouseEvent event ) {
        ToggleButton source = (ToggleButton) event.getSource();
        navigateTo( table, breadcrumb, new File( source.getText() ) );
        if(!source.isSelected()) {
            source.setSelected(true);
        }
    }
}
