package com.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads configuration from config.properties.
 * System properties (e.g. -Dbrowser=firefox) override file values.
 */
public class Configuration {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Configuration.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties", e);
        }
    }

    private Configuration() {}

    /**
     * Returns a property value.
     * Priority: system property (-Dkey=value) → config.properties → null.
     *
     * NOTE: Maven passes unresolved ${key} as an empty string "".
     * We treat blank system properties as "not set" so they don't
     * shadow the values from config.properties.
     */
    public static String get(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;  // explicitly set by user via -Dkey=value
        }
        return PROPS.getProperty(key);  // fallback to config.properties
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Convenience methods
    public static String baseUrl()   { return get("base.url", "https://vodovoz.ru"); }
    public static String browser()   { return get("browser", "chrome"); }
    /** headless is FALSE by default — browser always opens visibly unless -Dheadless=true is passed */
    public static boolean headless() { return getBoolean("headless", false); }
    public static int timeout()      { return getInt("browser.timeout", 15); }

}
