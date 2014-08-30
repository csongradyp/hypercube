package com.noe.hypercube.ui;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

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
            if(crumb.getValue().equals(account)) {
                final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.DROPBOX, "", "14", "0", ContentDisplay.GRAPHIC_ONLY);
                breadCrumbButton.setGraphic(iconLabel);
                if(isActive()) {
                    breadCrumbButton.getGraphic().setStyle("-fx-effect: innershadow(gaussian, white, 7, 1, 1, 1);");
                }
//                AwesomeDude.setIcon(breadCrumbButton, AwesomeIcon.DROPBOX, "14");
//                  breadCrumbButton.setGraphic(new ImageView(account + ".png"));
                breadCrumbButton.setText("");
            }
            changeStyle(breadCrumbButton, isActive());
            return breadCrumbButton;
        });
    }

    public String getAccount() {
        return account;
    }

    public void setCanAddMapping(Boolean canAddMapping) {
        mappingButton.setAdder(canAddMapping);
    }

}
