package com.noe.hypercube.ui.tray.menu.list;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.ui.util.FileManagerUtil;
import com.noe.hypercube.ui.util.IconInjector;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class TrayFileListItem extends FileListItem implements Initializable {

    private static final double HEIGHT = 40.0d;

    public TrayFileListItem(FileEvent fileEvent, ResourceBundle messageBundle) {
        super(fileEvent, messageBundle);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Label streamDirection = IconInjector.getStreamDirectionIcon(fileEvent);
        final Label filePath = createFileLabel(fileEvent);
        final Button viewButton = createShareButton(fileEvent);
        setOnMouseEntered(event -> viewButton.setVisible(true));
        setOnMouseExited(event -> viewButton.setVisible(false));
        setOnMouseClicked(event -> {
            if (isDoubleClick(event)) {
                FileManagerUtil.openFileManager(fileEvent.getLocalPath().toString());
            }
        });
        getChildren().clear();
        getChildren().addAll(streamDirection, filePath, viewButton, syncTime);
    }

    private boolean isDoubleClick(final MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton().compareTo(MouseButton.PRIMARY) == 0;
    }

    @Override
    protected Label createFileLabel(final FileEvent file) {
        final Label fileName = new Label(file.getLocalPath().getFileName().toString());
        IconInjector.setFileStatusIcon(file, fileName);
        fileName.setPrefSize(150, HEIGHT);
        fileName.setAlignment(Pos.CENTER_LEFT);
        return fileName;
    }

    private Button createShareButton(final FileEvent file) {
        final Button viewButton = new Button();
        viewButton.setPrefSize(15, 15);
        viewButton.setFocusTraversable(false);
        AwesomeDude.setIcon(viewButton, AwesomeIcon.SHARE_ALT);
        viewButton.setVisible(false);
        viewButton.setOnAction(event -> {
            System.out.println(file.getLocalPath());
        });
        return viewButton;
    }

    @Override
    public TrayFileListItem create(FileEvent file, ResourceBundle messageBundle) {
        return new TrayFileListItem(file, messageBundle);
    }
}
