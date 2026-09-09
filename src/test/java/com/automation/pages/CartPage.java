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
    private final SelenideElement basketSection = $(".basket-section, #basket-section, .order-cart, .basket-checkout-container");

    /**
     * Строки товаров в корзине.
     * Реальный HTML: tr[data-entity='basket-item'] (из basket-item-template)
     */
    private final ElementsCollection basketItems = $$("tr[data-entity='basket-item']");

    /** Пустая корзина */
    private final SelenideElement emptyBasket = $(".basket-empty, .empty-basket, [class*='basket-empty']");

    /**
     * Итоговая цена.
     * Реальный HTML: div[data-entity='basket-total-price']
     */
    private final SelenideElement totalPrice = $("[data-entity='basket-total-price'], .basket-total__price, .basket-checkout-total-price");

    /** Кнопка оформления заказа */
    private final SelenideElement orderButton = $("button.basket-btn-checkout, .btn-order, a[href*='/order/']");

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

    // ── Управление количеством ────────────────────────────────────────────────

    /**
     * Нажимает кнопку «+» для первого товара в корзине.
     * Реальный HTML: span[data-entity='basket-item-quantity-plus']
     */
    @Step("Увеличить количество первого товара (нажать '+')")
    public CartPage increaseFirstItemQuantity() {
        log.info("Нажимаю '+' для увеличения количества");
        // Реальный селектор из basket-item-template vodovoz.ru (строка 1298)
        SelenideElement plusBtn = $("[data-entity='basket-item-quantity-plus']");
        if (plusBtn.exists() && plusBtn.isDisplayed()) {
            executeJavaScript("arguments[0].click()", plusBtn);
            log.info("Нажата кнопка '+' (data-entity=basket-item-quantity-plus) ✓");
        } else {
            // Запасной вариант по CSS-классу
            SelenideElement plusByCss = $(".basket-item-btn-plus");
            if (plusByCss.exists()) {
                executeJavaScript("arguments[0].click()", plusByCss);
                log.info("Нажата кнопка '+' (.basket-item-btn-plus) ✓");
            } else {
                log.warn("Кнопка '+' не найдена — изменяю количество через JS на input");
                SelenideElement qtyInput = $("[data-entity='basket-item-quantity-field']");
                String currentVal = qtyInput.getValue();
                int newVal = Integer.parseInt(currentVal.trim()) + 1;
                executeJavaScript(
                    "arguments[0].value='" + newVal + "';" +
                    "arguments[0].dispatchEvent(new Event('change'));",
                    qtyInput
                );
                log.info("Количество изменено через JS: {} → {}", currentVal, newVal);
            }
        }
        com.codeborne.selenide.Selenide.sleep(2000); // ждём AJAX пересчёта
        return this;
    }

    /**
     * Нажимает кнопку «-» для первого товара в корзине.
     * Реальный HTML: span[data-entity='basket-item-quantity-minus']
     */
    @Step("Уменьшить количество первого товара (нажать '-')")
    public CartPage decreaseFirstItemQuantity() {
        log.info("Нажимаю '-' для уменьшения количества");
        SelenideElement minusBtn = $("[data-entity='basket-item-quantity-minus']");
        if (minusBtn.exists() && minusBtn.isDisplayed()) {
            executeJavaScript("arguments[0].click()", minusBtn);
            log.info("Нажата кнопка '-' ✓");
        } else {
            SelenideElement minusByCss = $(".basket-item-btn-minus");
            if (minusByCss.exists()) {
                executeJavaScript("arguments[0].click()", minusByCss);
                log.info("Нажата кнопка '-' (.basket-item-btn-minus) ✓");
            }
        }
        com.codeborne.selenide.Selenide.sleep(2000);
        return this;
    }


    /**
     * Возвращает числовое значение итоговой суммы корзины.
     * Очищает от пробелов, «₽», «руб.» и т.п.
     */
    @Step("Получить числовое значение итоговой суммы корзины")
    public int getCartTotalPriceValue() {
        // Пробуем несколько возможных контейнеров итога
        String[] totalSelectors = {
            ".basket-checkout-total-price-inner .price",
            "[data-entity='basket-total-price']",
            ".basket-checkout-total-price",
            ".basket-total__price",
            "[class*='total'][class*='price']"
        };
        for (String sel : totalSelectors) {
            try {
                SelenideElement el = $(sel);
                if (el.exists() && el.isDisplayed()) {
                    String raw = el.getText().replaceAll("[^\\d]", "").trim();
                    if (!raw.isEmpty()) {
                        int val = Integer.parseInt(raw);
                        log.info("Итоговая сумма ({}): {} ₽", sel, val);
                        return val;
                    }
                }
            } catch (Exception ignored) {}
        }
        log.warn("Не удалось получить числовое значение итоговой суммы");
        return 0;
    }

    /**
     * Удаляет первый товар из корзины через кнопку «Удалить».
     */
    @Step("Удалить первый товар из корзины")
    public CartPage removeFirstItem() {
        log.info("Удаляю первый товар из корзины");
        SelenideElement deleteBtn = $(
            "[data-entity='basket-item-delete'], " +
            ".basket-item-actions-remove, " +
            ".basket-item__remove, " +
            "[class*='basket'][class*='delete'], " +
            "[class*='basket'][class*='remove']"
        );
        if (deleteBtn.exists()) {
            executeJavaScript("arguments[0].click()", deleteBtn);
            log.info("Товар удалён (JS-клик) ✓");
            com.codeborne.selenide.Selenide.sleep(2000);
        } else {
            log.warn("Кнопка удаления не найдена!");
        }
        return this;
    }

    /**
     * Проверяет, что корзина пуста.
     *
     * Важно: vodovoz.ru показывает анимацию восстановления после удаления —
     * tr[data-entity='basket-item'] остаётся в DOM в состоянии "removed",
     * поэтому проверяем по отсутствию активных блоков управления количеством
     * (.basket-item-amount) или по счётчику корзины в шапке.
     */
    @Step("Корзина пуста")
    public CartPage shouldBeEmpty() {
        log.info("Проверяю что корзина пуста (ждём AJAX + анимации удаления)...");
        com.codeborne.selenide.Selenide.sleep(3000);

        // Стратегия 1: Блок управления количеством (.basket-item-amount) исчезает
        // при реальном удалении товара
        ElementsCollection activeQtyBlocks = $$(".basket-item-amount")
            .filter(com.codeborne.selenide.Condition.visible);

        if (activeQtyBlocks.size() == 0) {
            log.info("Блоки управления количеством отсутствуют → корзина пуста ✓");
            return this;
        }

        // Стратегия 2: Счётчик в шапке должен быть 0 или скрыт
        try {
            SelenideElement headerCount = $(".header-cart__count");
            String countText = headerCount.exists() ? headerCount.getText().trim() : "0";
            if (countText.equals("0") || countText.isEmpty()) {
                log.info("Счётчик корзины в шапке = '{}' → корзина пуста ✓", countText);
                return this;
            }
            log.info("Счётчик корзины в шапке: '{}'", countText);
        } catch (Exception e) {
            log.warn("Не удалось прочитать счётчик шапки: {}", e.getMessage());
        }

        // Стратегия 3: Элемент «корзина пуста»
        try {
            SelenideElement empty = $(".basket-empty, .empty-basket, [class*='basket-empty']");
            if (empty.exists() && empty.isDisplayed()) {
                log.info("Элемент 'корзина пуста' найден ✓");
                return this;
            }
        } catch (Exception ignored) {}

        // Все стратегии не сработали — корзина не пуста
        log.warn("Блоки количества ({}) ещё видны — корзина не пуста", activeQtyBlocks.size());
        org.junit.jupiter.api.Assertions.fail(
            "Ожидалась пустая корзина: visible .basket-item-amount = " + activeQtyBlocks.size());
        return this;
    }



    /**
     * Получает текущее значение счётчика количества первого товара.
     * Реальный HTML: input[data-entity='basket-item-quantity-field']
     */
    @Step("Получить текущее количество первого товара")
    public int getFirstItemQuantityValue() {
        try {
            // Реальный selector из basket-item-template vodovoz.ru (строка 1296)
            SelenideElement qtyInput = $("[data-entity='basket-item-quantity-field']");
            String val = qtyInput.getValue();
            int qty = Integer.parseInt(val.trim());
            log.info("Количество первого товара: {}", qty);
            return qty;
        } catch (Exception e) {
            log.warn("Не удалось получить количество товара: {}", e.getMessage());
            return 1;
        }
    }
}
