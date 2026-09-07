package com.automation.tests;

import com.automation.base.BaseTest;
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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * E2E-тест: Поиск → Добавление в корзину → Проверка корзины (vodovoz.ru).
 *
 * Сценарий:
 *   1. Открыть главную страницу
 *   2. Найти товар через строку поиска
 *   3. Запомнить название первого товара
 *   4. Нажать «В корзину»
 *   5. Дождаться AJAX-обновления счётчика корзины
 *   6. Перейти на страницу /basket/
 *   7. Проверить, что товар в корзине присутствует
 */
@Epic("vodovoz.ru")
@Feature("Корзина")
@DisplayName("E2E: Поиск → Корзина")
class AddToCartTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AddToCartTest.class);

    private final SearchPage       searchPage   = new SearchPage();
    private final SearchResultsPage resultsPage = new SearchResultsPage();
    private final CartPage         cartPage     = new CartPage();

    /** Поисковый запрос — товар с простой кнопкой «В корзину» без выбора параметров */
    private static final String SEARCH_QUERY = "вода питьевая";

    @BeforeEach
    void openMainPage() {
        searchPage.openPage().searchInputShouldBeReady();
        Selenide.sleep(2000); // пауза — видим главную страницу
    }

    // ── E2E Тест ─────────────────────────────────────────────────────────────

    @Test
    @Story("Добавление товара в корзину")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("E2E: Поиск 'вода питьевая' → В корзину → Корзина содержит товар")
    @Description("""
            E2E-сценарий:
            1. Открыть главную страницу vodovoz.ru
            2. Ввести в строку поиска: вода питьевая
            3. Дождаться результатов (не менее 1 товара)
            4. Запомнить название первого товара
            5. Нажать кнопку «В корзину» первого товара
            6. Убедиться что счётчик корзины в шапке стал > 0
            7. Открыть страницу корзины /basket/
            8. Проверить, что корзина не пуста
            9. Проверить, что добавленный товар присутствует в корзине
            """)
    void testSearchAndAddToCart() {
        // ── Шаг 1: Поиск товара ───────────────────────────────────────────────
        SearchResultsPage results = searchPage.search(SEARCH_QUERY);
        Selenide.sleep(2000); // видим результаты поиска

        results.shouldBeLoaded();
        results.shouldHaveProductsAtLeast(1);

        // ── Шаг 2: Запомнить название первого товара ──────────────────────────
        String productName = rememberFirstProduct(results);
        log.info("Добавляем в корзину: '{}'", productName);

        // ── Шаг 3: Добавить в корзину ─────────────────────────────────────────
        results.addFirstProductToCart();
        Selenide.sleep(2000); // видим обновлённый счётчик

        // ── Шаг 4: Проверить счётчик корзины в шапке ─────────────────────────
        results.cartCounterShouldBePositive();
        log.info("Счётчик корзины обновился до: {}", results.getCartCounterText());

        // ── Шаг 5: Открыть корзину ────────────────────────────────────────────
        cartPage.open();
        Selenide.sleep(2000); // видим страницу корзины

        // ── Шаг 6: Проверить корзину ──────────────────────────────────────────
        cartPage.shouldBeLoaded();
        cartPage.shouldNotBeEmpty();
        cartPage.shouldContainProduct(extractKeyword(productName));

        log.info("E2E-тест пройден: '{}' успешно добавлен в корзину ✓", productName);
    }

    // ── Вспомогательные шаги ─────────────────────────────────────────────────

    @Step("Запомнить название первого товара в результатах поиска")
    private String rememberFirstProduct(SearchResultsPage results) {
        String name = results.getFirstProductTitle();
        assertFalse(name.isEmpty(), "Название первого товара не должно быть пустым");
        log.info("Первый товар: '{}'", name);
        return name;
    }

    /**
     * Извлекает ключевое слово из полного названия товара для поиска в корзине.
     * Например: "Вода питьевая BonAqua 0.5 л" → "BonAqua" или "Вода"
     */
    private String extractKeyword(String fullName) {
        if (fullName == null || fullName.isBlank()) return "вода";
        // Берём первые два слова названия
        String[] words = fullName.trim().split("\\s+");
        return words.length >= 2 ? words[0] + " " + words[1] : words[0];
    }
}