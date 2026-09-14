package com.automation.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import com.codeborne.selenide.WebDriverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

/**
 * Page Object для модального окна «Написать отзыв» (feedback form, id=12).
 *
 * <h3>Архитектура формы на vodovoz.ru (выявлена через DOM-инспектор):</h3>
 * <ul>
 *   <li>Кнопка открытия: {@code [data-event='jqm'][data-name='feedback']} на /reviews/</li>
 *   <li>Контейнер модалки: {@code div.feedback_frame} (jqModal добавляет class "show")</li>
 *   <li>Форма: {@code form[name='aspro_lite_feedback_s1']} action="/ajax/form.php?id=12"</li>
 *   <li><b>Submit изначально disabled</b> — активируется чекбоксом Processing_of_personal_data</li>
 *   <li>Валидация: jQuery Validate → {@code label.error} у каждого невалидного поля</li>
 *   <li>Обязательные: #POPUP_RATING (скрытый), #POPUP_NAME, #POPUP_EMAIL, textarea#POPUP_MESSAGE</li>
 * </ul>
 */
public class FeedbackModal {

    private static final Logger log = LoggerFactory.getLogger(FeedbackModal.class);

    // ── Кнопка открытия ──────────────────────────────────────────────────────
    private final SelenideElement openButton =
            $("[data-event='jqm'][data-name='feedback']");

    // ── Поля формы (ждём их видимости как индикатор открытия) ────────────────
    private final SelenideElement nameInput     = $("div.feedback_frame #POPUP_NAME");
    private final SelenideElement emailInput    = $("div.feedback_frame #POPUP_EMAIL");
    private final SelenideElement messageInput  = $("div.feedback_frame #POPUP_MESSAGE");

    // ── Чекбокс согласия (скрытый) ───────────────────────────────────────────
    // Unchecked → submit disabled. JS-клик активирует кнопку.
    private final SelenideElement consentCheckbox =
            $("div.feedback_frame input[name='Processing_of_personal_data']");

    // ── Кнопка отправки (изначально disabled=true) ────────────────────────────
    private final SelenideElement submitButton  = $("div.feedback_frame #form_button_form");

    // ── Ошибки jQuery Validate ────────────────────────────────────────────────
    private final ElementsCollection validationErrors = $$("div.feedback_frame label.error");

    /**
     * Открывает модальное окно «Написать отзыв».
     * Ждёт появления поля «Имя» как индикатора загрузки AJAX-контента формы.
     * Активирует чекбокс согласия (без него Submit disabled).
     */
    @Step("Открыть форму «Написать отзыв»")
    public FeedbackModal open() {
        log.info("Кликаю 'Написать отзыв'");
        openButton.shouldBe(visible, Duration.ofSeconds(10));
        jsClick(openButton);
        // Ждём появления поля «Имя» — признак, что форма загружена через AJAX
        nameInput.shouldBe(visible, Duration.ofSeconds(15));
        log.info("Форма отзыва открыта");
        // Активируем чекбокс согласия → Submit становится enabled
        jsClick(consentCheckbox);
        sleep(500);
        return this;
    }

    @Step("Ввести имя: {name}")
    public FeedbackModal enterName(String name) {
        nameInput.shouldBe(visible, Duration.ofSeconds(5)).setValue(name);
        return this;
    }

    @Step("Ввести email: {email}")
    public FeedbackModal enterEmail(String email) {
        emailInput.shouldBe(visible, Duration.ofSeconds(5)).setValue(email);
        return this;
    }

    @Step("Ввести текст отзыва")
    public FeedbackModal enterMessage(String message) {
        messageInput.shouldBe(visible, Duration.ofSeconds(5)).setValue(message);
        return this;
    }

    /**
     * Нажимает Submit через JS, снимая disabled перед кликом.
     * jQuery Validate срабатывает на клиенте → добавляет label.error.
     */
    @Step("Нажать «Отправить»")
    public FeedbackModal submit() {
        submitButton.shouldBe(visible, Duration.ofSeconds(5));
        executeJavaScript(
            "var btn = arguments[0]; btn.removeAttribute('disabled'); btn.click();",
            submitButton.getWrappedElement()
        );
        sleep(800); // дать jQuery Validate время добавить label.error
        return this;
    }

    /**
     * Проверяет, что jQuery Validate добавил хотя бы один видимый label.error.
     */
    @Step("Ошибки валидации должны быть видимы")
    public FeedbackModal shouldShowValidationErrors() {
        validationErrors.first().shouldBe(visible, Duration.ofSeconds(10));
        log.info("Ошибки валидации: {} шт. ✓", validationErrors.size());
        return this;
    }

    private void jsClick(SelenideElement el) {
        ((JavascriptExecutor) WebDriverRunner.getWebDriver())
                .executeScript("arguments[0].click();", el.getWrappedElement());
    }
}
