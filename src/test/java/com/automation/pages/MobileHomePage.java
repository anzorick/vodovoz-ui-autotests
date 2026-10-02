package com.automation.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;

/**
 * Page Object for mobile (375x667) vodovoz.ru homepage and navigation.
 *
 * DOM selectors verified by live 375px inspection:
 *
 *   Burger button:      div.burger                    (visible only at <=1200px)
 *   Mobile menu:        #mobilemenu                   (gets class 'show' when open)
 *   Menu links:         #mobilemenu a.dark_link        (nav links inside drawer)
 *   Cart icon:          div.header-cart               (same as desktop)
 *   Search form:        form[name="search"]            (same as desktop, visible on mobile)
 *   Search input:       #title-search-input           (same as desktop)
 *   Product cards:      .catalog-block__wrapper       (same as desktop)
 *   Product title:      .catalog-block__info-title a  (same as desktop)
 *
 * IMPORTANT: vodovoz.ru switches layout ONLY by CSS width, NOT by userAgent.
 * Desktop Chrome userAgent is sufficient — no mobile emulation needed.
 *
 * Overlay management:
 *   - Cookie/delivery popup: try ESC + click close buttons
 *   - No separate mobile overlays found in inspection
 */
public class MobileHomePage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(MobileHomePage.class);

    // ── Burger & drawer ───────────────────────────────────────────────────────

    /** Burger-menu toggle button (appears at width <= 1200px) */
    private final SelenideElement burgerBtn = $("div.burger");

    /**
     * Mobile menu drawer: id="mobilemenu".
     * Gets class 'show' after burger click.
     */
    private final SelenideElement mobileMenu = $("#mobilemenu");

    /**
     * Navigation links inside mobile drawer.
     */
    private final ElementsCollection menuLinks = $$("#mobilemenu a.dark_link");

    // ── Header elements ───────────────────────────────────────────────────────

    /** Cart icon in header */
    private final SelenideElement cartIcon = $("div.header-cart");

    // ── Search ────────────────────────────────────────────────────────────────

    private final SelenideElement searchInput = $("#title-search-input");

    // ── Catalog ───────────────────────────────────────────────────────────────

    private final ElementsCollection productCards = $$(".catalog-block__wrapper");
    private final ElementsCollection productTitles = $$(".catalog-block__info-title a");

    // ── Navigation ────────────────────────────────────────────────────────────

    @Step("Open homepage at mobile viewport")
    public MobileHomePage openHomePage() {
        open("/");
        $("body").shouldBe(visible, Duration.ofSeconds(10));
        return this;
    }

    @Step("Open catalog category: {path}")
    public MobileHomePage openCatalogPage(String path) {
        open(path);
        return this;
    }

    // ── Overlays ──────────────────────────────────────────────────────────────

    /**
     * Closes any overlays/popups that may interfere with mobile testing.
     * Specifically handles:
     *   - Marketing popup: div.marketing-popup / .popup-text-info (confirmed blocking clicks)
     *   - Cookie banner, delivery zone popup
     * Strategy: ESC first, then JS force-hide the marketing popup, then iterate close buttons.
     */
    @Step("Close any overlays/popups (if present)")
    public MobileHomePage closeOverlaysIfPresent() {
        // ESC first
        try {
            com.codeborne.selenide.Selenide.actions()
                .sendKeys(org.openqa.selenium.Keys.ESCAPE).perform();
            com.codeborne.selenide.Selenide.sleep(400);
        } catch (Exception ignored) {}

        // JS: force-hide the marketing popup that is confirmed to block clicks on mobile
        try {
            executeJavaScript(
                "var overlays = document.querySelectorAll(" +
                "  '.marketing-popup, .popup-text-info, [class*=marketing-popup]'" +
                ");" +
                "overlays.forEach(function(el) {" +
                "  var closeBtn = el.querySelector('.mfp-close, .close, [class*=close]');" +
                "  if (closeBtn) { closeBtn.click(); } else { el.style.display='none'; }" +
                "});"
            );
            com.codeborne.selenide.Selenide.sleep(400);
        } catch (Exception ignored) {}

        // Also click any visible close buttons via Selenide
        String[] selectors = {
            ".mfp-close",
            ".popup-closer",
            ".popup-text-info .close",
            ".marketing-popup .close",
            "[class*='popup'][class*='close']",
            "[class*='modal'] .close",
            "button.close",
            ".cookie-notice__close",
            ".alert .close"
        };
        for (String sel : selectors) {
            try {
                SelenideElement el = $(sel);
                if (el.exists() && el.isDisplayed()) {
                    executeJavaScript("arguments[0].click()", el);
                    log.info("Closed overlay via JS click: {}", sel);
                    com.codeborne.selenide.Selenide.sleep(300);
                }
            } catch (Exception ignored) {}
        }
        log.info("Overlays closed");
        return this;
    }

    // ── Burger menu ───────────────────────────────────────────────────────────

    @Step("Burger button should be visible")
    public MobileHomePage burgerShouldBeVisible() {
        burgerBtn.shouldBe(visible, Duration.ofSeconds(10));
        log.info("Burger button is visible on mobile viewport");
        return this;
    }

    @Step("Click burger button to open mobile menu")
    public MobileHomePage clickBurger() {
        burgerBtn.shouldBe(visible, Duration.ofSeconds(10)).click();
        log.info("Burger clicked");
        return this;
    }

    @Step("Mobile menu drawer should be open (has class 'show')")
    public MobileHomePage menuShouldBeOpen() {
        // After burger click, #mobilemenu gains class 'show'
        mobileMenu.shouldBe(Condition.cssClass("show"), Duration.ofSeconds(10));
        log.info("Mobile menu is open, links count: {}", menuLinks.size());
        return this;
    }

    @Step("Mobile menu should have at least {minLinks} navigation links")
    public MobileHomePage menuShouldHaveLinks(int minLinks) {
        int actual = menuLinks.size();
        log.info("Menu links found: {}", actual);
        org.junit.jupiter.api.Assertions.assertTrue(
            actual >= minLinks,
            "Expected at least " + minLinks + " links in mobile menu, got: " + actual
        );
        return this;
    }

    @Step("Клик по мобильному меню до фактического перехода в каталог (двухуровневое меню)")
    public String clickFirstMenuLink() {
        String startUrl = com.codeborne.selenide.WebDriverRunner.url();

        // Уровень 1: если меню на верхнем уровне — зайти в секцию «Каталог» (переключатель панели, без перехода)
        SelenideElement catalogSwitcher = com.codeborne.selenide.Selenide
                .$("#mobilemenu a.dark_link[href='/catalog/']");
        if (catalogSwitcher.is(com.codeborne.selenide.Condition.visible, java.time.Duration.ofSeconds(2))) {
            catalogSwitcher.click();
            log.info("Кликнул 'Каталог': секция каталога раскрыта внутри меню");
        }

        // Уровень 2: кликать по категориям, пока не случится реальный переход
        for (int i = 0; i < 6; i++) {
            ElementsCollection links = com.codeborne.selenide.Selenide.$$(
                            "#mobilemenu a.dark_link[href*='/catalog/']:not([href='/catalog/']):not([href*='compare']):not([href*='.php'])")
                    .filter(com.codeborne.selenide.Condition.visible);
            if (i >= links.size()) {
                break;
            }
            SelenideElement link = links.get(i);
            String href = link.getAttribute("href");
            log.info("Кликаю по ссылке категории #{}: {}", i, href);
            link.scrollIntoView(true);
            link.click(com.codeborne.selenide.ClickOptions.usingJavaScript());
            for (int t = 0; t < 10; t++) {
                com.codeborne.selenide.Selenide.sleep(300);
                String now = com.codeborne.selenide.WebDriverRunner.url();
                if (!now.equals(startUrl)) {
                    log.info("Произошёл переход: {}", now);
                    return href;
                }
            }
            log.info("Ссылка #{} не дала перехода (переключатель подраздела?), пробую следующую", i);
        }
        throw new AssertionError("Ни одна из первых ссылок мобильного меню не привела к переходу; URL остался: "
                + com.codeborne.selenide.WebDriverRunner.url());
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Step("Mobile search input should be visible")
    public MobileHomePage searchShouldBeVisible() {
        searchInput.shouldBe(visible, Duration.ofSeconds(10));
        log.info("Search input visible on mobile");
        return this;
    }

    @Step("Type search query on mobile: {query}")
    public MobileHomePage typeSearchQuery(String query) {
        searchInput.shouldBe(visible, Duration.ofSeconds(10));
        searchInput.shouldBe(Condition.enabled, Duration.ofSeconds(10));
        searchInput.setValue(query);
        log.info("Typed query: {}", query);
        return this;
    }

    @Step("Submit mobile search (press Enter)")
    public MobileHomePage submitSearch() {
        searchInput.pressEnter();
        log.info("Search submitted via Enter");
        return this;
    }

    // ── Product card ──────────────────────────────────────────────────────────

    @Step("Product cards should be visible (at least {minCount})")
    public MobileHomePage productCardsShouldBeVisible(int minCount) {
        productCards.first().shouldBe(visible, Duration.ofSeconds(10));
        int actual = productCards.size();
        log.info("Product cards visible: {}", actual);
        org.junit.jupiter.api.Assertions.assertTrue(
            actual >= minCount,
            "Expected at least " + minCount + " product cards, got: " + actual
        );
        return this;
    }

    @Step("Click first product card title via JS (bypass overlay)")
    public String clickFirstProductTitle() {
        SelenideElement title = productTitles.filter(visible).first();
        title.shouldBe(visible, Duration.ofSeconds(10));
        String href = title.getAttribute("href");
        String text = title.getText().trim();
        log.info("JS-clicking product: '{}', href='{}'", text, href);
        // JS click bypasses the marketing popup overlay that intercepts regular clicks
        executeJavaScript("arguments[0].click()", title);
        return href;
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    @Step("Page should have product name visible")
    public MobileHomePage productNameShouldBeVisible() {
        // h1 is present on all vodovoz.ru PDPs (verified by live inspection).
        // Fallback: .char-side is also confirmed in SearchResultsPage.openFirstProduct()
        $("h1, .catalog-element__name, .catalog-element__title, .char-side")
            .shouldBe(visible, Duration.ofSeconds(15));
        log.info("Product name visible on mobile PDP");
        return this;
    }

    @Step("Page should have price visible")
    public MobileHomePage productPriceShouldBeVisible() {
        // .price__new-val verified across SearchResultsPage, CartPage, ProductCardTest
        $(".price__new-val, .price__new, [class*=price__new]")
            .shouldBe(visible, Duration.ofSeconds(15));
        log.info("Product price visible on mobile PDP");
        return this;
    }

    @Step("Page should have 'Add to cart' button visible")
    public MobileHomePage addToCartButtonShouldBeVisible() {
        // span.to_cart[data-action='basket'] verified across AddToCartTest, CartQuantityTest
        $("[data-action='basket'], span.to_cart, .js-item-action[data-action='basket']")
            .shouldBe(visible, Duration.ofSeconds(15));
        log.info("Add-to-cart button visible on mobile PDP");
        return this;
    }

    @Step("Cart icon should be visible in mobile header")
    public MobileHomePage cartIconShouldBeVisible() {
        cartIcon.shouldBe(visible, Duration.ofSeconds(10));
        log.info("Cart icon visible on mobile");
        return this;
    }

    @Step("URL should contain: {fragment}")
    public void urlShouldContainFragment(String fragment) {
        urlShouldContain(fragment);
    }
}
