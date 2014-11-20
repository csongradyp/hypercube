package com.noe.hypercube.ui.elements;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import net.engio.mbassy.listener.Handler;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CloudActivityBar extends HBox implements Initializable, EventHandler<FileEvent> {

    private static final String ACTIVE_STYLE = "active";
    private static final String INACTIVE_STYLE = "inactive";
    @FXML
    private SimpleBooleanProperty download = new SimpleBooleanProperty(this, "download");
    @FXML
    private Label cloudLabel;
    @FXML
    private HBox accountIcons;
    private Map<String, ImageView> accounts;
    private StreamDirection direction;

    public CloudActivityBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cloudActivityBar.fxml"));
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
        accounts = new HashMap<>();
        EventBus.subscribeToFileEvent(this);
        final ObservableList<AccountInfo> currentaccounts = AccountBundle.getAccounts();
        for (AccountInfo accountInfo : currentaccounts) {
            final ImageView accountImageView = getAccountImageView(accountInfo);
            accounts.put(accountInfo.getName(), accountImageView);
        }
        accountIcons.getChildren().addAll(accounts.values());
        addListenerForAccountChanges();
    }

    private ImageView getAccountImageView(AccountInfo accountInfo) {
        final ImageView accountImageView = ImageBundle.getAccountImageView(accountInfo.getName());
        accountImageView.getStyleClass().add(INACTIVE_STYLE);
        accountImageView.setFitHeight(15d);
        return accountImageView;
    }

    private void addListenerForAccountChanges() {
        AccountBundle.getAccounts().addListener((ListChangeListener<AccountInfo>) change -> {
            while (change.next()) {
                final List<? extends AccountInfo> addedAccount = change.getAddedSubList();
                for (AccountInfo account : addedAccount) {
                    if (account.isActive()) {
                        final ImageView accountImageView = getAccountImageView(account);
                        accounts.put(account.getName(), accountImageView);
                        accountIcons.getChildren().add(accountImageView);
                    }
                }
                final List<? extends AccountInfo> removedAccount = change.getRemoved();
                for (AccountInfo account : removedAccount) {
                    final ImageView accountImageView = accounts.remove(account.getName());
                    accountIcons.getChildren().remove(accountImageView);
                }
            }
        });
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        if (event.getDirection() == direction) {
            Platform.runLater(() -> {
                cloudLabel.getGraphic().getStyleClass().clear();
                final ImageView accountIcon = accounts.get(event.getAccount());
                if(accountIcon != null) {
                    accountIcon.getStyleClass().clear();
                    if (event.isStarted()) {
                        accountIcon.getStyleClass().add(ACTIVE_STYLE);
                        setCloudIconActive();
                    } else if (event.isFinished()) {
                        accountIcon.getStyleClass().add(INACTIVE_STYLE);
                        setCloudIconInactive();
                    }
                }
            });
        }
    }

    private void setCloudIconActive() {
        if(!cloudLabel.getGraphic().getStyleClass().contains(ACTIVE_STYLE)) {
            cloudLabel.getGraphic().getStyleClass().add(ACTIVE_STYLE);
        }
    }

    private void setCloudIconInactive() {
        if(!isTransactionInProgress()) {
            cloudLabel.getGraphic().getStyleClass().add(INACTIVE_STYLE);
        }
    }

    private boolean isTransactionInProgress() {
        for (ImageView imageView : accounts.values()) {
            if(imageView.getStyleClass().contains(ACTIVE_STYLE)) {
                return true;
            }
        }
        return false;
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
        AwesomeDude.setIcon(cloudLabel, (getIcon(direction)), "15");
        cloudLabel.getGraphic().getStyleClass().add(INACTIVE_STYLE);
    }

    @FXML
    public boolean getDownload() {
        return download.get();
    }

}
