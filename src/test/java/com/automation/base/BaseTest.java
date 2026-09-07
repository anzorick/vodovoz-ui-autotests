package com.automation.base;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Base class for all UI tests.
 * Гарантирует видимое окно Chrome на весь экран.
 */
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeEach
    void setUp(TestInfo testInfo) {
        log.info("=== Starting test: {} ===", testInfo.getDisplayName());

        String  browser  = com.automation.config.Configuration.browser();
        boolean headless = com.automation.config.Configuration.headless();
        long    timeout  = com.automation.config.Configuration.timeout() * 1000L;
        String  baseUrl  = com.automation.config.Configuration.baseUrl();

        log.info(">>> Browser: {} | headless: {} | baseUrl: {}", browser, headless, baseUrl);

        // ── WebDriver binary ─────────────────────────────────────────────────
        switch (browser.toLowerCase()) {
            case "firefox" -> io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
            case "edge"    -> io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
            default        -> io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
        }

        // ── ChromeOptions — гарантированное отображение окна ─────────────────
        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();

            if (headless) {
                // ── Режим CI (headless=true, нет дисплея) ────────────────────
                options.addArguments(
                    "--headless=new",           // современный headless-режим Chrome 112+
                    "--window-size=1920,1080",  // виртуальный размер окна для скриншотов
                    "--no-sandbox",             // обязательно в Linux CI без root
                    "--disable-dev-shm-usage",  // /dev/shm мал в Docker/CI → используем tmpfs
                    "--disable-gpu",            // без GPU в headless-среде
                    "--disable-extensions",
                    "--disable-infobars",
                    "--disable-notifications"
                );
            } else {
                // ── Локальный режим (headless=false, видимый браузер) ─────────
                options.addArguments(
                    "--start-maximized",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--foreground",
                    "--disable-infobars",
                    "--disable-notifications"
                );
                // Убираем баннер «Chrome управляется автоматическим ПО»
                options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
                options.setExperimentalOption("useAutomationExtension", false);
            }

            com.codeborne.selenide.Configuration.browserCapabilities = options;
        }


        // ── Selenide configuration ────────────────────────────────────────────
        com.codeborne.selenide.Configuration.browser         = browser;
        com.codeborne.selenide.Configuration.headless        = headless;
        com.codeborne.selenide.Configuration.timeout         = timeout;
        com.codeborne.selenide.Configuration.baseUrl         = baseUrl;
        com.codeborne.selenide.Configuration.browserSize     = "1920x1080";
        // В CI (headless) — НЕ держим браузер открытым (дисплея нет, ресурсы тратятся впустую)
        // Локально (headless=false) — держим для визуального контроля
        com.codeborne.selenide.Configuration.holdBrowserOpen = !headless;
        com.codeborne.selenide.Configuration.screenshots     = true;
        com.codeborne.selenide.Configuration.reportsFolder   = "screenshots";

        // ── Allure listener ───────────────────────────────────────────────────
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(true));
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        log.info("=== Finished test: {} ===", testInfo.getDisplayName());
        SelenideLogger.removeListener("AllureSelenide");
        Selenide.closeWebDriver();
    }
}