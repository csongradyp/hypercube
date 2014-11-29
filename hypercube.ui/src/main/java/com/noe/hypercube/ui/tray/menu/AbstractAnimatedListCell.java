package com.noe.hypercube.ui.tray.menu;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public abstract class AbstractAnimatedListCell<NODE extends Node> extends ListCell<NODE> {

    public enum AnimationType {
        FADE_OUT,
        FLAP_RIGHT,
        FLATTERN_OUT,
        FLY_FROM_DOWN,
        FLY_FROM_UP,
        ROTATE_RIGHT,
        SPEED_LEFT,
        SPEED_RIGHT,
        TRANSITION_DOWN,
        TRANSITION_LEFT,
        TRANSITION_RIGHT,
        TRANSITION_TOP,
        ZOOM_IN,
        POP_OUT
    }

    protected final AnimationType[] types;
    private final AnimationPack animationPack;

    protected AbstractAnimatedListCell(AnimationType[] types) {
        this.types = types;
        animationPack = new AnimationPack(this);
    }

    protected KeyFrame[] getKeyFrames(AnimationType[] types) {
        if (types == null) {
            return null;
        }
        KeyFrame[] frames = null;
        for (AnimationType type : types) {
            switch (type) {
                case FADE_OUT:
                    frames = animationPack.getFadeOut(frames);
                    break;
                case FLAP_RIGHT:
                    frames = animationPack.getFlapRight(frames);
                    break;
                case FLATTERN_OUT:
                    frames = animationPack.getFlatternOut(frames);
                    break;
                case FLY_FROM_DOWN:
                    frames = animationPack.getFlyFromDown(frames);
                    break;
                case FLY_FROM_UP:
                    frames = animationPack.getFlyFromUp(frames);
                    break;
                case ROTATE_RIGHT:
                    frames = animationPack.getRotateYRight(frames);
                    break;
                case SPEED_LEFT:
                    frames = animationPack.getSpeedLeft(frames);
                    break;
                case SPEED_RIGHT:
                    frames = animationPack.getSpeedRight(frames);
                    break;
                case TRANSITION_DOWN:
                    frames = animationPack.getTransitionDown(frames);
                    break;
                case TRANSITION_LEFT:
                    frames = animationPack.getTransitionLeft(frames);
                    break;
                case TRANSITION_RIGHT:
                    frames = animationPack.getTransitionRight(frames);
                    break;
                case TRANSITION_TOP:
                    frames = animationPack.getTransitionTop(frames);
                    break;
                case ZOOM_IN:
                    frames = animationPack.getZoomIn(0, frames);
                    break;
                case POP_OUT:
                    frames = animationPack.getPopOut(frames);
                    break;
            }
        }
        return frames;

    }

    @Override
    protected void updateItem(NODE item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty ) {
            setGraphic(item);
            if(isFirstItem()) {
                if (animationPack.getKeyFrames().isEmpty()) {
                    KeyFrame[] keyFrames = getKeyFrames(types);
                    if (keyFrames != null) {
                        animationPack.getKeyFrames().addAll(keyFrames);
                    }
                }
                animate();
            }
        } else {
            setGraphic(null);
            setText(null);
        }
    }

    private void animate() {
        if (canPlay()) {
            animationPack.getTimeline().playFromStart();
        }
    }

    private Boolean canPlay() {
        return animationPack != null && animationPack.getKeyFrames().size() >= 0
                && (animationPack.getTimeline().getStatus() == Timeline.Status.STOPPED|| animationPack.getTimeline().getStatus() == Timeline.Status.PAUSED);
    }

    private boolean isFirstItem() {
        return getIndex() == 0;
    }
}
