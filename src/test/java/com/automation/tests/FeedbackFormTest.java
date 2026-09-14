package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.FeedbackModal;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Негативные тесты формы «Написать отзыв» (feedback form, id=12) на vodovoz.ru.
 *
 * <h3>Форма находится на странице /reviews/ и открывается по кнопке «Написать отзыв»
 * (data-event="jqm", data-name="feedback", data-param-id="12").</h3>
 *
 * <h3>Механизм валидации:</h3>
 * <ul>
 *   <li>jQuery Validate (client-side) — срабатывает до отправки на сервер</li>
 *   <li>Добавляет {@code label.error} рядом с каждым невалидным полем</li>
 *   <li>Обязательные поля: Рейтинг, Имя, Email, Текст отзыва</li>
 *   <li>Submit изначально disabled — активируется чекбоксом согласия</li>
 * </ul>
 *
 * <p><b>Важно:</b> Реальная отправка отзыва НЕ выполняется — данные проверяются
 * на клиенте jQuery Validate. На сервер ничего не уходит.
 */
@Epic("vodovoz.ru")
@Feature("Форма написания отзыва")
@DisplayName("Форма «Написать отзыв» — негативные тесты")
class FeedbackFormTest extends BaseTest {

    private final FeedbackModal feedbackModal = new FeedbackModal();

    @BeforeEach
    void openReviewsPage() {
        log.info("Открываю страницу отзывов");
        Selenide.open("/reviews/");
        Selenide.sleep(2000); // ждём полной загрузки JS
    }

    // ── Тест 1: Пустая форма ─────────────────────────────────────────────────

    @Test
    @Story("Валидация формы")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Пустая форма отзыва показывает ошибки валидации")
    @Description("""
            Шаги:
            1. Открыть страницу /reviews/
            2. Нажать «Написать отзыв» → открывается модальное окно формы id=12
            3. Активировать чекбокс согласия (иначе Submit disabled)
            4. Отправить форму без заполнения полей
            5. Проверить что jQuery Validate добавил label.error
               (Рейтинг, Имя, Email, Текст отзыва — обязательные поля)
            """)
    void testEmptyFeedbackFormShowsValidation() {
        feedbackModal
                .open()       // открываем модалку + активируем чекбокс согласия
                .submit()     // Submit без заполнения полей → jQuery Validate
                .shouldShowValidationErrors(); // label.error должны быть видимы
    }

    // ── Тест 2: Только имя — остальные поля пусты ────────────────────────────

    @Test
    @Story("Валидация формы")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Форма с именем без рейтинга и текста показывает ошибки валидации")
    @Description("""
            Шаги:
            1. Открыть страницу /reviews/
            2. Нажать «Написать отзыв» → модальное окно
            3. Ввести только имя («Тест Авто»)
            4. Отправить форму без рейтинга, email и текста отзыва
            5. Проверить что валидация сработала (label.error видимы):
               - Рейтинг — обязателен
               - Email — обязателен
               - Текст отзыва — обязателен
            """)
    void testPartialFeedbackFormShowsValidation() {
        feedbackModal
                .open()
                .enterName("Тест Авто")  // заполняем только имя
                .submit()               // остальные поля пусты → валидация
                .shouldShowValidationErrors();
    }
}
