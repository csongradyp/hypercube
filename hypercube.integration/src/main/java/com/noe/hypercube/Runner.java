package com.noe.hypercube;

import com.noe.hypercube.monitoring.InstanceMonitor;
import com.noe.hypercube.monitoring.SingleInstanceMonitor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class Runner {

    private static final String ALREADY_RUNNING_ERROR_TITLE = "HyperCube - Running Error";
    private static final String ALREADY_RUNNING_ERROR_MSG = "Program is already running!";

    private static final String CONTEXT_XML_PATH = "classpath:/META-INF/appcontext.xml";
    public static final String EXIT_CONFIRM_MESSAGE = "Do you really want to exit?\nUnsaved settings will be lost";
    public static final String EXIT_PANE_TITLE = "Exit HyperCube";

    private static ConfigurableApplicationContext APPLICATION_CONTEXT;
    private static InstanceMonitor instanceMonitor = new SingleInstanceMonitor();
    private static HyperCubeApp app;
//    private static Gui gui;


    public static void main(String[] args) throws IOException {
//        updateAuthProperty();
        if (!instanceMonitor.isAlreadyRunning()) {
//            createGui();
            APPLICATION_CONTEXT = new FileSystemXmlApplicationContext(CONTEXT_XML_PATH);
            app = APPLICATION_CONTEXT.getBean(HyperCubeApp.class);
//            app.test();
            app.start();
        } else {
            JOptionPane.showMessageDialog(new Frame(), ALREADY_RUNNING_ERROR_MSG, ALREADY_RUNNING_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            Runner.exit();
        }
    }

//    private static void updateAuthProperty() {
//        String url = Connect.getUrl();
//        JOptionPane.showInputDialog(null, "Open in browser", url);
//        String authCode = JOptionPane.showInputDialog("Enter Authorization code");
//        Properties properties = new Properties();
//        InputStream resourceAsStream = Connect.class.getClassLoader().getResourceAsStream("auth.properties");
//        try {
//            properties.load(resourceAsStream);
//            properties.setProperty("google.auth.code", authCode);
//            properties.store(new FileWriter(new File("D:\\Code\\hypercube\\src\\main\\resources\\auth.properties")),"");
//        } catch (IOException e) {
//        }
//    }

    public static void exit() {
        if (canExit()) {
//            gui.removeTrayIcon();
            app.stop();
            APPLICATION_CONTEXT.close();
            System.exit(0);
        }
    }

    private static boolean canExit() {
        boolean exit = true;
//        if (isModifierUIElementOpen()) {
//            exit = isExitConfirmed();
//        }
        return exit;
    }

//    private static boolean isModifierUIElementOpen() {
//        boolean found = false;
//        Frame[] frames = Frame.getFrames();
//        for (Frame frame : frames) {
//            if (frame instanceof ModifierUIElement && frame.isVisible()) {
//                found = true;
//            }
//        }
//        return found;
//    }

    private static boolean isExitConfirmed() {
        boolean exit = false;
        int userInput = JOptionPane.showConfirmDialog(null, EXIT_CONFIRM_MESSAGE, EXIT_PANE_TITLE, YES_NO_OPTION);
        if (userInput == YES_OPTION) {
            exit = true;
        }
        return exit;
    }

//    private static void createGui() {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                try(InputStream imagePropertyStream = Runner.class.getResourceAsStream("/images.properties")) {
//                    PropertyResourceBundle resourceBundle = new PropertyResourceBundle(imagePropertyStream);
//                    ImageBundle imageBundle = new ImageBundle(resourceBundle);
//                    JPopupMenu popupMenu = new HPopupMenu();
//                    HTrayIcon trayIcon = new HTrayIcon(imageBundle, popupMenu);
//                    gui = new Gui(trayIcon);
//                    gui.createGUI();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (IOException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }
//        });
//    }

}
