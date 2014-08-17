package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.action.FileAction;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileActionButton extends Button implements Initializable {

    private static final String copy = "button.copy";
    private static final String copyCloud = "button.copy.cloud";
    private static final String move = "button.move";
    private static final String moveDownload = "button.move.download";
    private static final String moveUpload = "button.move.upload";
    private static final String moveCloud = "button.cloud.move";
    private static final String delete = "button.delete";
    private static final String deleteCloud = "button.delete.cloud";
    private static final String newFolder = "button.newfolder";
    private static final String newCloudFolder = "button.newfolder.cloud";
    private static final String edit = "button.edit";
    private static final String upload = "button.upload";
    private static final String download = "button.download";
    private static final String ICON_SIZE = "15";
    private static final String FONT_SIZE = "12";

    @FXML
    private SimpleObjectProperty<FileAction> action = new SimpleObjectProperty<>();

    public FileActionButton() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileActionButton.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        action.addListener((observable, oldAction, newAction) -> {
            Label iconLabel = null;
            switch (newAction) {
                case COPY:
                    setText(resources.getString(copy));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.COPY, "F5", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case DOWNLOAD:
                    setText(resources.getString(download));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD_DOWNLOAD, "F5", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case UPLOAD:
                    setText(resources.getString(upload));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD_UPLOAD, "F5", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case CLOUD_COPY:
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD, "F5", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    setText(resources.getString(copyCloud));
                    break;
                case MOVE:
                    setText(resources.getString(move));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.COPY, "F6", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case MOVE_UPLOAD:
                    setText(resources.getString(moveUpload));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD, "F5", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case MOVE_DOWNLOAD:
                    setText(resources.getString(moveDownload));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.HDD_ALT, "F6", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case CLOUD_MOVE:
                    setText(resources.getString(moveCloud));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD, "F6", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case NEW_FOLDER:
                    setText(resources.getString(newFolder));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.FOLDER, "F7", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case NEW_CLOUD_FOLDER:
                    setText(resources.getString(newCloudFolder));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.FOLDER_ALT, "F7", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case DELETE:
                    setText(resources.getString(delete));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.TRASH_ALT, "F8", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
                case CLOUD_DELETE:
                    setText(resources.getString(deleteCloud));
                    iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.TRASH_ALT, "F8", ICON_SIZE, FONT_SIZE, ContentDisplay.LEFT);
                    break;
            }
            iconLabel.setStyle("-fx-font-weight: bold");
            setGraphic(iconLabel);
            autosize();
        });
    }

    public FileAction getAction() {
        return action.get();
    }

    @FXML
    public void setAction(FileAction action) {
        this.action.set(action);
    }

    public SimpleObjectProperty<FileAction> actionProperty() {
        return action;
    }
}
