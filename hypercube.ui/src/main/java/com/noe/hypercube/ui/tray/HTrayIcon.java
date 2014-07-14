package com.noe.hypercube.ui.tray;

import javax.swing.*;
import java.awt.*;

public class HTrayIcon extends JTrayIcon {

    private static final String TOOLTIP_TEXT = "HyperCube - Cloud Connected (v1.0)";

    public HTrayIcon(Image image, JPopupMenu jpopup) {
        setToolTip(TOOLTIP_TEXT);
        setJPopupMenu(jpopup);
        setImageAutoSize(true);
        setImage(image);
    }

}
