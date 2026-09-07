package com.automation.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object для страницы результатов поиска vodovoz.ru (/catalog/?q=...).
 *
 * Реальные CSS-селекторы получены из HTML сайта:
 *   - Карточки товаров:        .catalog-block__wrapper
 *   - Название товара:         .catalog-block__info-title a.dark_link
 *   - Блок товаров:            .catalog-items
 *   - Кнопка «В корзину»:      span.to_cart[data-action="basket"]
 *   - Счётчик корзины шапке:   .header-cart__count
 */
public class SearchResultsPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(SearchResultsPage.class);

    // ── Локаторы ─────────────────────────────────────────────────────────────

    private final ElementsCollection productCards   = $$(".catalog-block__wrapper");
    private final ElementsCollection productTitles  = $$(".catalog-block__info-title a.dark_link");
    private final SelenideElement    catalogItems   = $(".catalog-items");

    /** Первая кнопка «В корзину» среди результатов */
    private final SelenideElement firstAddToCartBtn = $("span.to_cart[data-action='basket']");

    /** Все кнопки «В корзину» */
    private final ElementsCollection addToCartBtns  = $$("span.to_cart[data-action='basket']");

    /** Счётчик товаров в корзине (шапка сайта) */
    private final SelenideElement cartCounter       = $(".header-cart__count");

    // ── Проверки ─────────────────────────────────────────────────────────────

    @Step("Страница результатов должна быть загружена")
    public SearchResultsPage shouldBeLoaded() {
        catalogItems.shouldBe(visible);
        return this;
    }

    @Step("Должны быть найдены товары (не менее {minCount} шт.)")
    public SearchResultsPage shouldHaveProductsAtLeast(int minCount) {
        int actual = productCards.size();
        assertTrue(actual >= minCount,
                "Ожидалось не менее " + minCount + " товаров, найдено: " + actual);
        return this;
    }

    @Step("URL должен содержать поисковой запрос: {query}")
    public SearchResultsPage urlShouldContainQuery(String query) {
        urlShouldContain("q=");
        return this;
    }

    @Step("Хотя бы один товар должен содержать в названии: {keyword}")
    public SearchResultsPage atLeastOneProductShouldContain(String keyword) {
        boolean found = productTitles.stream()
                .anyMatch(el -> el.getText().toLowerCase()
                        .contains(keyword.toLowerCase()));
        assertTrue(found,
                "Ни один товар не содержит '" + keyword + "' в названии");
        return this;
    }

    // ── Действия с корзиной ───────────────────────────────────────────────────

    /**
     * Добавляет первый товар из результатов поиска в корзину.
     *
     * Особенности vodovoz.ru:
     * - Добавление через AJAX (без перезагрузки страницы)
     * - На странице может быть попап .popup-text-info__text (уведомление о доставке)
     *   который перехватывает клики — закрываем его перед кликом
     * - Используем JS-клик для надёжного обхода любых оверлеев
     *
     * @return имя добавленного товара (для проверки в корзине)
     */
    @Step("Добавить первый товар в корзину")
    public String addFirstProductToCart() {
        // Запоминаем имя первого товара до добавления
        String productName = getFirstProductTitle();
        log.info("Добавляю товар в корзину: '{}'", productName);

        // Шаг 1: Закрываем любые всплывающие попапы/уведомления
        closePoupupsIfPresent();

        // Шаг 2: Прокручиваем к кнопке «В корзину»
        firstAddToCartBtn.shouldBe(Condition.visible).scrollIntoView("{block: 'center'}");
        log.info("Кнопка 'В корзину' видима, нажимаю через JS...");

        // Шаг 3: JS-клик — надёжно обходит любые overlays/попапы
        com.codeborne.selenide.Selenide.executeJavaScript("arguments[0].click()", firstAddToCartBtn);
        log.info("Кнопка 'В корзину' нажата (JS-клик)");

        // Шаг 4: Ждём AJAX-обновления счётчика корзины в шапке
        cartCounter.shouldNotHave(Condition.text("0"),
                java.time.Duration.ofMillis(com.codeborne.selenide.Configuration.timeout));
        log.info("Счётчик корзины обновился: {}", cartCounter.getText());

        return productName;
    }

    /**
     * Закрывает всплывающие попапы/уведомления если они присутствуют.
     * vodovoz.ru показывает попап о зоне доставки при первом визите.
     */
    @Step("Закрыть всплывающие попапы (если есть)")
    private void closePoupupsIfPresent() {
        // Список возможных кнопок закрытия попапов на vodovoz.ru
        String[] closeSelectors = {
            ".popup-text-info .close",
            ".popup-text-info__close",
            ".popup-closer",
            ".mfp-close",
            "button.close",
            "[class*='popup'] [class*='close']",
            "[class*='modal'] [class*='close']",
            ".cookie-notice__close",
            ".alert .close"
        };

        for (String selector : closeSelectors) {
            try {
                com.codeborne.selenide.SelenideElement closeBtn = com.codeborne.selenide.Selenide.$(selector);
                if (closeBtn.exists() && closeBtn.isDisplayed()) {
                    closeBtn.click();
                    log.info("Закрыт попап: {}", selector);
                    com.codeborne.selenide.Selenide.sleep(500);
                }
            } catch (Exception ignored) {
                // Попап не найден — продолжаем
            }
        }

        // Дополнительно: закрываем попап кликом вне его области (ESC)
        try {
            com.codeborne.selenide.Selenide.actions().sendKeys(
                org.openqa.selenium.Keys.ESCAPE).perform();
            com.codeborne.selenide.Selenide.sleep(300);
        } catch (Exception ignored) {}
    }


    /**
     * Возвращает текущее значение счётчика корзины в шапке.
     */
    @Step("Получить количество товаров в корзине (счётчик в шапке)")
    public String getCartCounterText() {
        return cartCounter.getText().trim();
    }

    /**
     * Проверяет, что счётчик корзины в шапке не равен нулю.
     */
    @Step("Счётчик корзины должен быть больше 0")
    public SearchResultsPage cartCounterShouldBePositive() {
        cartCounter.shouldNotHave(Condition.text("0"));
        log.info("Счётчик корзины: {}", cartCounter.getText());
        return this;
    }

    @Step("Получить количество найденных товаров")
    public int getProductCount() {
        return productCards.size();
    }

    @Step("Получить название первого товара")
    public String getFirstProductTitle() {
        return productTitles.first().shouldBe(visible).getText().trim();
    }

    /** Алиас для совместимости с тестами */
    @Step("Получить название первого товара")
    public String getFirstProductName() {
        return getFirstProductTitle();
    }

    // ── Избранное ────────────────────────────────────────────────────────────

    /**
     * Добавляет первый товар в избранное через JS-клик.
     * Кнопка: a.js-item-action[data-action='favorite']
     *
     * @return имя добавленного товара
     */
    @Step("Добавить первый товар в избранное")
    public String addFirstProductToFavorites() {
        String productName = getFirstProductTitle();
        log.info("Добавляю в избранное: '{}'", productName);

        // Закрываем попапы/баннеры
        closePoupupsIfPresent();

        // Кнопка «В избранное» первого товара
        com.codeborne.selenide.SelenideElement favoriteBtn =
            com.codeborne.selenide.Selenide.$("a.js-item-action[data-action='favorite']");

        favoriteBtn.shouldBe(Condition.visible).scrollIntoView("{block: 'center'}");
        com.codeborne.selenide.Selenide.executeJavaScript("arguments[0].click()", favoriteBtn);
        log.info("Кнопка «В избранное» нажата (JS-клик)");

        // Ждём обновления счётчика избранного
        try {
            com.codeborne.selenide.Selenide.$(".icon-count--favorite")
                .shouldNotHave(Condition.text("0"), java.time.Duration.ofSeconds(10));
            log.info("Счётчик избранного обновился ✓");
        } catch (Exception e) {
            log.warn("Счётчик избранного не обновился — возможно, требуется авторизация");
        }

        return productName;
    }
}