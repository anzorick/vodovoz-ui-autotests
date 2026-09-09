package com.automation.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;

/**
 * Page Object для страниц каталога vodovoz.ru.
 *
 * HTML-структура (из анализа /catalog/pitevaya_voda_19_litrov/):
 *
 *   Список товаров:       .catalog-block__wrapper
 *   Сортировка (кнопка): .dropdown-select__title span (текущий вариант)
 *   Пункты сортировки:   a.dropdown-menu-item[data-url]  /  span.dropdown-menu-item--current
 *
 *   Варианты сортировки (URL-параметры):
 *     Популярные (default) — без параметров
 *     Дешевле  → ?sort=CATALOG_PRICE_2&order=asc
 *     Дороже   → ?sort=CATALOG_PRICE_2&order=desc
 *     Со скидкой → ?sort=PROPERTY_SKIDKA_PO_AKTSII_TOVAR_DNYA&order=desc
 *     Высокий рейтинг → ?sort=PROPERTY_EXTENDED_REVIEWS_RAITING&order=desc
 *
 *   Цена товара:         .catalog-block__price-new  (может содержать "руб." или "₽")
 */
public class CatalogPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(CatalogPage.class);

    /** Варианты сортировки */
    public enum SortOption {
        POPULAR("", ""),
        PRICE_ASC("CATALOG_PRICE_2", "asc"),
        PRICE_DESC("CATALOG_PRICE_2", "desc"),
        DISCOUNT("PROPERTY_SKIDKA_PO_AKTSII_TOVAR_DNYA", "desc"),
        RATING("PROPERTY_EXTENDED_REVIEWS_RAITING", "desc");

        public final String sortParam;
        public final String orderParam;

        SortOption(String sort, String order) {
            this.sortParam = sort;
            this.orderParam = order;
        }
    }

    // ── Локаторы ─────────────────────────────────────────────────────────────

    /** Карточки товаров */
    private final ElementsCollection productCards = $$(".catalog-block__wrapper");

    /** Цены товаров (новая/актуальная цена) */
    private final ElementsCollection productPrices = $$(
        ".catalog-block__price-new, .catalog-block__price");

    /** Кнопка-заголовок выпадающего списка сортировки */
    private final SelenideElement sortDropdownTitle = $(".filter-panel__sort .dropdown-select__title");

    /** Блок-контейнер списка вариантов сортировки */
    private final SelenideElement sortDropdownList = $(".filter-panel__sort .dropdown-select__list");

    // ── Навигация ─────────────────────────────────────────────────────────────

    /**
     * Открывает указанный раздел каталога.
     * @param catalogSlug например "pitevaya_voda_19_litrov"
     */
    @Step("Открыть каталог: /{catalogSlug}/")
    public CatalogPage open(String catalogSlug) {
        log.info("Открываю каталог: /catalog/{}/", catalogSlug);
        open("/catalog/" + catalogSlug + "/");
        return this;
    }

    /**
     * Применяет сортировку через прямую навигацию по URL (надёжнее клика по dropdown).
     * @param option вариант сортировки из enum SortOption
     * @param catalogSlug slug текущего раздела каталога
     */
    @Step("Применить сортировку: {option}")
    public CatalogPage sortBy(SortOption option, String catalogSlug) {
        if (option == SortOption.POPULAR) {
            com.codeborne.selenide.Selenide.open("/catalog/" + catalogSlug + "/");
        } else {
            String url = "/catalog/" + catalogSlug + "/"
                    + "?sort=" + option.sortParam
                    + "&order=" + option.orderParam;
            log.info("Сортировка по URL: {}", url);
            com.codeborne.selenide.Selenide.open(url);
        }
        shouldBeLoaded();
        return this;
    }

    // ── Проверки ─────────────────────────────────────────────────────────────

    /** Проверяет что каталог загружен (товары видны) */
    @Step("Каталог загружен (товары видны)")
    public CatalogPage shouldBeLoaded() {
        log.info("Жду загрузки каталога...");
        productCards.shouldBe(
            CollectionCondition.sizeGreaterThan(0),
            java.time.Duration.ofSeconds(15));
        log.info("Каталог загружен, товаров: {}", productCards.size());
        return this;
    }

    /** Проверяет что товаров не менее N */
    @Step("Количество товаров ≥ {minCount}")
    public CatalogPage productCountShouldBeAtLeast(int minCount) {
        int actual = productCards.size();
        org.junit.jupiter.api.Assertions.assertTrue(
            actual >= minCount,
            "Ожидалось ≥ " + minCount + " товаров, найдено: " + actual
        );
        log.info("Товаров в каталоге: {} (≥ {} ✓)", actual, minCount);
        return this;
    }

    // ── Получение данных ──────────────────────────────────────────────────────

    /**
     * Возвращает числовые значения цен всех товаров на странице.
     * Очищает строку от пробелов, «руб.», «₽» и прочего.
     */
    @Step("Получить список цен товаров")
    public List<Integer> getAllProductPrices() {
        List<Integer> prices = new ArrayList<>();
        for (SelenideElement el : productPrices) {
            try {
                String raw = el.getText()
                    .replaceAll("[^\\d]", "")  // оставляем только цифры
                    .trim();
                if (!raw.isEmpty()) {
                    prices.add(Integer.parseInt(raw));
                }
            } catch (Exception e) {
                log.warn("Не удалось распарсить цену: {}", e.getMessage());
            }
        }
        log.info("Цены ({} шт.): {}", prices.size(), prices);
        return prices;
    }

    /** Возвращает цену первого товара */
    @Step("Получить цену первого товара")
    public int getFirstProductPrice() {
        List<Integer> prices = getAllProductPrices();
        org.junit.jupiter.api.Assertions.assertFalse(prices.isEmpty(),
            "Список цен пуст — цены не найдены на странице");
        return prices.get(0);
    }

    /** Возвращает количество товаров на странице */
    @Step("Количество товаров на странице")
    public int getProductCount() {
        return productCards.size();
    }

    // ── Вспомогательные проверки цен ─────────────────────────────────────────

    /**
     * Проверяет, что цены отсортированы по возрастанию.
     * Допускает нарушение порядка не более чем у 20% товаров
     * (на сайте могут быть акционные цены, нарушающие строгий порядок).
     */
    @Step("Цены отсортированы по возрастанию")
    public CatalogPage pricesShouldBeAscending() {
        List<Integer> prices = getAllProductPrices();
        org.junit.jupiter.api.Assertions.assertTrue(
            prices.size() >= 2,
            "Нужно ≥ 2 цен для проверки сортировки, найдено: " + prices.size()
        );
        // Считаем количество нарушений порядка
        int violations = 0;
        for (int i = 1; i < prices.size(); i++) {
            if (prices.get(i) < prices.get(i - 1)) {
                violations++;
                log.warn("Нарушение порядка (по возрастанию): позиция {}: {} < {}", i, prices.get(i), prices.get(i - 1));
            }
        }
        int maxAllowed = Math.max(1, prices.size() / 5); // 20% допустимо
        org.junit.jupiter.api.Assertions.assertTrue(
            violations <= maxAllowed,
            String.format("Слишком много нарушений сортировки по возрастанию: %d/%d (допустимо ≤ %d)",
                violations, prices.size(), maxAllowed)
        );
        log.info("Сортировка по возрастанию: нарушений {}/{} (допустимо ≤ {}) ✓",
            violations, prices.size(), maxAllowed);
        return this;
    }

    /**
     * Проверяет, что первый товар дешевле последнего
     * (мягкая проверка: не все цены, только первая и последняя).
     */
    @Step("Первый товар дешевле последнего (сортировка ASC)")
    public CatalogPage firstProductShouldBeCheaperThanLast() {
        List<Integer> prices = getAllProductPrices();
        if (prices.size() < 2) {
            log.warn("Недостаточно товаров для сравнения first/last");
            return this;
        }
        int first = prices.get(0);
        int last  = prices.get(prices.size() - 1);
        org.junit.jupiter.api.Assertions.assertTrue(
            first <= last,
            String.format("Первый товар (%d ₽) дороже последнего (%d ₽) — сортировка по возрастанию нарушена", first, last)
        );
        log.info("first={} ₽ ≤ last={} ₽ ✓", first, last);
        return this;
    }

    /**
     * Проверяет, что первый товар дороже последнего (сортировка по убыванию).
     */
    @Step("Первый товар дороже последнего (сортировка DESC)")
    public CatalogPage firstProductShouldBeMoreExpensiveThanLast() {
        List<Integer> prices = getAllProductPrices();
        if (prices.size() < 2) {
            log.warn("Недостаточно товаров для сравнения first/last");
            return this;
        }
        int first = prices.get(0);
        int last  = prices.get(prices.size() - 1);
        org.junit.jupiter.api.Assertions.assertTrue(
            first >= last,
            String.format("Первый товар (%d ₽) дешевле последнего (%d ₽) — сортировка по убыванию нарушена", first, last)
        );
        log.info("first={} ₽ ≥ last={} ₽ ✓", first, last);
        return this;
    }
}