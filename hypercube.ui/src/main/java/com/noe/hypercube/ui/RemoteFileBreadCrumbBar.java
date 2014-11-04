package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ImageBundle;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.controlsfx.control.BreadCrumbBar;

public class RemoteFileBreadCrumbBar extends FileBreadCrumbBar {

    private String account;

    public RemoteFileBreadCrumbBar(String account) {
        super();
        setCanAddMapping(false);
        this.account = account;
    }

    @Override
    protected void setCrumbFactory() {
        setCrumbFactory(crumb -> {
            final BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton = new BreadCrumbBarSkin.BreadCrumbButton(crumb.getValue() != null ? crumb.getValue() : "");
            breadCrumbButton.setFocusTraversable(false);
            breadCrumbButton.setPadding(new Insets(1, 8, 1, 8));
            if(isAccountRoot(crumb)) {
                setAccountIcon(breadCrumbButton);
            }
            changeStyle(breadCrumbButton, isActive());
            return breadCrumbButton;
        });
    }

    private void setAccountIcon(BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton) {
        breadCrumbButton.setText("");
        breadCrumbButton.setGraphic(getAccountImage());
        if(isActive()) {
            breadCrumbButton.getGraphic().setStyle("-fx-effect: innershadow(gaussian, white, 7, 1, 1, 1);");
        }
    }

    private boolean isAccountRoot(TreeItem<String> crumb) {
        return crumb.getValue().equals(account) && crumb.getParent() == null;
    }

    public Path getPath(final BreadCrumbBar.BreadCrumbActionEvent<String> event) {
        String path = "";
        TreeItem<String> selectedCrumb = event.getSelectedCrumb();
        while (selectedCrumb != null) {
            final String folder = selectedCrumb.getValue();
            if (isAccountRoot(selectedCrumb)) {
                path = "/" + path;
            } else {
                path = folder + "/" + path;
            }
            selectedCrumb = selectedCrumb.getParent();
        }
        return Paths.get(path);
    }

    private ImageView getAccountImage() {
        final ImageView accountImageView = ImageBundle.getAccountImageView(account);
        accountImageView.setFitHeight(14.0);
        return accountImageView;
    }

    public String getAccount() {
        return account;
    }

    public void setCanAddMapping(Boolean canAddMapping) {
        mappingButton.setAdder(canAddMapping);
    }

}
