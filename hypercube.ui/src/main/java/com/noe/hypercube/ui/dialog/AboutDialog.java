package com.noe.hypercube.ui.dialog;


import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class AboutDialog extends Dialog<Boolean> implements Initializable {

    @FXML
    private Label productLabel;

    public AboutDialog() {
        ResourceBundle bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(AddConnectionDialog.class.getClassLoader().getResource("aboutDialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setTitle(bundle.getString("menu.about"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final ImageView imageView = ImageBundle.getImageView("icon.main");
        imageView.setFitWidth(40.0d);
        imageView.setFitHeight(40.0d);
        productLabel.setGraphic(imageView);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
}
