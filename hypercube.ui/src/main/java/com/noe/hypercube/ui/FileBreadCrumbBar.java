package com.noe.hypercube.ui;

import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import org.controlsfx.control.BreadCrumbBar;

public class FileBreadCrumbBar extends BreadCrumbBar<String> {

    private SimpleBooleanProperty active = new SimpleBooleanProperty();

    public FileBreadCrumbBar(Boolean active) {
        this();
        this.active.set(active);
    }

    public FileBreadCrumbBar() {
        setCrumbFactory();
        active.addListener((observableValue, oldValue, newValue) -> changeStyle(newValue));
    }

    protected void setCrumbFactory() {
        setCrumbFactory(crumb -> {
            final BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton = new BreadCrumbBarSkin.BreadCrumbButton(crumb.getValue() != null ? crumb.getValue() : "");
            breadCrumbButton.setPadding(new Insets(1,8,1,8));
            changeStyle(breadCrumbButton, isActive());
            return breadCrumbButton;
        });
    }

    private void changeStyle(Boolean isActive) {
        final ObservableList<Node> crumbs = getChildren();
        for (Node crumb : crumbs) {
            changeStyle((BreadCrumbBarSkin.BreadCrumbButton) crumb, isActive);
        }
    }

    protected void changeStyle(BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton, final Boolean isActive) {
        if (isActive) {
            setActiveStyle(breadCrumbButton);
        } else {
            setIncativeStyle(breadCrumbButton);
        }
    }

    private void setIncativeStyle(BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton) {
        breadCrumbButton.setStyle("-fx-font: 11 System;");
    }

    private void setActiveStyle(BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton) {
        breadCrumbButton.setStyle("-fx-font: 12 System; -fx-font-weight: bold; -fx-text-fill: white; -fx-base: steelblue; -fx-border-color: white;");
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }
}
