package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MappingCrumbButton extends Label implements Initializable {

    private final SimpleBooleanProperty adder = new SimpleBooleanProperty(true);
    private EventHandler<MouseEvent> mouseEnteredEventHandler;
    private EventHandler<MouseEvent> mouseExitedEventHandler;
    private ResourceBundle messageBundle;

    public MappingCrumbButton() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("mappingCrumbButton.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageBundle = resourceBundle;
        setAddMappingStyle();
        setStyle();
        adderProperty().addListener((observable, oldValue, newValue) -> setStyle());
    }


    private void setStyle() {
        removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredEventHandler);
        removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedEventHandler);
        if(adder.get()) {
            setAddMappingStyle();
        } else {
            setRemoveMappingStyle();
        }
        addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredEventHandler);
        addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedEventHandler);
    }

    private void setRemoveMappingStyle() {
        AwesomeDude.setIcon(this, AwesomeIcon.CHAIN, ContentDisplay.GRAPHIC_ONLY);
        mouseEnteredEventHandler = event -> AwesomeDude.setIcon(this, AwesomeIcon.CHAIN_BROKEN, ContentDisplay.GRAPHIC_ONLY);
        mouseExitedEventHandler = event -> AwesomeDude.setIcon(this, AwesomeIcon.CHAIN, ContentDisplay.GRAPHIC_ONLY);
        setTooltip(new Tooltip(messageBundle.getString("tooltip.mapping.remove")));
    }

    protected void setAddMappingStyle() {
        AwesomeDude.setIcon(this, AwesomeIcon.PLUS_SQUARE, ContentDisplay.GRAPHIC_ONLY);
        mouseEnteredEventHandler =  event -> {};
        mouseExitedEventHandler = event -> {};
        setTooltip(new Tooltip(messageBundle.getString("tooltip.mapping.add")));
    }

    public SimpleBooleanProperty adderProperty() {
        return adder;
    }

    public void setAdder(final Boolean adder) {
        this.adder.set(adder);
    }

    public Boolean isAdder() {
        return adder.get();
    }
}
