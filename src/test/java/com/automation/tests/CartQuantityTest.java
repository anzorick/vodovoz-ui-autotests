package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.CartPage;
import com.automation.pages.SearchPage;
import com.automation.pages.SearchResultsPage;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

/**
 * Тесты управления количеством товара в корзине vodovoz.ru.
 *
 * Предусловие (BeforeEach):
 *   поиск → добавление первого товара → переход в корзину
 */
@Epic("vodovoz.ru")
@Feature("Корзина — Управление количеством")
@DisplayName("Корзина: изменение количества и удаление товара")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class CartQuantityTest extends BaseTest {

    private final CartPage     cartPage    = new CartPage();
    private final SearchPage   searchPage  = new SearchPage();

    /** Добавляем товар в корзину перед каждым тестом */
    @BeforeEach
    void addProductToCartAndOpen() {
        log.info("=== Предусловие: добавляю товар в корзину ===");
        addFirstProductToCart("вода питьевая");
        cartPage.open();
        Selenide.sleep(2000);
        cartPage.shouldBeLoaded();
        cartPage.shouldNotBeEmpty();
        log.info("=== Предусловие выполнено: корзина содержит товар ===");
    }

    // ── Тест 1: Увеличение количества ─────────────────────────────────────────

    @Test
    @Story("Управление количеством")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("1. Увеличение количества товара через кнопку '+'")
    @Description("""
            Шаги:
            1. Добавить товар в корзину (предусловие)
            2. Запомнить итоговую сумму до изменения
            3. Нажать '+' для увеличения количества
            4. Проверить что товаров стало 2
            """)
    void testIncreaseQuantityUpdatesTotal() {
        // Шаг 1: запоминаем сумму до изменения
        int totalBefore = getCartTotal();

        // Шаг 2: увеличиваем количество
        cartPage.increaseFirstItemQuantity();
        Selenide.sleep(2000);

        // Шаг 3: проверяем что количество увеличилось или сумма выросла
        verifyQuantityIncreased(totalBefore);
    }

    // ── Тест 2: Удаление товара ────────────────────────────────────────────────

    @Test
    @Story("Удаление из корзины")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("2. Удаление товара из корзины")
    @Description("""
            Шаги:
            1. Добавить товар в корзину (предусловие)
            2. Нажать кнопку удаления товара
            3. Проверить что корзина опустела
            """)
    void testRemoveItemFromCart() {
        // Подтверждаем что корзина не пуста
        cartPage.shouldNotBeEmpty();
        log.info("Корзина содержит товар — готово к удалению");

        // Удаляем
        cartPage.removeFirstItem();
        Selenide.sleep(2000);

        // Проверяем что пуста
        verifyCartEmpty();
    }

    // ── Тест 3: Корзина не пуста и содержит верные данные ──────────────────────

    @Test
    @Story("Содержимое корзины")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("3. Корзина содержит добавленный товар с ценой")
    @Description("""
            Шаги:
            1. Добавить товар (предусловие)
            2. Проверить что корзина не пустая
            3. Проверить что итоговая сумма > 0
            4. Проверить кнопку «Оформить заказ»
            """)
    void testCartContainsProductWithPrice() {
        cartPage.shouldNotBeEmpty();
        cartPage.shouldHaveItemsCount(1);
        verifyTotalPricePositive();
        cartPage.orderButtonShouldBeVisible();
    }

    // ── Вспомогательные @Step-методы ─────────────────────────────────────────

    @Step("Поиск '{query}' и добавление первого товара в корзину")
    private void addFirstProductToCart(String query) {
        SearchResultsPage results = searchPage.openPage().search(query);
        Selenide.sleep(2000);
        results.addFirstProductToCart();
        Selenide.sleep(2000);
    }

    @Step("Получить текущую итоговую сумму корзины")
    private int getCartTotal() {
        int total = cartPage.getCartTotalPriceValue();
        log.info("Итоговая сумма до изменения: {} ₽", total);
        return total;
    }

    @Step("Проверить что количество увеличилось (сумма выросла или кол-во = 2)")
    private void verifyQuantityIncreased(int totalBefore) {
        // Проверяем через количество или через итог
        int qty = cartPage.getFirstItemQuantityValue();
        int totalAfter = cartPage.getCartTotalPriceValue();
        log.info("После '+': кол-во={}, сумма={} ₽ (было {} ₽)", qty, totalAfter, totalBefore);

        boolean qtyIncreased = qty >= 2;
        boolean totalIncreased = totalAfter > totalBefore && totalBefore > 0;

        org.junit.jupiter.api.Assertions.assertTrue(
            qtyIncreased || totalIncreased,
            String.format("Ни количество (%d), ни сумма (%d → %d) не выросли после нажатия '+'", qty, totalBefore, totalAfter)
        );
        log.info("Количество/сумма увеличились после '+' ✓");
    }

    @Step("Проверить что корзина пуста после удаления")
    private void verifyCartEmpty() {
        cartPage.shouldBeEmpty();
        log.info("Корзина пуста после удаления товара ✓");
    }

    @Step("Проверить что итоговая сумма корзины > 0")
    private void verifyTotalPricePositive() {
        int total = cartPage.getCartTotalPriceValue();
        org.junit.jupiter.api.Assertions.assertTrue(
            total > 0,
            "Итоговая сумма корзины должна быть > 0, но: " + total
        );
        log.info("Итоговая сумма: {} ₽ > 0 ✓", total);
    }
}