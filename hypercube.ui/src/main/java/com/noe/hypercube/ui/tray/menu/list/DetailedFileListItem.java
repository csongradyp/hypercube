package com.noe.hypercube.ui.tray.menu.list;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.util.IconInjector;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailedFileListItem extends FileListItem implements Initializable {

    public DetailedFileListItem(final FileEvent fileEvent, final ResourceBundle messageBundle) {
        super(fileEvent, messageBundle);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Label streamDirection = IconInjector.getStreamDirectionIcon(fileEvent);
        final Label filePath = createFileLabel(fileEvent);
        getChildren().addAll(streamDirection, filePath, syncTime);
    }

    @Override
    protected Label createFileLabel(final FileEvent fileEvent) {
        String text = createFileText(fileEvent);
        final Label label = new Label(text);
        label.setTooltip(new Tooltip(text));
        IconInjector.setFileStatusIcon(fileEvent, label);
        label.setAlignment(Pos.CENTER_LEFT);
        label.autosize();
        return label;
    }

    private VBox createFileGraphic(final FileEvent fileEvent) {
        VBox graphic = new VBox();
        final Label local = new Label(fileEvent.getLocalPath().toString());
        AwesomeDude.setIcon(local, AwesomeIcon.HOME);
        final Label remote = new Label(fileEvent.getRemotePath().toString(), ImageBundle.getAccountImageView(fileEvent.getAccount()));
        if(StreamDirection.DOWN == fileEvent.getDirection()) {
            graphic.getChildren().addAll(local, remote);
        } else {
            graphic.getChildren().addAll(remote, local);
        }
        return graphic;
    }

    private String createFileText(final FileEvent fileEvent) {
        if(StreamDirection.DOWN == fileEvent.getDirection()) {
            return String.format("%s: %s%n%s: %s", messageBundle.getString("storage.locals"), fileEvent.getLocalPath().toString(),messageBundle.getString("storage.remotes"), fileEvent.getRemotePath().toString());
        }
        return String.format("%s: %s%n%s: %s", messageBundle.getString("storage.remotes"), fileEvent.getLocalPath().toString(),messageBundle.getString("storage.locals"), fileEvent.getRemotePath().toString());
    }

    @Override
    public DetailedFileListItem create(FileEvent file, ResourceBundle messageBundle) {
        return new DetailedFileListItem(file, messageBundle);
    }
}
