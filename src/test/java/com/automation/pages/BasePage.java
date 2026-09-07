package com.automation.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;
import static com.codeborne.selenide.Selenide.webdriver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for all Page Objects.
 *
 * <p>Provides:
 * <ul>
 *   <li>Common navigation helpers (scroll, page load)</li>
 *   <li>Page-level assertion helpers (title, URL)</li>
 *   <li>Allure {@code @Step} annotations on every action</li>
 * </ul>
 *
 * <p>Every concrete page must extend this class.
 */
public abstract class BasePage {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Returns the current page title. */
    @Step("Get page title")
    public String getPageTitle() {
        return title();
    }

    /** Returns the current page URL. */
    @Step("Get current URL")
    public String getCurrentUrl() {
        return webdriver().driver().url();
    }

    @Step("Scroll to top of page")
    public void scrollToTop() {
        Selenide.executeJavaScript("window.scrollTo(0, 0)");
    }

    @Step("Scroll to bottom of page")
    public void scrollToBottom() {
        Selenide.executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    @Step("Refresh page")
    public void refresh() {
        Selenide.refresh();
    }

    @Step("Navigate back")
    public void goBack() {
        Selenide.back();
    }

    // ── Page-load ─────────────────────────────────────────────────────────────

    /**
     * Waits until {@code document.readyState === 'complete'}.
     * Selenide waits automatically on most actions; use this
     * only when needed after custom JS navigation.
     */
    @Step("Wait for page to load")
    public void waitForPageLoad() {
        Selenide.Wait().until(driver ->
                "complete".equals(
                        ((org.openqa.selenium.JavascriptExecutor) driver)
                                .executeScript("return document.readyState")));
    }

    // ── Soft assertion helpers ────────────────────────────────────────────────

    /**
     * Asserts the browser tab title equals {@code expected}.
     *
     * @param expected expected page title
     */
    @Step("Page title should be: {expected}")
    public void shouldHaveTitle(String expected) {
        assertEquals(expected, title(),
                "Page title mismatch");
    }

    /**
     * Asserts the current URL contains {@code part}.
     *
     * @param part substring expected in the URL
     */
    @Step("URL should contain: {part}")
    public void urlShouldContain(String part) {
        assertTrue(getCurrentUrl().contains(part),
                "URL should contain '" + part + "' but was: " + getCurrentUrl());
    }
}

