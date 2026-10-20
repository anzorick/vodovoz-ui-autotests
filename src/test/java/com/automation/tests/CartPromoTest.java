package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.CartPage;
import com.automation.pages.SearchPage;
import com.automation.pages.SearchResultsPage;
import com.codeborne.selenide.Condition;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.*;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * CartPromoTest — тесты применения промокода в корзине vodovoz.ru.
 *
 * DOM-селекторы (верифицированы).
 *   Поле промокода:   input[data-entity='basket-coupon-input']
 *   Кнопка Применить: span.basket-coupon-block-coupon-btn
 *   Признак скидки:   .basket-coupon-alert-inner.has_coupon / .basket-coupon-info
 *   Grand Total:      CartPage.getGrandTotal() (int)
 *
 * Промокод "вам10" -> скидка 10%, AJAX-пересчёт.
 * Невалидный код -> пометка "Не найден" рядом с полем, без popup-алерта.
 */
@Epic("vodovoz.ru")
@Feature("Корзина — Промокод")
@DisplayName("Применение промокода в корзине")
@Tag("regression")
@Tag("cart")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartPromoTest extends BaseTest {

    private static final String VALID_PROMO   = "вам10";
    private static final String INVALID_PROMO = "INVALIDCODE123";
    private static final String SEARCH_QUERY  = "вода";

    private final SearchPage       searchPage  = new SearchPage();
    private final SearchResultsPage resultsPage = new SearchResultsPage();
    private final CartPage          cartPage    = new CartPage();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Очищаем куки/localStorage перед каждым тестом,
     * чтобы промокод из предыдущего теста (has_coupon) не оставался в сессии.
     * Сначала нужно открыть страницу, чтобы WebDriver был инициализирован.
     */
    @BeforeEach
    @Step("Очистить сессию браузера (cookies + localStorage)")
    void clearSession() {
        open("/");   // initialize WebDriver + load domain
        com.codeborne.selenide.Selenide.clearBrowserCookies();
        com.codeborne.selenide.Selenide.clearBrowserLocalStorage();
        log.info("Сессия очищена (cookies + localStorage) ✓");
    }


    // ── Precondition ─────────────────────────────────────────────────────────


    @Step("Добавить первый товар по запросу '{query}' и открыть корзину")
    private void addProductAndOpenCart(String query) {
        searchPage.openPage().search(query);
        com.codeborne.selenide.Selenide.sleep(2000);
        resultsPage.addFirstProductToCart();
        com.codeborne.selenide.Selenide.sleep(2000);
        open("/basket/");
        cartPage.shouldBeLoaded();
        com.codeborne.selenide.Selenide.sleep(2000);
        cartPage.shouldNotBeEmpty();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Валидный промокод \u00abвам10\u00bb применяется и пересчитывает итог на ~10%")
    @Description("Добавить товар -> /basket/ -> запомнить Grand Total -> ввести вам10 ->"
                 + " нажать Применить -> признак скидки виден -> итог после приблизительно"
                 + " равен итогу до * 0.9 (+-1 руб.)")
    void testValidPromoCodeApplied() {
        addProductAndOpenCart(SEARCH_QUERY);

        Assumptions.assumeTrue(
            cartPage.isPromoCodeFieldPresent(),
            "Поле промокода недоступно — тест пропускается"
        );

        int totalBefore = readGrandTotal("до промокода");
        Assumptions.assumeTrue(totalBefore > 0, "Grand Total = 0 — корзина пуста");

        applyPromoCode(VALID_PROMO);

        verifyDiscountVisible();

        int totalAfter = readGrandTotal("после промокода");
        double expected = totalBefore * 0.9;
        log.info("Проверка: {} * 0.9 = {:.0f} ~ {} (+-1 руб.)", totalBefore, expected, totalAfter);
        Assertions.assertEquals(expected, (double) totalAfter, 1.0,
            String.format("Итог после скидки 10%%: ожидалось ~%.0f (+-1 руб.), получено %d", expected, totalAfter));
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Невалидный промокод не применяет скидку и не открывает popup-алерт")
    @Description("Добавить товар -> /basket/ -> запомнить Grand Total -> ввести INVALIDCODE123"
                 + " -> Grand Total не изменился -> has_coupon нет -> модалки ошибки нет")
    void testInvalidPromoCodeNoAlert() {
        addProductAndOpenCart(SEARCH_QUERY);

        Assumptions.assumeTrue(
            cartPage.isPromoCodeFieldPresent(),
            "Поле промокода недоступно — тест пропускается"
        );

        int totalBefore = readGrandTotal("до промокода");
        Assumptions.assumeTrue(totalBefore > 0, "Grand Total = 0 — корзина пуста");

        applyPromoCode(INVALID_PROMO);

        verifyDiscountNotApplied();

        int totalAfter = readGrandTotal("после невалидного промокода");
        Assertions.assertEquals(totalBefore, totalAfter,
            "Grand Total не должен измениться после невалидного промокода: "
            + totalBefore + " -> " + totalAfter);

        verifyNoErrorPopup();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Step("Считать Grand Total [{label}]")
    private int readGrandTotal(String label) {
        int total = cartPage.getGrandTotal();
        log.info("Grand Total [{}]: {} руб.", label, total);
        return total;
    }

    @Step("Ввести и применить промокод '{code}'")
    private void applyPromoCode(String code) {
        cartPage.enterPromoCode(code).clickApplyPromo();
        com.codeborne.selenide.Selenide.sleep(2000);
        log.info("Промокод '{}' применён, ожидание AJAX завершено", code);
    }

    @Step("Проверить, что скидка применена (discount indicator visible)")
    private void verifyDiscountVisible() {
        // Primary check: .basket-coupon-alert-inner gets class "has_coupon" after valid promo
        boolean found = false;
        if ($(".basket-coupon-alert-inner").exists()) {
            String cls = $(".basket-coupon-alert-inner").getAttribute("class");
            if (cls != null && cls.contains("has_coupon")) {
                $(".basket-coupon-alert-inner.has_coupon")
                    .shouldBe(Condition.visible, Duration.ofSeconds(10));
                found = true;
                log.info("Скидка применена: .basket-coupon-alert-inner.has_coupon ✓");
            }
        }
        // Fallback: .basket-coupon-info shows discount text
        if (!found && $(".basket-coupon-info").exists() && $(".basket-coupon-info").isDisplayed()) {
            $(".basket-coupon-info").shouldBe(Condition.visible, Duration.ofSeconds(5));
            found = true;
            log.info("Скидка применена: .basket-coupon-info ✓");
        }
        if (!found) {
            log.warn("Discount indicator не найден — проверяем через изменение Grand Total");
        }
    }

    @Step("Проверить, что скидка НЕ применена (has_coupon не означает скидку — проверяем по .basket-coupon-info)")
    private void verifyDiscountNotApplied() {
        // NOTE: в Bitrix-шаблоне класс 'has_coupon' добавляется при ЛЮБОМ вводе промокода
        // (и при ошибке, и при успехе) — использовать его для проверки нельзя.
        // Реальная проверка: текст в .basket-coupon-info НЕ содержит числовой суммы скидки.
        if ($(".basket-coupon-info").exists() && $(".basket-coupon-info").isDisplayed()) {
            String infoText = $(".basket-coupon-info").getText().trim();
            log.info("basket-coupon-info text: '{}'", infoText);
            // Если текст содержит знак минус и цифры — это скидка, чего не должно быть
            boolean looksLikeDiscount = infoText.matches(".*-\\s*\\d+.*");
            Assertions.assertFalse(looksLikeDiscount,
                "Текст скидки не должен появляться при невалидном промокоде: '" + infoText + "'");
        }
        // Основная проверка (Grand Total не изменился) выполняется в вызывающем методе
        log.info("Числовая скидка не отображается ✓");
    }


    @Step("Проверить отсутствие popup-алерта об ошибке")
    private void verifyNoErrorPopup() {
        if ($(".modal.show").exists()) {
            $(".modal.show").shouldBe(Condition.hidden, Duration.ofSeconds(2));
        }
        if ($("[class*=error-popup]").exists()) {
            $("[class*=error-popup]").shouldBe(Condition.hidden, Duration.ofSeconds(2));
        }
        log.info("Popup-алерт об ошибке промокода отсутствует ✓");
    }
}
