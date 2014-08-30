package com.noe.hypercube.ui;

import com.noe.hypercube.event.dto.RemoteQuotaInfo;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.util.FileSizeCalculator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;

public class DriveSpaceBar extends HBox {

    @FXML
    private ProgressBar spaceBar;
    @FXML
    private HBox labels;
    @FXML
    private Text total;
    @FXML
    private Text available;
    @FXML
    private Text used;

    public DriveSpaceBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("driveSpaceBar.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void update(Path localPath) {
        final File driveRoot = localPath.getRoot().toFile();
        final Long totalSpace = driveRoot.getTotalSpace();
        final Long freeSpace = driveRoot.getFreeSpace();
        final Long usedSpace = totalSpace - freeSpace;
        update(totalSpace, usedSpace, freeSpace);
    }

    public void update(RemoteQuotaInfo quotaInfo) {
        final Long totalSpace = quotaInfo.getTotalSpace();
        final Long usedSpace = quotaInfo.getUsedSpace();
        final Long freeSpace = totalSpace - usedSpace;
        update(totalSpace, usedSpace, freeSpace);
    }

    private void update(final Long totalSpace, final Long usedSpace, final Long freeSpace) {
        final Double usedPercent = usedSpace.doubleValue() / totalSpace.doubleValue();
        spaceBar.setProgress(usedPercent);
        used.setText((int)(usedPercent*100) + "%");
        total.setText(FileSizeCalculator.humanReadableByteCount(totalSpace));
        available.setText(FileSizeCalculator.humanReadableByteCount(freeSpace));
    }

    public void clear() {
        spaceBar.setProgress(0.0);
        total.setText("?");
        available.setText("?");
    }
}
