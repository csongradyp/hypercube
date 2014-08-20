package com.noe.hypercube.ui.bundle;


import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ConfigurationBundle {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationBundle.class);

    private static final BidiMap<String, String> languages = new DualHashBidiMap<>();
    private static final ConfigurationBundle instance = new ConfigurationBundle();
    private static final String LOCATION_SECTION = "location";
    public static final String LANGUAGE = "language";
    private final Ini ini;

    private ConfigurationBundle() {
        languages.put("Magyar", "hu");
        languages.put("English", "en");
        try {
            URL resource = getClass().getClassLoader().getResource("settings/settings.ini");
            ini = new Ini(new File(resource.toURI()));
        } catch (NullPointerException | IOException | URISyntaxException e) {
            throw new IllegalStateException("Configuration File is missing!", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                storeConfig();
            }
        });
    }

    public static Path getStartLocation(String side) {
        String location = instance.ini.get(LOCATION_SECTION).get(side);
        return Paths.get(location);
    }

    public static String getLanguage() {
        return instance.ini.get(LANGUAGE).get(LANGUAGE);
    }

    public static Locale getLocale() {
        return new Locale(instance.ini.get(LANGUAGE).get(LANGUAGE));
    }

    public static String setLanguage(Locale locale) {
        final String country = locale.getLanguage();
        return instance.ini.get(LANGUAGE).replace(LANGUAGE, country);
    }

    public static String setLanguage(String locale) {
        final String country = (String) languages.get(locale);
        return instance.ini.get(LANGUAGE).replace(LANGUAGE, country);
    }

    public static void setStartLocation(String side, Path newLocation) {
        instance.ini.get(LOCATION_SECTION).replace(side, newLocation.toString());
    }

    public static void storeConfig() {
        try {
            instance.ini.store();
            LOG.info("Configuration has been saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
