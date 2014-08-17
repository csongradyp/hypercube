package com.noe.hypercube.ui.elements;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import net.engio.mbassy.listener.Handler;

import java.io.IOException;

public class CloudActivityIcon extends Label implements EventHandler<FileEvent> {

    @FXML
    private SimpleBooleanProperty download = new SimpleBooleanProperty(this, "download");

    private StreamDirection direction;

    public CloudActivityIcon() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cloudActivityIcon.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        EventBus.subscribeToFileEvent(this);
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        if (event.getDirection() == direction) {
            Platform.runLater(() -> {
                getGraphic().getStyleClass().clear();
                if (event.isStarted()) {
                    getGraphic().getStyleClass().add("active");
                } else if (event.isFinished()) {
                    getGraphic().getStyleClass().add("inactive");
                }
            });
        }
    }

    private AwesomeIcon getIcon(final StreamDirection direction) {
        if (direction == StreamDirection.DOWN) {
            return AwesomeIcon.CLOUD_DOWNLOAD;
        }
        return AwesomeIcon.CLOUD_UPLOAD;
    }

    @FXML
    public void setDownload(final boolean download) {
        this.download.set(download);
        direction = download ? StreamDirection.DOWN : StreamDirection.UP;
        AwesomeDude.setIcon(CloudActivityIcon.this, getIcon(direction), "15");
        getGraphic().getStyleClass().add("inactive");
    }

    @FXML
    public boolean getDownload() {
        return download.get();
    }

}
