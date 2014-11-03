package com.noe.hypercube.ui;

import com.noe.hypercube.ui.elements.MappingCrumbButton;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.controlsfx.control.BreadCrumbBar;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBreadCrumbBar extends HBox {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private final SimpleBooleanProperty active = new SimpleBooleanProperty();
    private final BreadCrumbBar<String> breadCrumbBar;
    protected final MappingCrumbButton mappingButton;

    private EventHandler<MouseEvent> removeMappingEventHandler;
    private EventHandler<MouseEvent> addMappingEventHandler;

    public FileBreadCrumbBar() {
        mappingButton = new MappingCrumbButton();
        breadCrumbBar = new BreadCrumbBar<>();
        breadCrumbBar.setFocusTraversable(false);
        setCrumbFactory();
        active.addListener((observableValue, oldValue, newValue) -> changeStyle(newValue));
        getChildren().addAll(mappingButton, breadCrumbBar);
        mappingButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMappingAction);
    }

    protected void setCrumbFactory() {
        breadCrumbBar.setCrumbFactory(crumb -> {
            final BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton = new BreadCrumbBarSkin.BreadCrumbButton(crumb.getValue() != null ? crumb.getValue() : "");
            breadCrumbButton.setFocusTraversable(false);
            breadCrumbButton.setPadding(new Insets(1, 8, 1, 8));
            changeStyle(breadCrumbButton, isActive());
            return breadCrumbButton;
        });
    }

    private void onMappingAction(MouseEvent event) {
        if(mappingButton.isAdder()) {
            addMappingEventHandler.handle(event);
        } else {
            removeMappingEventHandler.handle(event);
        }
    }

    private void changeStyle(final Boolean isActive) {
        final ObservableList<Node> crumbs = breadCrumbBar.getChildrenUnmodifiable();
        for (Node crumb : crumbs) {
            changeStyle((BreadCrumbBarSkin.BreadCrumbButton) crumb, isActive);
        }
    }

    protected void changeStyle(final BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton, final Boolean isActive) {
        if (isActive) {
            setActiveStyle(breadCrumbButton);
        } else {
            setInactiveStyle(breadCrumbButton);
        }
    }

    private void setInactiveStyle(final BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton) {
        breadCrumbButton.setStyle("-fx-font: 11 System;");
    }

    private void setActiveStyle(final BreadCrumbBarSkin.BreadCrumbButton breadCrumbButton) {
        breadCrumbButton.setStyle("-fx-font: 12 System; -fx-font-weight: bold; -fx-text-fill: white; -fx-base: steelblue; -fx-border-color: white;");
    }

    public void setOnCrumbAction(final EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> eventHandler) {
        breadCrumbBar.setOnCrumbAction(eventHandler);
    }

    public void setOnRemoveMapping(EventHandler<MouseEvent> eventHandler) {
        removeMappingEventHandler = eventHandler;
    }

    public void setOnAddMapping(EventHandler<MouseEvent> eventHandler) {
        addMappingEventHandler = eventHandler;
    }

    public Callback<TreeItem<String>, Button> getCrumbFactory() {
        return breadCrumbBar.getCrumbFactory();
    }

    public void setCrumbFactory(final Callback<TreeItem<String>, Button> crumbFactory) {
        breadCrumbBar.setCrumbFactory(crumbFactory);
    }

    public void setSelectedCrumb(final TreeItem<String> crumb) {
        breadCrumbBar.setSelectedCrumb(crumb);
    }

    public TreeItem<String> getSelectedCrumb() {
        return breadCrumbBar.getSelectedCrumb();
    }

    public Path getLocation() {
        String path = "";
        TreeItem<String> selectedCrumb = breadCrumbBar.getSelectedCrumb();
        while (selectedCrumb != null) {
            path = selectedCrumb.getValue() + SEPARATOR + path;
            selectedCrumb = selectedCrumb.getParent();
        }
        return Paths.get(path);
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

    public void removeMappingButton() {
        getChildren().remove(mappingButton);
    }
}
