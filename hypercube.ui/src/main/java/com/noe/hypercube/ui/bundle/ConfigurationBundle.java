package com.noe.hypercube.ui.bundle;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationBundle {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationBundle.class);

    private static final SimpleStringProperty activeLanguage = new SimpleStringProperty();
    private static final ConfigurationBundle instance = new ConfigurationBundle();
    private static final String LOCATION_SECTION = "location";
    private static final String LANGUAGE = "language";
    private final BidiMap<String, String> languages = new DualHashBidiMap<>();
    private final Ini ini;

    private ConfigurationBundle() {
        languages.put("Magyar", "hu");
        languages.put("English", "en");
        try {
            ini = new Ini(new File("./settings/settings.ini"));
        } catch (NullPointerException | IOException e) {
            throw new IllegalStateException("Configuration File is missing!", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                storeConfig();
            }
        });
    }

    public static Path getStartLocation(final String side) {
        String location = instance.ini.get(LOCATION_SECTION).get(side);
        return Paths.get(location);
    }

    public static String getLanguage() {
        return instance.ini.get(LANGUAGE).get(LANGUAGE);
    }

    public static String getLanguageLongName() {
        return instance.languages.getKey(getLanguage());
    }

    public static Locale getLocale() {
        return new Locale(instance.ini.get(LANGUAGE).get(LANGUAGE));
    }

    public static void setLanguage(final Locale locale) {
        final String country = locale.getLanguage();
        instance.ini.get(LANGUAGE).replace(LANGUAGE, country);
        activeLanguage.set(country);
    }

    public static void setLanguage(final String locale) {
        final String country = instance.languages.get(locale);
        instance.ini.get(LANGUAGE).replace(LANGUAGE, country);
        activeLanguage.set(country);
    }

    public static void setStartLocation(final String side, final Path newLocation) {
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

    public static String getActiveLanguage() {
        return activeLanguage.get();
    }

    public static StringProperty activeLanguageProperty() {
        return activeLanguage;
    }
}
