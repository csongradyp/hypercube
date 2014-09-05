package com.noe.hypercube.ui.bundle;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;

import static com.noe.hypercube.ui.util.FileNameConvensionUtil.getIconFileName;

public class ImageBundle {

    private static final ImageBundle INSTANCE = new ImageBundle();
    public static final String IMAGES_LOCATION = "./images/";
    private PropertyResourceBundle bundle;
    private Map<String, Image> imageCache;

    private ImageBundle() {
        imageCache = Collections.synchronizedMap(new WeakHashMap<String, Image>());
        try {
            bundle = new PropertyResourceBundle(ImageBundle.class.getClassLoader().getResourceAsStream("images.properties"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static Image getImage(String imageId) {
        if (INSTANCE.imageCache.containsKey(imageId)) {
            return INSTANCE.imageCache.get(imageId);
        }
        Image image = loadImage(imageId);
        INSTANCE.imageCache.put(imageId, image);
        return image;
    }

    public static ImageView getImageView(String imageId) {
        return new ImageView(getImage(imageId));
    }

    private static Image loadImage(String imageId) {
        String imageFileName = INSTANCE.bundle.getString(imageId);
        final URI imageLocation = Paths.get(IMAGES_LOCATION + imageFileName).toUri();
        return new Image(imageLocation.toString(), true);
    }

    public static java.awt.Image getRawImage(String imageId) {
        String imagePath = INSTANCE.bundle.getString(imageId);
        try {
            return ImageIO.read(new File(IMAGES_LOCATION + imagePath));
        } catch (IOException e) {
            return new BufferedImage(0, 0, 0);
        }
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
