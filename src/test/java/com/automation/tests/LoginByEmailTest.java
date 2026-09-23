package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.AuthModalPage;
import com.automation.pages.SearchPage;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Тест: Форма авторизации по Email и паролю.
 *
 * Сценарий (негативный — некорректные данные):
 *   1. Открыть главную страницу
 *   2. Нажать «Войти» в шапке
 *   3. Переключиться на вход по Email
 *   4. Ввести некорректные данные
 *   5. Принять согласие на обработку данных
 *   6. Нажать «Войти»
 *   7. Проверить появление сообщения об ошибке
 */
@Epic("vodovoz.ru")
@Feature("Авторизация")
@DisplayName("Авторизация по Email")
@Tag("regression")
class LoginByEmailTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(LoginByEmailTest.class);

    private static final String INVALID_EMAIL    = "test_qa_auto@example.com";
    private static final String INVALID_PASSWORD = "WrongPass123!";

    private final SearchPage     searchPage = new SearchPage();
    private final AuthModalPage  authPage   = new AuthModalPage();

    @BeforeEach
    void openMainPage() {
        searchPage.openPage();
        Selenide.sleep(2000);
    }

    @Test
    @Story("Негативный сценарий: неверный Email/пароль")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Вход с неверным Email → проверка сообщения об ошибке")
    @Description("""
            Негативный E2E-сценарий:
            1. Открыть главную страницу vodovoz.ru
            2. Нажать кнопку «Войти» в шапке сайта
            3. Дождаться открытия модального окна авторизации
            4. Переключиться на вход по Email
            5. Ввести некорректный email: test_qa_auto@example.com
            6. Ввести неверный пароль: WrongPass123!
            7. Принять условия обработки персональных данных
            8. Нажать кнопку «Войти»
            9. Проверить, что отображается сообщение об ошибке авторизации
            """)
    void testLoginWithInvalidEmailShowsError() {
        // Шаг 1: Открыть форму входа
        openLoginModal();

        // Шаг 2: Переключиться на Email-вход
        authPage.switchToEmailLogin();
        Selenide.sleep(1000);

        // Шаг 3: Заполнить форму
        fillLoginForm();

        // Шаг 4: Отправить форму
        authPage.clickSubmit();

        // Шаг 5: Проверить ошибку
        verifyErrorMessage();
    }

    @Step("Открыть модальное окно авторизации")
    private void openLoginModal() {
        log.info("Открываю форму авторизации");
        authPage.clickLoginInHeader();
        authPage.waitForModalToOpen();
    }

    @Step("Заполнить форму: Email={email}, Password=***")
    private void fillLoginForm() {
        String email = INVALID_EMAIL;
        log.info("Заполняю форму: email={}", email);
        authPage.enterEmail(email);
        authPage.enterPassword(INVALID_PASSWORD);
        authPage.acceptPersonalDataConsent();
    }

    @Step("Проверить сообщение об ошибке авторизации")
    private void verifyErrorMessage() {
        log.info("Проверяю сообщение об ошибке...");
        authPage.errorMessageShouldBeVisible();
        String errorText = authPage.getErrorText();
        log.info("Текст ошибки: '{}'", errorText);
    }

    @Test
    @DisplayName("Успешная авторизация по Email и паролю")
    @Description("Открыть модалку -> войти через почту -> ввести валидные креды -> согласие -> войти -> модалка закрыта, шапка авторизована, /personal/ открывается без редиректа на вход")
    public void testLoginWithValidCredentials() {
        String email = com.automation.base.ConfigReader.get("auth.email");
        String password = com.automation.base.ConfigReader.get("auth.password");
        org.junit.jupiter.api.Assumptions.assumeTrue(
                !email.contains("placeholder"),
                "Не переданы реальные креды тестового аккаунта (-Dauth.email / -Dauth.password) — тест пропускается");

        com.codeborne.selenide.Selenide.open("/");
        openLoginModal();
        authPage.switchToEmailLogin()
                .enterEmail(email)
                .enterPassword(password)
                .acceptPersonalDataConsent()
                .clickSubmit();
        authPage.shouldBeLoggedIn();

        // Контроль: личный кабинет открывается без редиректа на форму входа
        com.codeborne.selenide.Selenide.open("/personal/");
        org.junit.jupiter.api.Assertions.assertTrue(
                com.codeborne.selenide.WebDriverRunner.url().contains("/personal/"),
                "После входа /personal/ должен открываться без редиректа на авторизацию");
    }
}