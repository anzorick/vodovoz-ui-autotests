package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.base.ConfigReader;
import com.automation.pages.CartPage;
import com.automation.pages.SearchPage;
import com.automation.pages.SearchResultsPage;
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

/**
 * Тесты бизнес-логики корзины vodovoz.ru.
 *
 * Реальные DOM-селекторы (из /basket/):
 *   Кол-во:   input[data-entity='basket-item-quantity-field']
 *   Плюс:     span[data-entity='basket-item-quantity-plus']
 *   Минус:    span[data-entity='basket-item-quantity-minus']
 *   Цена ед:  span.basket-item-price-current-text
 *   Итог:     div.basket-checkout-total-price-inner
 *   Промокод: input[data-entity='basket-coupon-input'] + span.basket-coupon-block-coupon-btn
 *   Ошибка:   .basket-coupon-alert-section
 */
@Epic("vodovoz.ru")
@Feature("Корзина — Бизнес-логика цен")
@DisplayName("Корзина: пересчёт суммы")
@Tag("regression")
@Tag("cart")
class CartPriceTest extends BaseTest {

    private final CartPage   cartPage   = new CartPage();
    private final SearchPage searchPage = new SearchPage();

    @BeforeEach
    void addProductToCartAndOpen() {
        String query = ConfigReader.get("cart.query");
        log.info("=== Предусловие: добавляю товар '{}' в корзину ===", query);
        addFirstProductToCart(query);
        cartPage.open();
        Selenide.sleep(2000);
        cartPage.shouldBeLoaded();
        cartPage.shouldNotBeEmpty();
        log.info("=== Предусловие выполнено ===");
    }

    // Сценарий 1: AJAX-пересчёт суммы

    @Test
    @Story("Пересчёт суммы корзины")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("1. AJAX-пересчёт итоговой суммы при изменении количества товара")
    @Description("E2E: qty=1 -> запомнить P и Total. '+' -> qty=2 И Total=P*2. '-' -> qty=1 И Total=P.")
    void testCartTotalRecalculatesOnQuantityChange() {
        int unitPrice = readUnitPrice();
        readGrandTotal("начало");
        verifyQuantity(1, "начало");

        cartPage.increaseQuantity();
        verifyQuantity(2, "после +");
        waitForTotalAndVerify(unitPrice * 2, "после +");

        cartPage.decreaseQuantity();
        verifyQuantity(1, "после -");
        waitForTotalAndVerify(unitPrice, "после -");
        log.info("Тест AJAX-пересчёта суммы завершён успешно.");
    }

    // Сценарий 2: Негативный промокод

    @Test
    @Story("Промокод — негативный сценарий")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("2. Неверный промокод показывает ошибку и не меняет сумму")
    @Description("Негативный тест: ввести невалидный промокод -> блок ошибки -> Grand Total не изменился.")
    void testInvalidPromoCodeShowsError() {
        if (!cartPage.isPromoCodeFieldPresent()) {
            log.warn("Поле промокода не найдено — тест пропущен");
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Поле промокода basket-coupon-input недоступно");
            return;
        }
        int totalBefore = readGrandTotal("до промокода");
        String invalidCode = ConfigReader.get("promo.code.invalid");
        applyPromoCode(invalidCode);
        cartPage.promoCodeErrorShouldBeVisible();
        int totalAfter = readGrandTotal("после промокода");
        verifySumUnchanged(totalBefore, totalAfter);
        log.info("Тест негативного промокода завершён успешно.");
    }

    // Вспомогательные методы

    @Step("Поиск '{query}' -> добавить первый товар в корзину")
    private void addFirstProductToCart(String query) {
        SearchResultsPage results = searchPage.openPage().search(query);
        Selenide.sleep(2000);
        results.addFirstProductToCart();
        Selenide.sleep(2000);
    }

    @Step("Прочитать цену единицы первого товара")
    private int readUnitPrice() {
        int price = cartPage.getUnitPrice();
        log.info("Цена единицы товара: {} руб.", price);
        org.junit.jupiter.api.Assertions.assertTrue(price > 0, "Цена единицы > 0, но: " + price);
        return price;
    }

    @Step("Прочитать Grand Total [{label}]")
    private int readGrandTotal(String label) {
        int total = cartPage.getGrandTotal();
        log.info("Grand Total [{}]: {} руб.", label, total);
        org.junit.jupiter.api.Assertions.assertTrue(total > 0, "Grand Total > 0 [" + label + "], но: " + total);
        return total;
    }

    @Step("Проверить quantity == {expected} [{label}]")
    private void verifyQuantity(int expected, String label) {
        int actual = cartPage.getQuantity();
        log.info("Quantity [{}]: {} (ожидалось {})", label, actual, expected);
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual,
            "Quantity [" + label + "]: ожидалось " + expected + ", было " + actual);
    }

    @Step("Polling: ожидать Grand Total = {expectedRubles} руб. [{label}]")
    private void waitForTotalAndVerify(int expectedRubles, String label) {
        cartPage.waitUntilGrandTotalEquals(expectedRubles);
        int actual = cartPage.getGrandTotal();
        org.junit.jupiter.api.Assertions.assertEquals(expectedRubles, actual,
            "Grand Total [" + label + "]: ожидалось " + expectedRubles + " руб., но: " + actual);
        log.info("Grand Total [{}]: {} руб. == {} руб.", label, actual, expectedRubles);
    }

    @Step("Ввести и применить промокод '{promoCode}'" )
    private void applyPromoCode(String promoCode) {
        cartPage.enterAndApplyPromoCode(promoCode);
    }

    @Step("Проверить что Grand Total не изменился: {before} == {after} руб.")
    private void verifySumUnchanged(int before, int after) {
        org.junit.jupiter.api.Assertions.assertEquals(before, after,
            "Grand Total изменился после неверного промокода: " + before + " -> " + after + " руб.");
        log.info("Grand Total не изменился: {} руб.", after);
    }
}
