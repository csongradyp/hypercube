package com.noe.hypercube.ui.desktop;


import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration {

    private Ini ini;
    private static final Configuration instance = new Configuration();

    private Configuration(){
        try {
            URL resource = getClass().getClassLoader().getResource("settings/settings.ini");
            ini = new Ini(new File(resource.toURI()));
        } catch (IOException | URISyntaxException e) {
            try {
                ini = new Ini(new File("settings.ini"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public Ini getIni() {
        return ini;
    }

    public static Path getStartLocation(String side) {
        String location = instance.getIni().get("location").get(side);
        return Paths.get(location);
    }

    public static void setStartLocation(String side, Path newLocation) {
        instance.getIni().get("location").replace(side, newLocation.toString());
        storeConfig();
    }

    private static void storeConfig() {
        try {
            instance.getIni().store();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
