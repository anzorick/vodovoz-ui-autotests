package com.automation.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.sleep;

/**
 * Page Object для модального окна «Заказать звонок» (callback).
 *
 * <h3>Архитектура формы на vodovoz.ru (выявлена через DOM-инспектор):</h3>
 * <ul>
 *   <li>Кнопка открытия: {@code div[data-name='callback']} — видима только при ≥1200px (class hide-1200).
 *       Браузер настраивается на 1920×1080 в BaseTest → кнопка видима.</li>
 *   <li>Модалка открывается механизмом jqModal (data-event="jqm").</li>
 *   <li>Контейнер модалки: {@code div.callback_frame} (получает class "show" при открытии).</li>
 *   <li>Submit-кнопка изначально <b>disabled</b> — активируется только после того,
 *       как пользователь принимает чекбокс «Согласие на обработку персональных данных»
 *       ({@code input[name='Processing_of_personal_data']}).</li>
 *   <li>Валидация: jQuery Validate добавляет {@code label.error} рядом с каждым невалидным полем.</li>
 *   <li>Форма содержит CAPTCHA — реальную отправку не тестируем.</li>
 * </ul>
 */
public class CallbackModal {

    // ─── Кнопка открытия формы ─────────────────────────────────────────────────
    // div с data-name="callback" (class hide-1200 — видима только при ≥1200px).
    // Используется первый найденный элемент (в шапке), т.к. browserSize=1920x1080.
    private final SelenideElement openFormButton =
            $("div[data-name='callback'].phones__callback");

    // ─── Контейнер модального окна ─────────────────────────────────────────────
    // После клика jqModal добавляет class "show" к div.callback_frame
    private final SelenideElement modalContainer = $("div.callback_frame");

    // ─── Поля формы ────────────────────────────────────────────────────────────
    // Подтверждено через DOM-инспектор: id=POPUP_NAME, name=form_text_65
    private final SelenideElement nameInput =
            $("div.callback_frame #POPUP_NAME");

    // Подтверждено: id=POPUP_PHONE, name=form_text_66, type=tel
    private final SelenideElement phoneInput =
            $("div.callback_frame #POPUP_PHONE");

    // ─── Чекбокс согласия — делает Submit активным ─────────────────────────────
    // input[name='Processing_of_personal_data'] — по умолчанию unchecked → submit disabled
    private final SelenideElement personalDataCheckbox =
            $("div.callback_frame input[name='Processing_of_personal_data']");

    // ─── Кнопка отправки ───────────────────────────────────────────────────────
    // id=form_button_form, name=web_form_submit, type=submit — изначально disabled!
    private final SelenideElement submitButton =
            $("div.callback_frame #form_button_form");

    // ─── Элементы ошибок валидации ─────────────────────────────────────────────
    // jQuery Validate добавляет <label class="error" for="FIELD_ID"> рядом с невалидными полями.
    // Конкретные id ошибок: POPUP_NAME-error, POPUP_PHONE-error, captcha_word-error
    private final ElementsCollection validationErrors =
            $$("div.callback_frame label.error");

    /**
     * Открывает модальное окно «Заказать звонок».
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>JS-клик по кнопке div[data-name='callback'] (обходит возможные перекрытия слоями).</li>
     *   <li>Ждёт появления контейнера div.callback_frame.</li>
     *   <li>Активирует чекбокс согласия на обработку персональных данных через JS
     *       (чекбокс технически hidden, но управляет атрибутом disabled у Submit).</li>
     * </ol>
     */
    @Step("Открыть форму заказа звонка")
    public CallbackModal open() {
        // Ждём, пока кнопка появится в DOM
        openFormButton.shouldBe(visible, Duration.ofSeconds(10));
        // JS-клик обходит перекрытие другими слоями (маркетинг-попап, overlay)
        jsClick(openFormButton);
        // Ждём открытия модалки
        modalContainer.shouldBe(visible, Duration.ofSeconds(10));
        // Активируем чекбокс «Согласие на обработку персональных данных»,
        // иначе Submit остаётся disabled и jQuery Validate не запускается
        jsClick(personalDataCheckbox);
        sleep(300); // даём JS время снять disabled с кнопки
        return this;
    }

    /**
     * Вводит имя в поле «Ваше имя».
     *
     * @param name значение для ввода (пустая строка — тест на пустую форму)
     */
    @Step("Ввести имя: {name}")
    public CallbackModal enterName(String name) {
        nameInput.shouldBe(visible, Duration.ofSeconds(10)).setValue(name);
        return this;
    }

    /**
     * Вводит телефон в поле «Телефон».
     *
     * @param phone значение для ввода (например, "123" — заведомо невалидный телефон)
     */
    @Step("Ввести телефон: {phone}")
    public CallbackModal enterPhone(String phone) {
        SelenideElement phoneField = phoneInput.shouldBe(visible, Duration.ofSeconds(10));
        phoneField.click();
        phoneField.sendKeys(phone); // посимвольный ввод для корректной работы маски
        return this;
    }

    /**
     * Нажимает кнопку отправки формы через JS-клик.
     * <p>
     * После активации чекбокса в {@link #open()} Submit уже enabled.
     * JS-клик используется для надёжности на случай перекрытия элементами.
     */
    @Step("Нажать кнопку отправки формы")
    public CallbackModal submit() {
        submitButton.shouldBe(visible, Duration.ofSeconds(10));
        // jQuery Validate перехватывает submit → запускает валидацию
        // JS-клик обходит перекрытие слоями
        jsClick(submitButton);
        sleep(500); // даём jQuery Validate время добавить label.error
        return this;
    }

    /**
     * Проверяет, что появились ошибки валидации (label.error от jQuery Validate).
     * <p>
     * После отправки пустой/невалидной формы jQuery Validate добавляет
     * {@code <label class="error" for="POPUP_NAME">Поле обязательно!</label>} рядом
     * с каждым невалидным полем (POPUP_NAME, POPUP_PHONE, captcha_word).
     */
    @Step("Проверить, что появились ошибки валидации")
    public CallbackModal shouldShowValidationErrors() {
        // Хотя бы один label.error должен быть видим
        validationErrors.first().shouldBe(visible, Duration.ofSeconds(10));
        return this;
    }

    // ─── Вспомогательный метод ────────────────────────────────────────────────

    /**
     * Выполняет клик через JavaScript для обхода перекрытий элементами.
     */
    private void jsClick(SelenideElement element) {
        ((JavascriptExecutor) WebDriverRunner.getWebDriver())
                .executeScript("arguments[0].click();", element.getWrappedElement());
    }
    // Поле CAPTCHA (код с картинки)
    private final SelenideElement captchaInput = $("div.callback_frame input[name='captcha_word'], input[name='captcha_word']");

    @Step("Ввести код с картинки: {code}")
    public CallbackModal enterCaptcha(String code) {
        captchaInput.shouldBe(visible, Duration.ofSeconds(10)).setValue(code);
        return this;
    }
    @Step("Проверить, что отправка отклонена: модалка открыта, капча на месте, благодарности нет")
    public CallbackModal shouldNotBeSubmitted() {
        $("div.callback_frame").shouldBe(visible, Duration.ofSeconds(10));
        captchaInput.shouldBe(visible, Duration.ofSeconds(10));
        $("div.callback_frame").shouldNotHave(text("Спасибо"), Duration.ofSeconds(5));
        return this;
    }
}