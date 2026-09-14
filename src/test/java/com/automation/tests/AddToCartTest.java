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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * E2E-тест: Поиск → Добавление в корзину → Проверка корзины (vodovoz.ru).
 *
 * <p>Параметризованный тест запускается для нескольких поисковых запросов,
 * гарантируя работоспособность корзины с разными типами товаров.
 */
@Epic("vodovoz.ru")
@Feature("Корзина")
@DisplayName("E2E: Поиск → Корзина")
@Tag("smoke")
@Tag("regression")
@Tag("cart")
class AddToCartTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AddToCartTest.class);

    private final SearchPage        searchPage  = new SearchPage();
    private final SearchResultsPage resultsPage = new SearchResultsPage();
    private final CartPage          cartPage    = new CartPage();

    @BeforeEach
    void openMainPage() {
        searchPage.openPage().searchInputShouldBeReady();
        Selenide.sleep(2000);
    }

    // ── E2E Параметризованный тест ────────────────────────────────────────────

    @ParameterizedTest(name = "E2E корзина: товар по запросу ''{0}''")
    @CsvSource({"вода питьевая", "кулер"})
    @Story("Добавление товара в корзину")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("E2E: Поиск → В корзину → Корзина содержит товар")
    @Description("""
            E2E-сценарий (параметризован по поисковому запросу):
            1. Открыть главную страницу vodovoz.ru
            2. Ввести в строку поиска переданный query
            3. Дождаться результатов (не менее 1 товара)
            4. Запомнить название первого товара
            5. Нажать кнопку «В корзину» первого товара
            6. Убедиться что счётчик корзины в шапке стал > 0
            7. Открыть страницу корзины /basket/
            8. Проверить, что корзина не пуста
            9. Проверить, что добавленный товар присутствует в корзине
            """)
    void testSearchAndAddToCart(String query) {
        // ── Шаг 1: Поиск товара ───────────────────────────────────────────────
        SearchResultsPage results = searchPage.search(query);
        Selenide.sleep(2000);

        results.shouldBeLoaded();
        results.shouldHaveProductsAtLeast(1);

        // ── Шаг 2: Запомнить название первого товара ──────────────────────────
        String productName = rememberFirstProduct(results);
        log.info("Запрос='{}', добавляем: '{}'", query, productName);

        // ── Шаг 3: Добавить в корзину ─────────────────────────────────────────
        results.addFirstProductToCart();
        Selenide.sleep(2000);

        // ── Шаг 4: Проверить счётчик корзины в шапке ─────────────────────────
        results.cartCounterShouldBePositive();
        log.info("Счётчик корзины: {}", results.getCartCounterText());

        // ── Шаг 5: Открыть корзину ────────────────────────────────────────────
        cartPage.open();
        Selenide.sleep(2000);

        // ── Шаг 6: Проверить корзину ──────────────────────────────────────────
        cartPage.shouldBeLoaded();
        cartPage.shouldNotBeEmpty();
        cartPage.shouldContainProduct(extractKeyword(productName));

        log.info("E2E-тест пройден: '{}' добавлен в корзину ✓", productName);
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
     * Извлекает ключевое слово (первые два слова) из полного названия товара.
     * Например: «Вода питьевая BonAqua 0.5 л» → «Вода питьевая»
     */
    private String extractKeyword(String fullName) {
        if (fullName == null || fullName.isBlank()) return "вода";
        String[] words = fullName.trim().split("\\s+");
        return words.length >= 2 ? words[0] + " " + words[1] : words[0];
    }
}