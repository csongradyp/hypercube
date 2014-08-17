package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.util.ProgressAwareInputStream;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;

import java.io.IOException;

public final class DialogFactory {

    public static Dialog createLoginDialog() {
        final TextField txUserName = new TextField();
        final PasswordField txPassword = new PasswordField();

        final Action actionLogin = new AbstractAction("Login") {
            {
                ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
            }

            @Override
            public void handle(ActionEvent event) {
                Dialog dlg = (Dialog) event.getSource();
                // real login code here
                dlg.hide();
            }
        };

        Dialog dialog = new Dialog(null, "Login Dialog", false, DialogStyle.NATIVE);

        // listen to user input on dialog (to enable / disable the login button)
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> validate(actionLogin, txUserName, txPassword);
        txUserName.textProperty().addListener(changeListener);
        txPassword.textProperty().addListener(changeListener);

        // layout a custom GridPane containing the input fields and labels
        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.add(new Label("User name"), 0, 0);
        content.add(txUserName, 1, 0);
        content.add(new Label("Password"), 0, 1);
        content.add(txPassword, 1, 1);

        dialog.setResizable(false);
        dialog.setIconifiable(false);
        dialog.setContent(content);
        dialog.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
        validate(actionLogin, txUserName, txPassword);

        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(txUserName::requestFocus);
        return dialog;
    }

    private static void validate(Action action, TextInputControl... inputControl) {
        boolean valid = true;
        for (TextInputControl input : inputControl) {
            valid &= !input.getText().trim().isEmpty();
        }
        action.disabledProperty().set(!valid);
    }

    public static Dialog createProgressDialog(String tile, ProgressAwareInputStream stream) {
        final Dialog dialog = new Dialog(null, tile, false, DialogStyle.CROSS_PLATFORM_DARK);
        final ProgressBar pb = new ProgressBar(0);
        final ProgressIndicator pi = new ProgressIndicator(0);

        stream.setOnProgressListener((percentage, tag) -> Platform.runLater(() -> {
            pb.setProgress(percentage);
            pi.setProgress(percentage);
            if (percentage == 1) {
                dialog.hide();
            }
        }));

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(pb, pi);
        dialog.setContent(hb);

        return dialog;
    }

    public static Dialog createProgressDialog(String tile) {
        final Dialog dialog = new Dialog(null, tile, false, DialogStyle.CROSS_PLATFORM_DARK);
        final ProgressBar pb = new ProgressBar(0);
        final ProgressIndicator pi = new ProgressIndicator(0);

//        stream.setOnProgressListener( ( percentage, tag ) -> Platform.runLater( () -> {
//            pb.setProgress(percentage);
//            pi.setProgress(percentage);
//            if(percentage == 1) {
//                dialog.hide();
//            }
//        } ) );

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(pb, pi);
        dialog.setContent(hb);

        return dialog;
    }

    public static Dialog createFxmlDialog(String tile, String fxmlContentPath) throws IOException {
        Dialog dialog = new Dialog(null, tile);
        Node content = FXMLLoader.load(DialogFactory.class.getClassLoader().getResource(fxmlContentPath));

        dialog.setResizable(false);
        dialog.setIconifiable(false);
        dialog.setContent(content);
        dialog.getActions().addAll(Dialog.Actions.OK, Dialog.Actions.CANCEL);
        return dialog;
    }
}

