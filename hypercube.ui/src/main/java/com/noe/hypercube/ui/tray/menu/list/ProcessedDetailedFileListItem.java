package com.noe.hypercube.ui.tray.menu.list;

import com.noe.hypercube.event.domain.FileEvent;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Label;

import java.util.ResourceBundle;

public class ProcessedDetailedFileListItem extends DetailedFileListItem {

    public ProcessedDetailedFileListItem(final FileEvent fileEvent, final ResourceBundle messageBundle) {
        super(fileEvent, messageBundle);
    }

    @Override
    protected Label getDefaultStatusIcon() {
        return AwesomeDude.createIconLabel(AwesomeIcon.CLOCK_ALT, "20");
    }
}
