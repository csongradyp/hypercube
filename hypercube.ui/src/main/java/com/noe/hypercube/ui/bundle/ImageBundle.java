package com.noe.hypercube.ui.bundle;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.WeakHashMap;

import static com.noe.hypercube.ui.util.FileNameConvensionUtil.getIconFileName;

public class ImageBundle {

    private static final ImageBundle INSTANCE = new ImageBundle();
    public static final String IMAGES_LOCATION = "./images/";
    private final PropertyResourceBundle bundle;
    private final Map<String, Image> imageCache;

    private ImageBundle() {
        imageCache = Collections.synchronizedMap(new WeakHashMap<>());
        try {
            bundle = new PropertyResourceBundle(ImageBundle.class.getClassLoader().getResourceAsStream("images.properties"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static Image getImage(final String imageId) {
        if (INSTANCE.imageCache.containsKey(imageId)) {
            return INSTANCE.imageCache.get(imageId);
        }
        Image image = loadImage(imageId);
        INSTANCE.imageCache.put(imageId, image);
        return image;
    }

    public static ImageView getImageView(final String imageId) {
        return new ImageView(getImage(imageId));
    }

    private static Image loadImage(final String imageId) {
        String imageFileName = INSTANCE.bundle.getString(imageId);
        final URI imageLocation = Paths.get(IMAGES_LOCATION + imageFileName).toUri();
        return new Image(imageLocation.toString(), true);
    }

    public static java.awt.Image getRawImage(final String imageId) {
        String imagePath = INSTANCE.bundle.getString(imageId);
        return Toolkit.getDefaultToolkit().getImage(IMAGES_LOCATION + imagePath);
    }

    public static Image getAccountImage(final String account) {
        if (INSTANCE.imageCache.containsKey(account)) {
            return INSTANCE.imageCache.get(account);
        }
        final URI imageLocation = Paths.get(IMAGES_LOCATION + getIconFileName(account)).toUri();
        final Image image = new Image(imageLocation.toString(), true);
        INSTANCE.imageCache.put(account, image);
        return image;
    }

    public static ImageView getAccountImageView(final String account) {
        final ImageView accountIcon = new ImageView(getAccountImage(account));
        accountIcon.setPreserveRatio(true);
        accountIcon.setSmooth(true);
        accountIcon.setFitHeight(16.0);
        return accountIcon;
    }

}
