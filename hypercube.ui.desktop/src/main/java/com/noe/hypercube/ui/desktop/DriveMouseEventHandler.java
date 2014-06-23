package com.noe.hypercube.ui.desktop;

import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;

import static com.noe.hypercube.ui.desktop.NavigationUtil.navigateTo;

public class DriveMouseEventHandler implements EventHandler<MouseEvent> {

    private BreadCrumbBar<String> breadcrumb;
    private TableView<File> table;

    public DriveMouseEventHandler( BreadCrumbBar<String> breadcrumb, TableView<File> table ) {
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
