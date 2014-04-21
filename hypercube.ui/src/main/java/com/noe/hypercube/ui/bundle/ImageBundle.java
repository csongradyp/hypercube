package com.noe.hypercube.ui.bundle;

import java.awt.*;
import java.util.PropertyResourceBundle;

public class ImageBundle {

    private PropertyResourceBundle bundle;

    public ImageBundle(PropertyResourceBundle bundle) {
        this.bundle = bundle;
    }

    public Image getImage(String imageId) {
        String imagePath = bundle.getString(imageId);
        return Toolkit.getDefaultToolkit().getImage(imagePath);
    }
}
