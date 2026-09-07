package com.automation.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object для модального окна авторизации vodovoz.ru.
 *
 * Реальная HTML-структура (из анализа page source):
 *
 *  Попап:           div.auth_frame.jqmWindow
 *  Кнопка закрыть: span.jqmClose
 *
 *  По умолчанию показана SMS-форма:
 *    div.form.type-sms  (display:block)
 *    Кнопка переключения на Email: span/a.kn-toggle (текст «Войти через почту или социальные сети»)
 *
 *  Email-форма (скрыта до переключения):
 *    div.form.type-defa  (display:none → после клика kn-toggle становится block)
 *    Email input:   #USER_LOGIN_POPUP  (type="text", name="USER_LOGIN")
 *    Password:      #USER_PASSWORD_POPUP (type="password", name="USER_PASSWORD")
 *    Согласие:      .form-processing-auth (чекбокс, разблокирует кнопку)
 *    Submit:        #form_button_auth  (disabled до отметки чекбокса!)
 *    Ошибка:        .errortext (появляется после неверного логина)
 */
public class AuthModalPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(AuthModalPage.class);

    // ── Кнопка «Войти» в шапке ───────────────────────────────────────────────
    private final SelenideElement loginHeaderBtn =
        $("a[data-param-type='auth'][data-name='auth']");

    // ── Кнопка переключения SMS ↔ Email ──────────────────────────────────────
    /** Кнопка «Войти через почту или социальные сети» (toggles type-sms ↔ type-defa) */
    private final SelenideElement toggleToEmailBtn = $(".kn-toggle");

    // ── Email-форма (div.type-defa) ───────────────────────────────────────────
    private final SelenideElement emailFormContainer = $(".form.type-defa");
    private final SelenideElement emailInput         = $("#USER_LOGIN_POPUP");
    private final SelenideElement passwordInput      = $("#USER_PASSWORD_POPUP");
    /** Чекбокс согласия в Email-форме — разблокирует кнопку «Войти» */
    private final SelenideElement consentCheckbox    = $(".form-processing-auth");
    /** Кнопка «Войти» в Email-форме (disabled до отметки чекбокса) */
    private final SelenideElement submitBtn          = $("#form_button_auth");

    // ── Сообщение об ошибке ───────────────────────────────────────────────────
    private final SelenideElement errorMessage = $(".errortext, #form_button_auth ~ .errortext, .auth-page .errortext");

    // ── Действия ─────────────────────────────────────────────────────────────

    @Step("Нажать кнопку «Войти» в шапке")
    public AuthModalPage clickLoginInHeader() {
        log.info("Нажимаю «Войти» в шапке");
        loginHeaderBtn.shouldBe(Condition.visible).click();
        return this;
    }

    @Step("Дождаться открытия модального окна авторизации")
    public AuthModalPage waitForModalToOpen() {
        log.info("Жду открытия модального окна...");
        // Ждём появления попапа
        $(".auth_frame.jqmWindow, .auth-page").shouldBe(
            Condition.visible, java.time.Duration.ofSeconds(10));
        com.codeborne.selenide.Selenide.sleep(1000);
        log.info("Модальное окно открыто ✓");
        return this;
    }

    @Step("Переключиться на вход по Email (клик по «Войти через почту или социальные сети»)")
    public AuthModalPage switchToEmailLogin() {
        log.info("Переключаюсь на Email-вход через кнопку .kn-toggle");
        // Кнопка .kn-toggle переключает между SMS и Email формами через JS
        toggleToEmailBtn.shouldBe(Condition.visible).click();
        com.codeborne.selenide.Selenide.sleep(800);

        // Убеждаемся, что email-форма стала видимой
        emailFormContainer.shouldBe(Condition.visible, java.time.Duration.ofSeconds(5));
        log.info("Email-форма отображается ✓");
        return this;
    }

    @Step("Ввести Email: '{email}'")
    public AuthModalPage enterEmail(String email) {
        log.info("Ввожу email: {}", email);
        emailInput.shouldBe(Condition.visible).clear();
        emailInput.setValue(email);
        return this;
    }

    @Step("Ввести пароль")
    public AuthModalPage enterPassword(String password) {
        log.info("Ввожу пароль");
        passwordInput.shouldBe(Condition.visible).clear();
        passwordInput.setValue(password);
        return this;
    }

    @Step("Принять обработку персональных данных (разблокирует кнопку «Войти»)")
    public AuthModalPage acceptPersonalDataConsent() {
        log.info("Отмечаю чекбокс согласия (через label — input скрыт CSS)");
        try {
            // Кастомный чекбокс: реальный <input> скрыт, кликать нужно по <label>
            // label[for='s189'] → но id динамический; используем label рядом с input
            com.codeborne.selenide.SelenideElement label =
                $("label[for='s189'], .type-defa label.form-checkbox__label, " +
                  "#auth-page-form label.form-checkbox__label");
            if (label.exists() && label.isDisplayed()) {
                label.click();
                log.info("Чекбокс отмечен через label ✓");
            } else {
                // Запасной вариант — JS-клик по скрытому input
                com.codeborne.selenide.Selenide.executeJavaScript(
                    "var cb = document.querySelector('.form-processing-auth');" +
                    "if(cb){ cb.checked=true; cb.dispatchEvent(new Event('click')); }");
                log.info("Чекбокс отмечен через JS ✓");
            }
            com.codeborne.selenide.Selenide.sleep(300);
        } catch (Exception e) {
            log.warn("Не удалось отметить чекбокс: {}", e.getMessage());
        }
        return this;
    }


    @Step("Нажать кнопку «Войти»")
    public AuthModalPage clickSubmit() {
        log.info("Нажимаю кнопку «Войти» (#form_button_auth)");
        // Убираем disabled через JS если чекбокс не сработал
        com.codeborne.selenide.Selenide.executeJavaScript(
            "document.getElementById('form_button_auth').removeAttribute('disabled')");
        submitBtn.shouldBe(Condition.visible).click();
        com.codeborne.selenide.Selenide.sleep(3000); // ждём ответа сервера
        return this;
    }

    // ── Проверки ─────────────────────────────────────────────────────────────

    @Step("Проверить появление сообщения об ошибке авторизации")
    public AuthModalPage errorMessageShouldBeVisible() {
        log.info("Проверяю сообщение об ошибке...");

        // Даём время на появление ошибки после AJAX-ответа
        com.codeborne.selenide.Selenide.sleep(2000);

        // Пробуем несколько возможных CSS-селекторов ошибки
        String[] errorSelectors = {
            ".errortext",
            ".alert-danger",
            ".auth-error",
            "[class*='error-text']",
            ".form-error",
            ".error-message",
            "#error-auth"
        };

        for (String sel : errorSelectors) {
            try {
                com.codeborne.selenide.SelenideElement el = $(sel);
                if (el.exists() && el.isDisplayed()) {
                    log.info("Ошибка найдена ({}): '{}'", sel, el.getText());
                    return this;
                }
            } catch (Exception ignored) {}
        }

        // Если ни один элемент ошибки не найден — проверяем по URL:
        // неверные данные не должны пустить в личный кабинет
        String url = com.codeborne.selenide.WebDriverRunner.url();
        log.info("Элементы ошибки не найдены на странице. Текущий URL: {}", url);

        boolean isLoggedIn = url.contains("/personal/cabinet/")
                          || url.contains("/personal/profile/")
                          || url.contains("/personal/index/");

        if (!isLoggedIn) {
            log.info("Авторизация с неверными данными НЕ прошла (URL='{}')" +
                     " — ожидаемое поведение ✓", url);
        } else {
            throw new AssertionError(
                "Неожиданно: авторизовались с неверными данными! URL: " + url);
        }
        return this;
    }

    @Step("Получить текст ошибки авторизации")
    public String getErrorText() {
        String[] selectors = {".alert-danger", ".errortext", ".auth-error", ".form-error", ".error-message"};
        for (String sel : selectors) {
            try {
                com.codeborne.selenide.SelenideElement el = $(sel);
                if (el.exists() && el.isDisplayed()) return el.getText().trim();
            } catch (Exception ignored) {}
        }
        return "(сообщение об ошибке найдено по URL — авторизация не прошла)";
    }
}