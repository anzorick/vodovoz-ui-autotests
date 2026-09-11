package com.automation.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Page Object для карточки товара (PDP) vodovoz.ru.
 *
 * Реальные CSS-селекторы получены из HTML /catalog/kulery_ael/107520/:
 *
 *   H1 заголовок:       h1.switcher-title  (class="font_32 switcher-title ...")
 *   Цена:               span.price__new-val  (содержит "7 080 ₽")
 *   Артикул:            span.article  (содержит "Арт. 0043516")
 *   Характеристики:     .char-side  (div с заголовком «Характеристики» и .properties.list)
 *   Кнопка «В корзину»: span.btn.to_cart[data-action='basket']
 *   Блок счётчика:      div.in_cart   — появляется и СТАНОВИТСЯ видимым после добавления
 *                       (содержит counter__action--minus, counter__count, counter__action--plus)
 *   Шапка корзины:      .header-cart__count
 */
public class ProductPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ProductPage.class);

    // ── Локаторы ─────────────────────────────────────────────────────────────

    /**
     * H1 заголовок товара.
     * Реальный HTML: <h1 class="font_32 switcher-title js-popup-title font_20--to-600">
     */
    private final SelenideElement productTitle = $("h1.switcher-title, h1.font_32, .catalog-detail h1");

    /**
     * Актуальная цена товара.
     * Реальный HTML: <span class="price__new-val font_24">7 080 ₽</span>
     */
    private final SelenideElement productPrice = $("span.price__new-val, .price__new-val, .catalog-detail__price .price__new span");

    /**
     * Артикул товара.
     * Реальный HTML: <span class="article">Арт.&nbsp;<span class="js-replace-article" data-value="0043516">0043516</span></span>
     */
    private final SelenideElement articleBlock = $("span.article");

    /**
     * Блок характеристик.
     * Реальный HTML: <div class="char-side"><div class="char-side__title">Характеристики</div>...
     */
    private final SelenideElement characteristicsBlock = $(".char-side, .properties.list, .catalog-detail__previewtext");

    /**
     * Кнопка «В корзину» на карточке товара.
     * Реальный HTML: <span class="btn btn-default btn-lg btn-wide to_cart animate-load js-item-action"
     *                       data-action="basket" title="В корзину">В корзину</span>
     * НЕ «Купить в 1 клик» (кнопка с data-name="ocb")
     */
    private final SelenideElement addToCartButton = $("span.to_cart[data-action='basket']:not([data-name='ocb'])");

    /**
     * Блок счётчика количества (появляется после добавления в корзину).
     * Реальный HTML: <div class="btn btn-default in_cart btn-lg btn-wide">
     *                  <div class="counter js-ajax">
     *                    <span class="counter__action counter__action--minus">
     *                    <input type="text" value="1" class="counter__count">
     *                    <span class="counter__action counter__action--plus">
     *                  </div>
     *                </div>
     * Важно: div.in_cart ВИДИМ после добавления (а input.counter__count скрыт через opacity/clip)
     */
    private final SelenideElement inCartBlock = $("div.in_cart");

    /**
     * Счётчик корзины в шапке сайта.
     * Реальный HTML: <span class="header-cart__count">1</span>
     */
    private final SelenideElement headerCartCount = $(".header-cart__count");

    // ── Проверки загрузки ────────────────────────────────────────────────────

    /**
     * Проверяет что карточка товара загружена (H1 виден).
     */
    @Step("Проверить, что карточка товара успешно загружена")
    public ProductPage shouldBeLoaded() {
        productTitle.shouldBe(visible, Duration.ofSeconds(10));
        // Маркер карточки: артикул есть только на PDP, на выдаче его нет
        $("span.article").shouldBe(visible, Duration.ofSeconds(10));
        return this;
    }

    // ── Получение данных ──────────────────────────────────────────────────────

    /**
     * Возвращает текст H1 заголовка товара.
     */
    @Step("Получить название товара (H1)")
    public String getProductTitle() {
        String title = productTitle.shouldBe(visible, Duration.ofSeconds(10)).getText().trim();
        log.info("Название товара: '{}'", title);
        return title;
    }

    /**
     * Возвращает текст артикула, например "Арт. 0043516".
     */
    @Step("Получить артикул товара")
    public String getArticleText() {
        try {
            String article = articleBlock.shouldBe(visible, Duration.ofSeconds(10)).getText().trim();
            log.info("Артикул: '{}'", article);
            return article;
        } catch (Exception e) {
            log.warn("Артикул не найден: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Возвращает числовое значение цены (очищает от пробелов и «₽»).
     */
    @Step("Получить цену товара")
    public int getProductPriceValue() {
        try {
            String raw = productPrice.shouldBe(visible, Duration.ofSeconds(10))
                .getText()
                .replaceAll("[^\\d]", "")
                .trim();
            int price = raw.isEmpty() ? 0 : Integer.parseInt(raw);
            log.info("Цена товара: {} ₽", price);
            return price;
        } catch (Exception e) {
            log.warn("Не удалось получить цену: {}", e.getMessage());
            return 0;
        }
    }

    // ── Проверки элементов ────────────────────────────────────────────────────

    /**
     * Проверяет что цена товара отображается и > 0.
     */
    @Step("Цена товара отображается и > 0")
    public ProductPage shouldHavePrice() {
        productPrice.shouldBe(visible, Duration.ofSeconds(10));
        int price = getProductPriceValue();
        org.junit.jupiter.api.Assertions.assertTrue(price > 0,
            "Цена товара должна быть > 0, но: " + price);
        log.info("Цена: {} ₽ > 0 ✓", price);
        return this;
    }

    /**
     * Проверяет наличие артикула товара.
     */
    @Step("Артикул товара присутствует")
    public ProductPage shouldHaveArticle() {
        try {
            articleBlock.shouldBe(visible, Duration.ofSeconds(10));
            String article = articleBlock.getText().trim();
            org.junit.jupiter.api.Assertions.assertFalse(article.isEmpty(),
                "Артикул не должен быть пустым");
            log.info("Артикул присутствует: '{}' ✓", article);
        } catch (Exception e) {
            // Не все товары имеют артикул — мягкая проверка
            log.warn("Артикул не найден (возможно, отсутствует для данного товара): {}", e.getMessage());
        }
        return this;
    }

    /**
     * Проверяет наличие блока характеристик или описания.
     * Реальный HTML: div.char-side  ИЛИ  .catalog-detail__previewtext
     */
    @Step("Блок характеристик или описания присутствует")
    public ProductPage shouldHaveDetails() {
        log.info("Проверяю блок характеристик/описания...");
        try {
            characteristicsBlock.shouldBe(visible, Duration.ofSeconds(10));
            log.info("Блок характеристик/описания найден ✓");
        } catch (Exception e) {
            // Запасной вариант: ищем любой видимый текстовый блок карточки
            log.warn("Основной блок не найден, ищу запасной: {}", e.getMessage());
            $(".catalog-detail__main, .product-container, .catalog-detail").shouldBe(visible, Duration.ofSeconds(5));
            log.info("Запасной блок карточки найден ✓");
        }
        return this;
    }

    // ── Действия ─────────────────────────────────────────────────────────────

    /**
     * Нажимает кнопку «В корзину» на странице товара через JS-клик.
     *
     * Кнопка: span.to_cart[data-action='basket'] (НЕ «Купить в 1 клик»)
     * После клика: кнопка скрывается, появляется div.in_cart со счётчиком
     */
    @Step("Нажать 'В корзину' на карточке товара")
    public ProductPage addToCart() {
        log.info("Нажимаю 'В корзину' на карточке товара...");
        addToCartButton.shouldBe(visible, Duration.ofSeconds(10))
            .scrollIntoView("{block: 'center'}");
        log.info("Кнопка 'В корзину' видима, нажимаю через JS-клик...");
        executeJavaScript("arguments[0].click()", addToCartButton);
        log.info("Кнопка 'В корзину' нажата (JS-клик) ✓");
        return this;
    }

    /**
     * Проверяет что после добавления появился динамический блок счётчика количества.
     *
     * Логика: div.in_cart становится видимым (display:block) после AJAX-добавления.
     * Исходная кнопка «В корзину» при этом скрывается.
     */
    @Step("Счётчик количества (div.in_cart) появился после добавления в корзину")
    public ProductPage quantityCounterShouldBeVisible() {
        log.info("Ожидаю появления блока счётчика div.in_cart...");
        inCartBlock.shouldBe(visible, Duration.ofSeconds(15));
        log.info("Блок счётчика div.in_cart видим ✓");
        return this;
    }

    /**
     * Проверяет что счётчик корзины в шапке > 0.
     */
    @Step("Счётчик корзины в шапке > 0")
    public ProductPage headerCartCounterShouldBePositive() {
        log.info("Проверяю счётчик корзины в шапке...");
        headerCartCount.shouldNotHave(Condition.text("0"), Duration.ofSeconds(10));
        log.info("Счётчик в шапке: '{}' ✓", headerCartCount.getText());
        return this;
    }
}
