package com.noe.hypercube.ui.desktop.event;

import com.noe.hypercube.ui.desktop.domain.File;
import com.noe.hypercube.ui.desktop.domain.LocalFile;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.BreadCrumbBar;

import java.util.ArrayList;
import java.util.List;

import static com.noe.hypercube.ui.desktop.util.NavigationUtil.SEPARATOR;
import static com.noe.hypercube.ui.desktop.util.NavigationUtil.navigateTo;

public class BreadCrumbEventHandler implements EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> {

    private TableView<File> table;

    public BreadCrumbEventHandler(TableView<File> table) {
        this.table = table;
    }

    @Override
    public void handle(BreadCrumbBar.BreadCrumbActionEvent<String> event) {
        TreeItem<String> selectedCrumb = event.getSelectedCrumb();
        List<String> folders = new ArrayList<>();
        while (selectedCrumb != null) {
            folders.add(0, selectedCrumb.getValue());
            selectedCrumb = selectedCrumb.getParent();
        }
        String path = "";
        for (String folder : folders) {
            path += folder + SEPARATOR;
        }
        navigateTo(table, (BreadCrumbBar<String>) event.getSource(), new LocalFile(path));
    }
}
