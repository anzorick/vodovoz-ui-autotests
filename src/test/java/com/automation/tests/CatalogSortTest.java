package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.CatalogPage;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты сортировки каталога vodovoz.ru.
 *
 * Раздел: /catalog/pitevaya_voda_19_litrov/ (питьевая вода 19 литров)
 */
@Epic("vodovoz.ru")
@Feature("Каталог — Сортировка")
@DisplayName("Сортировка товаров в каталоге")
class CatalogSortTest extends BaseTest {

    private static final String CATALOG_SLUG = "pitevaya_voda_19_litrov";
    private final CatalogPage catalogPage = new CatalogPage();

    @BeforeEach
    void openCatalog() {
        log.info("Открываю каталог: /catalog/{}/", CATALOG_SLUG);
        Selenide.open("/catalog/" + CATALOG_SLUG + "/");
        catalogPage.shouldBeLoaded();
        Selenide.sleep(2000);
    }

    // ── Тест 1: Сортировка «Дешевле» ──────────────────────────────────────────

    @Test
    @Story("Сортировка по цене")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Сортировка 'Дешевле': первый товар дешевле последнего")
    @Description("""
            Шаги:
            1. Открыть раздел каталога (питьевая вода 19 л)
            2. Применить сортировку «Дешевле» (order=asc)
            3. Проверить что цены идут по возрастанию (first ≤ last)
            4. Проверить наличие товаров
            """)
    void testSortByPriceAscending() {
        catalogPage.sortBy(CatalogPage.SortOption.PRICE_ASC, CATALOG_SLUG);
        Selenide.sleep(2000);

        verifySortedAscending();
    }

    // ── Тест 2: Сортировка «Дороже» ───────────────────────────────────────────

    @Test
    @Story("Сортировка по цене")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Сортировка 'Дороже': первый товар дороже последнего")
    @Description("""
            Шаги:
            1. Открыть раздел каталога (питьевая вода 19 л)
            2. Применить сортировку «Дороже» (order=desc)
            3. Проверить что цены идут по убыванию (first ≥ last)
            4. Проверить наличие товаров
            """)
    void testSortByPriceDescending() {
        catalogPage.sortBy(CatalogPage.SortOption.PRICE_DESC, CATALOG_SLUG);
        Selenide.sleep(2000);

        verifySortedDescending();
    }

    // ── Тест 3: Параметризованный — несколько вариантов сортировки ──────────

    @Test
    @Story("Каталог загружается после каждого вида сортировки")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Каталог отображает товары после сортировки по скидке и рейтингу")
    @Description("""
            Проверяет что после сортировки «Со скидкой» и «Высокий рейтинг»
            каталог остаётся рабочим и содержит товары.
            """)
    void testSortByDiscountAndRatingShowsProducts() {
        // Со скидкой
        checkSortShowsProducts(CatalogPage.SortOption.DISCOUNT, "Со скидкой");
        Selenide.sleep(1500);

        // Высокий рейтинг
        checkSortShowsProducts(CatalogPage.SortOption.RATING, "Высокий рейтинг");
    }

    // ── Вспомогательные @Step-методы ─────────────────────────────────────────

    @Step("Проверить сортировку по возрастанию цены")
    private void verifySortedAscending() {
        catalogPage.productCountShouldBeAtLeast(2);
        catalogPage.firstProductShouldBeCheaperThanLast();
        log.info("Сортировка по возрастанию подтверждена ✓");
    }

    @Step("Проверить сортировку по убыванию цены")
    private void verifySortedDescending() {
        catalogPage.productCountShouldBeAtLeast(2);
        catalogPage.firstProductShouldBeMoreExpensiveThanLast();
        log.info("Сортировка по убыванию подтверждена ✓");
    }

    @Step("Применить сортировку '{label}' и проверить товары")
    private void checkSortShowsProducts(CatalogPage.SortOption option, String label) {
        log.info("Проверяю сортировку: {}", label);
        catalogPage.sortBy(option, CATALOG_SLUG);
        Selenide.sleep(2000);
        catalogPage.productCountShouldBeAtLeast(1);
        log.info("Сортировка '{}': товары присутствуют ✓", label);
    }
}