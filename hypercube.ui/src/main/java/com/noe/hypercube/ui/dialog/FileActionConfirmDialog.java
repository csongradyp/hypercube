package com.noe.hypercube.ui.dialog;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;

import java.nio.file.Path;

public class FileActionConfirmDialog extends Dialog {

    public FileActionConfirmDialog(Object owner, String title) {
        super(owner, title);
    }

    public FileActionConfirmDialog(Object owner, String title, boolean lightweight) {
        super(owner, title, lightweight);
    }

    public FileActionConfirmDialog(Object owner, String title, boolean lightweight, DialogStyle style) {
        super(owner, title, lightweight, style);
    }

    private void init(Path from, Path to) {
        final Label fromLabel = new Label("From: " + from + "*");
        final Label toLabel = new Label("To: " + to + "*");
        final HBox progressBox = new HBox(5);
        progressBox.setAlignment(Pos.CENTER);
        final VBox content = new VBox(5, fromLabel, toLabel, progressBox);
        setContent(content);
    }

}
