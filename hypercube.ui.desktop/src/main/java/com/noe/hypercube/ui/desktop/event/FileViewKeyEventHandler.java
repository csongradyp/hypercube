package com.noe.hypercube.ui.desktop.event;

import com.noe.hypercube.ui.desktop.util.NavigationUtil;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;

import static com.noe.hypercube.ui.desktop.util.NavigationUtil.navigateTo;
import static com.noe.hypercube.ui.desktop.util.NavigationUtil.stepBack;

public class FileViewKeyEventHandler implements EventHandler<KeyEvent> {

    private final TableView<File> tableView;
    private final BreadCrumbBar<String> breadCrumb;

    public FileViewKeyEventHandler(TableView<File> tableView, BreadCrumbBar<String> breadCrumb) {
        this.tableView = tableView;
        this.breadCrumb = breadCrumb;
    }

    public void init(File file) {
        navigateTo( tableView, breadCrumb, file );
    }

    @Override public void handle( KeyEvent event ) {
        TableView source = (TableView)event.getSource();
        File selectedFile = (File)source.getSelectionModel().getSelectedItem();

        if (event.getCode() == KeyCode.BACK_SPACE) {
            if (!selectedFile.getName().equals(NavigationUtil.TO_PARENT_PLACEHOLDER)) {
                 selectedFile = selectedFile.getParentFile();
            }
            stepBack( tableView, breadCrumb, selectedFile );
        }
        else if (event.getCode() == KeyCode.ENTER) {
            navigateTo( tableView, breadCrumb, selectedFile );
        }
        else if (event.getCode() == KeyCode.SPACE) {
//            TablePosition focusedCell = source.getFocusModel().getFocusedCell();
//            String s = FileSizeCalculator.calculateSelected( selectedFile );
//            System.out.println(s);
        }
    }
}

