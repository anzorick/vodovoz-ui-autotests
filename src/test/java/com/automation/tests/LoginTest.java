package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for the Login page.
 *
 * <p>Notice how tests are written at a <em>business level</em>:
 * no raw Selenide calls, no selectors — only Page Object methods.
 * This is the key benefit of POM.
 */
@Epic("Authentication")
@Feature("Login")
@DisplayName("Login Page Tests")
class LoginTest extends BaseTest {

    // The Page Object is created once per test class.
    // Because Selenide lazily initialises elements, this is safe.
    private final LoginPage loginPage = new LoginPage();

    @BeforeEach
    void openLoginPage() {
        loginPage.openPage()
                 .shouldBeLoaded();   // guard: verify page is ready
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @Story("Successful login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User logs in with valid credentials and is redirected to the dashboard")
    @DisplayName("Login succeeds with valid credentials")
    void testSuccessfulLogin() {
        loginPage.login("admin", "secret123");
        // After login → verify redirect, e.g.:
        // new HomePage().shouldBeLoaded();
        loginPage.urlShouldContain("/dashboard");
    }

    // ── Negative cases ────────────────────────────────────────────────────────

    @Test
    @Story("Failed login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid credentials should show an error message")
    @DisplayName("Login fails with invalid credentials")
    void testLoginWithInvalidCredentials() {
        loginPage.login("wrong_user", "wrong_pass");
        loginPage.errorShouldBeVisible();
    }

    @ParameterizedTest(name = "username=''{0}'' / password=''{1}''")
    @Story("Failed login")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Login fails with various invalid credential combinations")
    @CsvSource({
            "'',           password123",
            "user@test.com, ''",
            "invalid,      invalid"
    })
    void testLoginWithInvalidCredentialsParametrized(String username, String password) {
        loginPage.login(username, password);
        loginPage.errorShouldBeVisible();
    }
}

