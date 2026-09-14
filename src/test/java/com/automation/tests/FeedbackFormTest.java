package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.base.ConfigReader;
import com.automation.pages.FeedbackModal;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Негативные тесты формы «Написать отзыв» (feedback form, id=12) на vodovoz.ru.
 *
 * <h3>Форма на /reviews/, кнопка data-name="feedback", data-param-id="12".</h3>
 * Валидация: jQuery Validate → label.error у каждого невалидного поля.
 * Submit изначально disabled — активируется чекбоксом согласия.
 * Реальная отправка НЕ выполняется.
 */
@Epic("vodovoz.ru")
@Feature("Форма написания отзыва")
@DisplayName("Форма «Написать отзыв» — негативные тесты")
@Tag("regression")
@Tag("forms")
class FeedbackFormTest extends BaseTest {

    private final FeedbackModal feedbackModal = new FeedbackModal();

    @BeforeEach
    void openReviewsPage() {
        log.info("Открываю страницу отзывов");
        Selenide.open("/reviews/");
        Selenide.sleep(2000);
    }

    @Test
    @Story("Валидация формы")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Пустая форма отзыва показывает ошибки валидации")
    @Description("""
            1. Открыть /reviews/
            2. Нажать «Написать отзыв» → модалка id=12
            3. Активировать чекбокс согласия (иначе Submit disabled)
            4. Отправить без заполнения полей
            5. Проверить label.error (Рейтинг, Имя, Email, Текст — required)
            """)
    void testEmptyFeedbackFormShowsValidation() {
        feedbackModal
                .open()
                .submit()
                .shouldShowValidationErrors();
    }

    @Test
    @Story("Валидация формы")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Форма с именем без рейтинга и текста показывает ошибки валидации")
    @Description("""
            1. Открыть /reviews/
            2. Нажать «Написать отзыв» → модалка
            3. Ввести только имя «Тест Авто»
            4. Отправить без рейтинга, email и текста
            5. Проверить label.error (Рейтинг, Email, Текст — обязательны)
            """)
    void testPartialFeedbackFormShowsValidation() {
        feedbackModal
                .open()
                .enterName("Тест Авто")
                .submit()
                .shouldShowValidationErrors();
    }
}
