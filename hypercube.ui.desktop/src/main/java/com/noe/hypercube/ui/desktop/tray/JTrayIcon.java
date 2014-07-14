package com.noe.hypercube.ui.desktop.tray;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

/**
 * Wrapper for AWT class TrayIcon to be used like a Swing class.
 */
public class JTrayIcon extends TrayIcon {
    private JDialog trayParent;
    private JPopupMenu popupMenu;
    private MouseListener trayIconMouseListener;
    private PopupMenuListener popupMenuListener;

    public JTrayIcon() {
        this("");
        init();
    }

    /**
     * Extend of {@link java.awt.TrayIcon} to provide {@link javax.swing.JPopupMenu}.
     * @param image - Icon image.
     * @param tooltip - Default tool tip text.
     */
    public JTrayIcon(Image image, String tooltip) {
        super(image, tooltip);
        init();
    }

    /**
     * <p>Extend of {@link java.awt.TrayIcon} to provide {@link javax.swing.JPopupMenu}.</p>
     * <p>Icon image must be set after.</p>
     * @param tooltip - Default tool tip text.
     */
    public JTrayIcon(String tooltip) {
        super(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), tooltip);
        init();
    }

    private void init() {
        trayParent = new JDialog();
        trayParent.setSize(0, 0);
        trayParent.setUndecorated(true);
        trayParent.setAlwaysOnTop(true);
        trayParent.setVisible(false);
    }

    private Point computeDisplayPoint(int x, int y, Dimension dim) {
        int computedX = x;
        int computedY = y;
        if (computedX - dim.width > 0) {
            computedX -= dim.width;
        }
        if (computedY - dim.height > 0) {
            computedY -= dim.height;
        }
        return new Point(computedX, computedY);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener listener) {
        if (listener == trayIconMouseListener) {
            return;
        }
        super.removeMouseListener(listener);
    }

    /**
     * Assigns a single popup menu as trayicon menu.
     * @param popup - {@link javax.swing.JPopupMenu}
     */
    public void setJPopupMenu(JPopupMenu popup) {
        popupMenu = popup;
        popupMenuListener = new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                trayParent.setVisible(false);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                trayParent.setVisible(false);
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
        };

        popupMenu.addPopupMenuListener(popupMenuListener);
        setTrayIconMouseListener();
    }

    /**
     * This does nothing! Do NOT use! Only legacy dummy method.
     * @param popup - AWT popup menu.
     */
    @Override
    public void setPopupMenu(PopupMenu popup) {
    }

    private void setTrayIconMouseListener() {
        if (trayIconMouseListener == null) {
            trayIconMouseListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopup(e.getPoint());
                        trayParent.setVisible(false);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        if (trayParent.isVisible()) {
                            trayParent.setVisible(false);
                        } else {
                            showPopup(e.getPoint());
                        }
                    }
                }
            };

            addMouseListener(trayIconMouseListener);
        }
    }

    private void showPopup(final Point p) {
        trayParent.setVisible(true);
        trayParent.toFront();
        Point p2 = computeDisplayPoint(p.x, p.y, popupMenu.getPreferredSize());
        popupMenu.show(trayParent, p2.x - trayParent.getLocation().x, p2.y - trayParent.getLocation().y);
    }

    public void dispose() {
        trayParent.dispose();
        trayParent.remove(popupMenu);
    }

}
