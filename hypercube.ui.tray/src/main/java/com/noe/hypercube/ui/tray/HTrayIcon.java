package com.noe.hypercube.ui.tray;

import com.noe.hypercube.ui.bundle.ImageBundle;

import javax.swing.*;
import java.awt.*;

public class HTrayIcon extends JTrayIcon {

    private static final String TOOLTIP_TEXT = "HyperCube v1.0";
    public static final String DEFAULT_TRAY_ICON_KEY = "tray.default";

    public HTrayIcon(ImageBundle imageBundle, JPopupMenu jpopup) {
        setToolTip(TOOLTIP_TEXT);
        setJPopupMenu(jpopup);
        setImageAutoSize(true);
        Image image = imageBundle.getImage(DEFAULT_TRAY_ICON_KEY);
        setImage(image);
    }


}
