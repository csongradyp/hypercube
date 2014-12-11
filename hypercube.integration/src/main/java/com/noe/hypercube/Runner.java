package com.noe.hypercube;

import com.noe.hypercube.monitoring.InstanceMonitor;
import com.noe.hypercube.monitoring.SingleInstanceMonitor;
import com.noe.hypercube.ui.FileCommander;
import java.awt.*;
import java.io.IOException;
import javafx.application.Application;
import javax.swing.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Runner {

    private static final String ALREADY_RUNNING_ERROR_TITLE = "HyperCube - Running Error";
    private static final String ALREADY_RUNNING_ERROR_MSG = "Program is already running!";

    private static final String CONTEXT_XML_PATH = "META-INF/appcontext.xml";

    private static ConfigurableApplicationContext APPLICATION_CONTEXT;
    private static InstanceMonitor INSTANCE_MONITOR = new SingleInstanceMonitor();
    private static HyperCubeApp app;

    public static void main(String[] args) throws IOException {
        if (INSTANCE_MONITOR.isAlreadyRunning()) {
            JOptionPane.showMessageDialog(new Frame(), ALREADY_RUNNING_ERROR_MSG, ALREADY_RUNNING_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } else {
            createGui();
            APPLICATION_CONTEXT = new ClassPathXmlApplicationContext(CONTEXT_XML_PATH);
            APPLICATION_CONTEXT.registerShutdownHook();
            app = APPLICATION_CONTEXT.getBean(HyperCubeApp.class);
            app.start();
        }
    }

    private static void createGui() {
        new Thread(() -> Application.launch(FileCommander.class)).start();
    }

}
