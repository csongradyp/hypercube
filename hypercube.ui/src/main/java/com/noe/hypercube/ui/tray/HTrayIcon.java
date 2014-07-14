package com.noe.hypercube.ui.tray;

import java.awt.*;

public class HTrayIcon extends TrayIcon {

    private static final String TOOLTIP_TEXT = "HyperCube - Cloud Connected (v1.0)";

    public HTrayIcon(Image image, PopupMenu popup) {
        super(image, TOOLTIP_TEXT, popup);
        setToolTip(TOOLTIP_TEXT);
        setImageAutoSize(true);
    }

}
