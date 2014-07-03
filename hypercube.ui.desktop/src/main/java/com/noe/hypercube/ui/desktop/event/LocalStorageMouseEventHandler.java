package com.noe.hypercube.ui.desktop.event;

import com.noe.hypercube.ui.desktop.domain.File;
import com.noe.hypercube.ui.desktop.domain.LocalFile;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.BreadCrumbBar;

import static com.noe.hypercube.ui.desktop.util.NavigationUtil.navigateTo;

public class LocalStorageMouseEventHandler implements EventHandler<MouseEvent> {

    private BreadCrumbBar<String> breadcrumb;
    private TableView<File> table;

    public LocalStorageMouseEventHandler(TableView<File> table, BreadCrumbBar<String> breadcrumb) {
        this.breadcrumb = breadcrumb;
        this.table = table;
    }

    @Override
    public void handle(MouseEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        navigateTo(table, breadcrumb, new LocalFile(source.getText()));
        if (!source.isSelected()) {
            source.setSelected(true);
        }
    }
}
