package com.automation.utils;

import com.codeborne.selenide.Selenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/** Utility class for taking and saving screenshots. */
public final class ScreenshotUtils {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOTS_DIR = "screenshots";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {}

    /**
     * Takes a screenshot and saves it to the screenshots/ directory.
     *
     * @param testName name of the test (used in the filename)
     * @return path to the saved screenshot, or null on failure
     */
    public static String take(String testName) {
        try {
            Path dir = Paths.get(SCREENSHOTS_DIR);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(FORMATTER);
            String filename = testName + "_" + timestamp;

            // Selenide 7.x: screenshot() returns the path as String, not File
            String screenshotPath = Selenide.screenshot(filename);
            if (screenshotPath != null) {
                log.info("Screenshot saved: {}", screenshotPath);
                return screenshotPath;
            }
        } catch (Exception e) {
            log.warn("Failed to take screenshot for '{}': {}", testName, e.getMessage());
        }
        return null;
    }
}
