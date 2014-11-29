package com.noe.hypercube.ui.tray.menu.list;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.util.IconInjector;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.FileSystems;
import java.util.ResourceBundle;

public class DetailedFileListItem extends FileListItem {

    public DetailedFileListItem(final FileEvent fileEvent, final ResourceBundle messageBundle) {
        super(fileEvent, messageBundle);
        getChildren().clear();
        Label statusIcon = getDefaultStatusIcon();
        final Node fileInfo = createFileLabel(fileEvent);
        getChildren().addAll(statusIcon, fileInfo, syncTime);
        setAlignment(Pos.CENTER_LEFT);
    }

    protected Label getDefaultStatusIcon() {
        return IconInjector.getStreamDirectionIcon(fileEvent);
    }

    public void setStatusIcon(final Label statusIcon) {
        getChildren().remove(0);
        getChildren().add(0, statusIcon);
    }

    @Override
    protected HBox createFileLabel(final FileEvent fileEvent) {
        final VBox graphic = createFileGraphic(fileEvent);
        final Label status = new Label();
        IconInjector.setFileStatusIcon(fileEvent, status);
        return new HBox(5.0d, status, graphic);
    }

    private VBox createFileGraphic(final FileEvent fileEvent) {
        VBox graphic = new VBox(2.0d);
        final Label local = new Label(FileSystems.getDefault().getSeparator() + fileEvent.getLocalPath().toString());
        local.setTooltip(new Tooltip(createFileText(fileEvent)));
        AwesomeDude.setIcon(local, AwesomeIcon.HOME, "18");
        final Label remote = new Label(FileSystems.getDefault().getSeparator() + fileEvent.getRemotePath().toString(), ImageBundle.getAccountImageView(fileEvent.getAccount()));
        remote.setTooltip(new Tooltip(createFileText(fileEvent)));
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
