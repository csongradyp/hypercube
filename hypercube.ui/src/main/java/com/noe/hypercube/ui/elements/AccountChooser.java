package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class AccountChooser extends ComboBox<String> {

    public AccountChooser() {
        super(new ObservableListWrapper<>(AccountBundle.getConnectedAccountNames()));
        setMinWidth(100.0d);
        final Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setGraphic(ImageBundle.getAccountImageView(item));
                            setText(item);
                        } else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        };
        setButtonCell(cellFactory.call(null));
        setCellFactory(cellFactory);
        if(!getItems().isEmpty()) {
            setValue(getItems().get(0));
        }
    }
}
