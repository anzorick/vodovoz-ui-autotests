package com.automation.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Page Object for the Login page.
 *
 * <p>Demonstrates the POM pattern:
 * <ul>
 *   <li>Locators are private fields — only this class knows the selectors</li>
 *   <li>Actions return {@code this} for fluent chaining</li>
 *   <li>Every public method is annotated with {@code @Step} for Allure reports</li>
 *   <li>Assertion helpers encapsulate {@code shouldBe} / {@code shouldHave} calls</li>
 * </ul>
 */
public class LoginPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────
    // Kept private: tests never access raw elements directly

    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement loginButton   = $("button[type='submit']");
    private final SelenideElement errorMessage  = $(".error-message");

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Opens the login page.
     * Named {@code openPage} to avoid shadowing Selenide's static {@code open()}.
     *
     * @return this page (fluent)
     */
    @Step("Open login page")
    public LoginPage openPage() {
        open("/login");
        return this;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        usernameInput.shouldBe(visible).setValue(username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        // password is intentionally not logged/shown in the step name
        passwordInput.shouldBe(visible).setValue(password);
        return this;
    }

    @Step("Click 'Login' button")
    public LoginPage clickLogin() {
        loginButton.shouldBe(enabled).click();
        return this;
    }

    /**
     * Convenience method: fills credentials and submits the form.
     *
     * @param username login
     * @param password password
     */
    @Step("Login as: {username}")
    public void login(String username, String password) {
        enterUsername(username)
                .enterPassword(password)
                .clickLogin();
    }

    // ── Assertion helpers ─────────────────────────────────────────────────────
    // Keep assertions inside the Page Object so tests stay readable

    /** Asserts the error message block is visible. */
    @Step("Error message should be displayed")
    public LoginPage errorShouldBeVisible() {
        errorMessage.shouldBe(visible);
        return this;
    }

    /** Asserts the error message contains {@code expectedText}. */
    @Step("Error message should contain: {expectedText}")
    public LoginPage errorShouldContain(String expectedText) {
        errorMessage.shouldHave(text(expectedText));
        return this;
    }

    /** Asserts the login form is fully loaded and ready. */
    @Step("Login page should be loaded")
    public LoginPage shouldBeLoaded() {
        usernameInput.shouldBe(visible);
        passwordInput.shouldBe(visible);
        loginButton.shouldBe(visible);
        return this;
    }

    // ── Raw getters (for legacy assertions if needed) ─────────────────────────

    public boolean isErrorDisplayed() {
        return errorMessage.isDisplayed();
    }

    public String getErrorText() {
        return errorMessage.getText();
    }
}

