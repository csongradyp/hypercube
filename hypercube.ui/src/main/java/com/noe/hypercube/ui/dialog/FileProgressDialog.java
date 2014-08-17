package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import org.controlsfx.dialog.Dialog;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class FileProgressDialog extends Dialog implements Initializable {

    @FXML
    private Label source;
    @FXML
    private Label destination;
    @FXML
    private Label processedFile;
    @FXML
    private Label counterLabel;
    @FXML
    private Label destinationLabel;

    @FXML
    private Button pauseButton;
    @FXML
    private Button interruptButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressIndicator indicator;

    private final Path sourceFolder;
    private final Path destinationFolder;
    private final Integer fileListSize;

    private AtomicInteger currentIndex;


    public FileProgressDialog(final Object owner, final String title, final Integer fileListSize, final Path sourceFolder, final Path destinationFolder) {
        super(owner, title);
        currentIndex = new AtomicInteger(0);
        this.sourceFolder = sourceFolder;
        this.destinationFolder = destinationFolder;
        ResourceBundle bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("FileProgressDialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.fileListSize = fileListSize;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        source.setText(sourceFolder.toString());
        destination.setText(destinationFolder.toString());
        counterLabel.setText(getCurrentIndex());
    }

    private String getCurrentIndex() {
        return String.format("%d/%d", currentIndex.get(), fileListSize);
    }

    public void hideDestinationFolder() {
        destination.setText("");
        destinationLabel.setText("");
    }

    public void resetProgressBar() {
        final int index = currentIndex.incrementAndGet();
        progressBar.setProgress(0);
        counterLabel.setText(getCurrentIndex());
        indicator.setProgress(index / fileListSize.doubleValue());
    }

    public void setProcessedFile(final String fileName) {
        processedFile.setText(fileName);
        resetProgressBar();
    }

    public void setProgress(Double percentage) {
        Platform.runLater(() -> {
            progressBar.setProgress(percentage);
            indicator.setProgress(fileListSize / currentIndex.doubleValue());
            if (percentage == 1) {
                hide();
            }
        });
    }

}
