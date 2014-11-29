package com.noe.hypercube.ui.tray.menu;

import javafx.scene.Node;

public class AnimatedListCell<T extends Node> extends AbstractAnimatedListCell<T> {

    public AnimatedListCell(AnimationType... types) {
        super(types);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
    }
}
