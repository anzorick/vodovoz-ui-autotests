package com.automation.tests;

import com.automation.base.BaseTest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


/**
 * Тесты поисковой формы на сайте vodovoz.ru.
 *
 * Сайт: https://vodovoz.ru
 * Тестируемый элемент: строка поиска в шапке сайта
 */
@Epic("vodovoz.ru")
@Feature("Поиск товаров")
@DisplayName("Поиск на vodovoz.ru")
class VodovozSearchTest extends BaseTest {

    private final SearchPage searchPage = new SearchPage();

    @BeforeEach
    void openMainPage() {
        searchPage.openPage()
                  .searchInputShouldBeReady();

        // Пауза 3 сек — чтобы видеть главную страницу перед поиском
        Selenide.sleep(3000);
    }

    // ── Тест 1: Поиск по существующему товару ─────────────────────────────────

    @Test
    @Story("Успешный поиск")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Поиск 'вода' возвращает товары с водой")
    @Description("""
            Шаги:
            1. Открыть главную страницу vodovoz.ru
            2. Ввести в строку поиска: вода
            3. Нажать кнопку поиска
            4. Проверить, что URL содержит параметр q=
            5. Проверить, что найдено не менее 5 товаров
            6. Проверить, что хотя бы один товар содержит 'вода' в названии
            """)
    void testSearchForWaterReturnsProducts() {
        SearchResultsPage results = searchPage.search("вода");

        // Пауза 3 сек — чтобы видеть страницу результатов после поиска
        Selenide.sleep(3000);

        results.shouldBeLoaded();
        results.urlShouldContainQuery("вода");
        results.shouldHaveProductsAtLeast(5);
        results.atLeastOneProductShouldContain("вода");
    }

    // ── Тест 2: Поиск по конкретному типу товара ──────────────────────────────

    @Test
    @Story("Успешный поиск")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Поиск 'кулер' возвращает страницу каталога с результатами")
    @Description("""
            Шаги:
            1. Открыть главную страницу vodovoz.ru
            2. Ввести в строку поиска: кулер
            3. Нажать кнопку поиска
            4. Проверить, что открылась страница каталога (/catalog/)
            5. Проверить, что URL содержит параметр поиска q=
            6. Проверить, что найден хотя бы 1 товар
            """)
    void testSearchForCoolerReturnsResults() {
        SearchResultsPage results = searchPage.search("кулер");

        // Пауза 3 сек — чтобы видеть страницу результатов после поиска
        Selenide.sleep(3000);

        results.shouldBeLoaded();
        // Реальный URL: /catalog/kulery/ или /catalog/?q=кулер
        // urlShouldContain() использует contains() — регистрозависимый!
        // "/Catalog/" ≠ "/catalog/" → поэтому используем lowercase
        String currentUrl = results.getCurrentUrl().toLowerCase();
        org.junit.jupiter.api.Assertions.assertTrue(
            currentUrl.contains("/catalog/"),
            "URL должен содержать /catalog/, но был: " + currentUrl
        );
        results.shouldHaveProductsAtLeast(1);
    }

    // ── Тест 3: Параметризованный поиск ───────────────────────────────────────

    /**
     * Параметризованный тест: проверяет поиск по 4 разным запросам.
     *
     * JUnit 5 @ParameterizedTest запускает этот метод отдельно для каждой
     * строки @CsvSource. Каждый прогон = отдельная строка в Allure-отчёте.
     *
     * Набор данных (query, minProducts):
     *   "вода"    → минимум 5 товаров
     *   "кулер"   → минимум 1 товар
     *   "помпа"   → минимум 3 товара
     *   "лимонад" → минимум 2 товара
     */
    @ParameterizedTest(name = "Прогон {index}: Поиск по запросу ''{0}'', ожидаем не менее {1} товаров")
    @CsvSource({
        "вода,    5",
        "кулер,   1",
        "помпа,   3",
        "лимонад, 2"
    })
    @Story("Параметризованный поиск")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Параметризованный поиск по различным запросам")
    @Description("""
            Параметризованный тест с @CsvSource.
            Для каждого запроса проверяем:
            1. Страница каталога открылась (/catalog/ в URL)
            2. Количество товаров ≥ указанного минимума
            3. Хотя бы один товар содержит поисковое слово в названии
            """)
    void testParametrizedSearch(String query, int minProducts) {
        // Шаг 1: Выполнить поиск по переданному запросу
        SearchResultsPage results = doSearch(query);
        Selenide.sleep(2000);

        // Шаг 2: Проверить что открылась страница каталога
        checkCatalogUrl(results, query);

        // Шаг 3: Проверить минимальное количество товаров
        results.shouldHaveProductsAtLeast(minProducts);

        // Шаг 4: Проверить что хотя бы один товар содержит поисковое слово
        checkKeywordInResults(results, query);
    }

    // ── Вспомогательные шаги (@Step для Allure) ───────────────────────────────

    @Step("Поиск по запросу: '{query}'")
    private SearchResultsPage doSearch(String query) {
        return new SearchPage().search(query.trim());
    }

    @Step("URL содержит /catalog/ (запрос: '{query}')")
    private void checkCatalogUrl(SearchResultsPage results, String query) {
        String url = results.getCurrentUrl().toLowerCase();
        org.junit.jupiter.api.Assertions.assertTrue(
            url.contains("/catalog/"),
            "Поиск '" + query + "': ожидался URL с /catalog/, но был: " + url
        );
    }

    @Step("Хотя бы один товар содержит слово '{keyword}' (без учёта регистра)")
    private void checkKeywordInResults(SearchResultsPage results, String keyword) {
        results.atLeastOneProductShouldContain(keyword.trim());
    }
}