package com.automation.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Page Object для страницы сравнения товаров vodovoz.ru (/catalog/compare.php).
 *
 * DOM-структура (верифицирована live-инспекцией):
 *   Главный контейнер:  div.catalog-compare
 *   Товарная карусель:  div.catalog-compare__items   (owl-carousel, N товаров)
 *   Свойства товара:    div.catalog-compare__item-props  (по одному на каждый товар)
 *   Строки атрибутов:   div.catalog-compare__props-slider  (синхронная прокрутка)
 *   Удалить товар:      span.remove[onclick*='DELETE_FROM_COMPARE_RESULT&ID='] внутри карусели
 *   Счётчик сравнения в шапке: span.icon-count--compare
 *   Ссылка на сравнение: a.compare-link[href='/catalog/compare.php']
 *
 * Кнопка добавления в сравнение на карточке каталога:
 *   a.js-item-action[data-action='compare']
 */
public class ComparePage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ComparePage.class);

    /** Главный контейнер страницы сравнения */
    private final SelenideElement compareContainer = $("div.catalog-compare.swipeignore");

    /**
     * Колонки свойств — по одной на каждый добавленный товар.
     * div.catalog-compare__item-props: одна колонка = один товар.
     */
    private final ElementsCollection productColumns = $$("div.catalog-compare__item-props");

    /**
     * Кнопки удаления товара из сравнения.
     * span.remove с onclick DELETE_FROM_COMPARE_RESULT&ID= внутри карусели товаров.
     */
    private final ElementsCollection removeButtons =
        $$(".catalog-compare__items span.remove");

    /**
     * Строки атрибутов (характеристики) в слайдере свойств.
     */
    private final ElementsCollection propRows =
        $$(".catalog-compare__props-slider .catalog-compare__props-row");

    // ── Навигация ─────────────────────────────────────────────────────────────

    @Step("Открыть страницу сравнения /catalog/compare.php")
    public ComparePage open() {
        com.codeborne.selenide.Selenide.open("/catalog/compare.php");
        return this;
    }

    // ── Проверки ──────────────────────────────────────────────────────────────

    @Step("Страница сравнения должна быть загружена")
    public ComparePage shouldBeLoaded() {
        compareContainer.shouldBe(visible, Duration.ofSeconds(10));
        log.info("Compare page loaded, product columns: {}", productColumns.size());
        return this;
    }

    @Step("В таблице сравнения должно быть {expected} товара")
    public ComparePage shouldContainProducts(int expected) {
        int actual = getProductsCount();
        log.info("Products in compare table: expected={}, actual={}", expected, actual);
        org.junit.jupiter.api.Assertions.assertEquals(
            expected, actual,
            "Ожидалось " + expected + " товаров в сравнении, найдено: " + actual
        );
        return this;
    }

    @Step("Проверить наличие строки атрибута: {attributeName}")
    public ComparePage shouldHaveAttributeRow(String attributeName) {
        boolean found = propRows.stream()
            .anyMatch(row -> row.getText().toLowerCase()
                .contains(attributeName.toLowerCase()));
        log.info("Attribute row '{}' found: {}", attributeName, found);
        org.junit.jupiter.api.Assertions.assertTrue(
            found,
            "Строка атрибута '" + attributeName + "' не найдена на странице сравнения"
        );
        return this;
    }

    // ── Геттеры ───────────────────────────────────────────────────────────────

    @Step("Получить количество товаров в таблице сравнения")
    public int getProductsCount() {
        return productColumns.size();
    }

    @Step("Получить значения атрибута '{attributeName}' по всем товарам")
    public List<String> getAttributeRowValues(String attributeName) {
        // Строки атрибутов в слайдере prop-rows содержат заголовок + значения по колонкам
        for (SelenideElement row : propRows) {
            String label = row.$(".catalog-compare__props-row-name").getText().trim();
            if (label.toLowerCase().contains(attributeName.toLowerCase())) {
                List<String> values = row.$$(".catalog-compare__props-row-value")
                    .stream()
                    .map(el -> el.getText().trim())
                    .collect(Collectors.toList());
                log.info("Attribute '{}' values: {}", attributeName, values);
                return values;
            }
        }
        log.warn("Attribute '{}' not found in compare table", attributeName);
        return List.of();
    }

    // ── Действия ──────────────────────────────────────────────────────────────

    /**
     * Удаляет товар из сравнения по индексу (0-based).
     * Кнопка удаления: span.remove внутри карусели товаров.
     * Вызывает AJAX: CatalogCompareObj.MakeAjaxAction(...DELETE_FROM_COMPARE_RESULT&ID=N...).
     * После удаления ждёт, пока количество колонок уменьшится на 1.
     */
    @Step("Удалить товар из сравнения с индексом {index}")
    public ComparePage removeProduct(int index) {
        int beforeCount = getProductsCount();
        log.info("Removing product at index {}, current count={}", index, beforeCount);

        SelenideElement btn = removeButtons.get(index);
        btn.shouldBe(visible, Duration.ofSeconds(10));
        executeJavaScript("arguments[0].click()", btn);

        // Ждём AJAX-обновления страницы (уменьшение числа колонок)
        long deadline = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < deadline) {
            com.codeborne.selenide.Selenide.sleep(400);
            if (getProductsCount() < beforeCount) break;
        }
        int afterCount = getProductsCount();
        log.info("After removal: count={}", afterCount);
        return this;
    }
}
