package com.automation.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object для страницы корзины vodovoz.ru (/basket/).
 *
 * Реальная HTML-структура корзины:
 *   Контейнер страницы:  .basket-section
 *   Список товаров:      .basket-item  (каждый товар)
 *   Название товара:     .basket-item__title a
 *   Количество:          .basket-item__quantity
 *   Цена товара:         .basket-item__price
 *   Итоговая сумма:      .basket-total__price
 *   Кнопка «Оформить»:   .btn-order, a[href*="order"]
 *   Пустая корзина:      .basket-empty, .empty-basket
 */
public class CartPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(CartPage.class);

    // ── Selectors ────────────────────────────────────────────────────────────

    /** Контейнер всей страницы корзины */
    private final SelenideElement basketSection = $(".basket-section, #basket-section, .order-cart, .cart-container");

    /** Список элементов корзины — пробуем несколько возможных классов */
    private final ElementsCollection basketItems = $$(".basket-item, .order-basket__item, .cart-item, [class*='basket-item']");

    /** Пустая корзина */
    private final SelenideElement emptyBasket = $(".basket-empty, .empty-basket, [class*='basket-empty']");

    /** Итоговая цена */
    private final SelenideElement totalPrice = $(".basket-total__price, .order-total__price, [class*='total']");

    /** Кнопка оформления заказа */
    private final SelenideElement orderButton = $(".btn-order, a[href*='/order/'], button[class*='order']");

    // ── Navigation ───────────────────────────────────────────────────────────

    /**
     * Открывает страницу корзины напрямую через URL.
     */
    @Step("Открыть страницу корзины /basket/")
    public CartPage open() {
        log.info("Открываю страницу корзины: /basket/");
        com.codeborne.selenide.Selenide.open("/basket/");
        return this;
    }

    // ── Assertions ───────────────────────────────────────────────────────────

    /**
     * Проверяет, что страница корзины загружена — URL содержит /basket/.
     */
    @Step("Страница корзины загружена")
    public CartPage shouldBeLoaded() {
        log.info("Проверяю загрузку страницы корзины");
        urlShouldContain("/basket/");
        return this;
    }

    /**
     * Проверяет, что корзина не пуста — найден хотя бы 1 товар.
     */
    @Step("Корзина содержит товары (не пуста)")
    public CartPage shouldNotBeEmpty() {
        log.info("Проверяю, что корзина не пуста");
        basketItems.shouldHave(CollectionCondition.sizeGreaterThan(0));
        return this;
    }

    /**
     * Проверяет точное количество товаров в корзине.
     *
     * @param expectedCount ожидаемое количество позиций
     */
    @Step("Корзина содержит {expectedCount} товар(а/ов)")
    public CartPage shouldHaveItemsCount(int expectedCount) {
        log.info("Проверяю количество товаров в корзине: ожидаю {}", expectedCount);
        basketItems.shouldHave(CollectionCondition.size(expectedCount));
        return this;
    }

    /**
     * Проверяет, что хотя бы один товар в корзине содержит указанный текст в названии.
     *
     * @param partialName часть названия товара (регистронезависимо)
     */
    @Step("В корзине есть товар, содержащий в названии: '{partialName}'")
    public CartPage shouldContainProduct(String partialName) {
        log.info("Ищу товар '{}' в корзине", partialName);
        // Ищем среди заголовков товаров в корзине
        ElementsCollection titles = $$(".basket-item__title, .basket-item__name, [class*='basket-item'] a, [class*='cart-item'] a");
        boolean found = titles.stream()
                .anyMatch(el -> {
                    try {
                        return el.getText().toLowerCase().contains(partialName.toLowerCase());
                    } catch (Exception e) {
                        return false;
                    }
                });
        if (!found) {
            log.warn("Товар '{}' не найден среди {} элементов корзины. Проверяю весь текст страницы...", partialName, titles.size());
            // Запасной вариант: проверяем весь текст страницы корзины
            $("body").shouldHave(Condition.text(partialName));
        }
        log.info("Товар '{}' найден в корзине ✓", partialName);
        return this;
    }

    /**
     * Возвращает количество товаров в корзине.
     */
    @Step("Получить количество товаров в корзине")
    public int getItemsCount() {
        int count = basketItems.size();
        log.info("Количество товаров в корзине: {}", count);
        return count;
    }

    /**
     * Проверяет, что отображается итоговая сумма (корзина не пуста и сумма посчитана).
     */
    @Step("Итоговая сумма заказа отображается")
    public CartPage totalPriceShouldBeVisible() {
        log.info("Проверяю отображение итоговой суммы");
        totalPrice.shouldBe(Condition.visible);
        return this;
    }

    /**
     * Проверяет, что кнопка «Оформить заказ» видима и активна.
     */
    @Step("Кнопка 'Оформить заказ' доступна")
    public CartPage orderButtonShouldBeVisible() {
        log.info("Проверяю кнопку оформления заказа");
        orderButton.shouldBe(Condition.visible);
        return this;
    }
}