package com.noe.hypercube.ui.desktop;

import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;

import static com.noe.hypercube.ui.desktop.NavigationUtil.navigateTo;

public class FileViewMouseEventHandler implements EventHandler<MouseEvent> {

    private BreadCrumbBar<String> breadcrumb;

    public FileViewMouseEventHandler(BreadCrumbBar<String> breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    @Override
    public void handle( MouseEvent event ) {
        TableView<File> table = (TableView) event.getSource();
        if ( isDoubleClick( event ) ) {
            File selectedItem = table.getSelectionModel().getSelectedItem();
            navigateTo( table, breadcrumb, selectedItem );
        }
    }

    private boolean isDoubleClick( MouseEvent event ) {
        return event.getClickCount() == 2 && event.getButton().compareTo( MouseButton.PRIMARY ) == 0;
    }
}
