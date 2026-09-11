package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.CallbackModal;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.codeborne.selenide.Selenide.open;

@Epic("Интернет-магазин Vodovoz.ru")
@Feature("Сервисные формы")
@DisplayName("Сервисные формы: заказ звонка")
public class CallbackFormTest extends BaseTest {

    @Test
    @Story("Валидация формы обратного звонка")
    @DisplayName("Отправка пустой формы показывает ошибки валидации")
    @Description("Открытие модалки заказа звонка -> отправка пустой формы -> проверка ошибок валидации")
    void testEmptyCallbackFormShowsValidation() {
        open("/");
        new CallbackModal().open()
                .submit()
                .shouldShowValidationErrors();
    }

    @Test
    @Story("Валидация формы обратного звонка")
    @DisplayName("Форма не отправляется с неверным кодом защиты (капчей)")
    @Description("Имя + телефон из 10 цифр, начинающийся с 9 + код капчи 12345 -> отправка -> запрос отклонён, модалка осталась открытой")
    void testWrongCaptchaRejectsSubmission() {
        open("/");
        new CallbackModal().open()
                .enterName("Тест Тестович")
                .enterPhone("9123456789")
                .enterCaptcha("12345")
                .submit()
                .shouldNotBeSubmitted();
    }
}