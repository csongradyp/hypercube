package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.domain.file.IFile;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class FileListCellFactory implements Callback<ListView<IFile>, ListCell<IFile>> {

    @Override
    public ListCell<IFile> call(ListView<IFile> param) {
        return new ListCell<IFile>(){
            @Override
            protected void updateItem(IFile file, boolean empty) {
                super.updateItem(file, empty);
                if (file != null) {
                    setGraphic(IconFactory.getFileIcon(file));
                    setText(file.getName());
                }
            }
        };
    }
}
