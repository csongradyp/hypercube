package com.noe.hypercube.ui.desktop.bundle;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.WeakHashMap;

public class ImageBundle {

    private static final ImageBundle INSTANCE = new ImageBundle();
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
        if(INSTANCE.imageCache.containsKey(imageId)) {
            return INSTANCE.imageCache.get(imageId);
        }
        Image image = loadImage(imageId);
        INSTANCE.imageCache.put(imageId, image);
        return image;
    }

    private static Image loadImage(String imageId) {
        String imagePath = INSTANCE.bundle.getString(imageId);
        return new Image(imagePath, true);
    }

    public static java.awt.Image getRawImage(String imageId) {
        String imagePath = INSTANCE.bundle.getString(imageId);
        try {
            return ImageIO.read(ClassLoader.getSystemResource(imagePath));
        } catch (IOException e) {
            return new BufferedImage(0, 0, 0);
        }
    }
}
