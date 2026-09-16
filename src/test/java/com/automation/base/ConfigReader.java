package com.automation.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Читает тестовые данные из {@code src/test/resources/config.properties}.
 *
 * <p>Приоритет: системное свойство (-Dkey=value) > config.properties.
 * Если файл не найден или ключ отсутствует — бросает {@link IllegalStateException}.
 *
 * <p>Пример использования:
 * <pre>{@code
 *   String query = ConfigReader.get("search.query.default"); // "вода"
 * }</pre>
 */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                    "config.properties не найден в classpath (src/test/resources/config.properties)");
            }
            PROPS.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка загрузки config.properties", e);
        }
    }

    private ConfigReader() {}

    /**
     * Возвращает значение ключа.
     *
     * <p>Приоритет: системное свойство ({@code -Dkey=value}) → config.properties.
     * Trim применяется к результату.
     *
     * @param key  ключ свойства
     * @return     значение (не пустое, trim)
     * @throws IllegalStateException если ключ не найден ни в одном источнике
     */
    public static String get(String key) {
        // 1. Системное свойство (передаётся через -Dkey=value)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        // 2. config.properties
        String fileProp = PROPS.getProperty(key);
        if (fileProp != null && !fileProp.isBlank()) {
            return fileProp.trim();
        }
        throw new IllegalStateException(
            "Ключ '" + key + "' не найден ни в -D системных свойствах, " +
            "ни в config.properties");
    }

    /**
     * Возвращает значение ключа или {@code defaultValue} если ключ не найден.
     */
    public static String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (IllegalStateException e) {
            return defaultValue;
        }
    }
}
