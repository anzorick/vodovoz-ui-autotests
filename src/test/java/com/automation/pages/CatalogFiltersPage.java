package com.automation.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object для страницы каталога с фильтрами vodovoz.ru.
 *
 * <h3>Архитектура фильтров (выявлена через DOM-инспектор):</h3>
 * <ul>
 *   <li><b>Chip-навигация</b> — горизонтальные кнопки-ссылки под заголовком раздела
 *       (class: {@code a.chip.chip--toggle}), ведут на подразделы категории
 *       (напр., /catalog/pitevaya_voda_19_litrov/v_odnorazovoy_tare/).
 *       Паттерн «активного» chip: текущий URL содержит slug подраздела.</li>
 *   <li><b>Dropdown-фильтры (SmartFilter Bitrix)</b> — горизонтальная полоса
 *       кнопок с выпадающими списками (class: {@code .bx_filter_parameters_box}).
 *       Работают через AJAX-перезагрузку и изменение URL.</li>
 *   <li><b>Товары</b>: {@code .catalog-block__wrapper}</li>
 * </ul>
 */
public class CatalogFiltersPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(CatalogFiltersPage.class);

    // ── Локаторы ─────────────────────────────────────────────────────────────

    /** Карточки товаров в каталоге */
    private final ElementsCollection productCards = $$(".catalog-block__wrapper");

    /**
     * Chip-кнопки — навигационные ссылки под заголовком раздела.
     * Пример: «В многоразовой таре», «В одноразовой таре».
     * class: chip chip--toggle bg-theme-active color-theme-hover-no-active
     */
    private final ElementsCollection chipButtons = $$("a.chip");

    /**
     * Dropdown-кнопки Bitrix SmartFilter (верхняя панель фильтров).
     * Содержат «Бренд», «Тип воды», «Цена» и т.д.
     */
    private final ElementsCollection filterDropdowns = $$(".bx_filter_parameters_box");

    // ── Навигация ─────────────────────────────────────────────────────────────

    /**
     * Открывает указанный раздел каталога.
     *
     * @param catalogPath путь относительно baseUrl, например
     *                    {@code "/catalog/pitevaya_voda_19_litrov/"}
     */
    @Step("Открыть страницу каталога: {catalogPath}")
    public CatalogFiltersPage openCategory(String catalogPath) {
        log.info("Открываю каталог: {}", catalogPath);
        open(catalogPath);
        shouldBeLoaded();
        return this;
    }

    /**
     * Применяет chip-фильтр — кликает по chip-ссылке, чей текст совпадает
     * с {@code chipText}, и ждёт перехода на страницу подраздела.
     *
     * @param chipText отображаемый текст chip-кнопки (например, «В одноразовой таре»)
     */
    @Step("Применить chip-фильтр: {chipText}")
    public CatalogFiltersPage applyChipFilter(String chipText) {
        log.info("Применяю chip-фильтр: {}", chipText);
        chipButtons.shouldBe(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(10));
        for (SelenideElement chip : chipButtons) {
            if (chip.isDisplayed() && chip.getText().trim().equalsIgnoreCase(chipText.trim())) {
                String href = chip.getAttribute("href");
                log.info("Chip '{}' -> href: {}", chipText, href);
                chip.click();
                shouldBeLoaded();
                return this;
            }
        }
        // fallback: navigateByChipHref
        throw new AssertionError("Chip-кнопка с текстом '" + chipText + "' не найдена");
    }

    /**
     * Применяет фильтр путём прямой навигации по URL подраздела.
     * Более надёжный метод — не зависит от JS-анимации dropdown.
     *
     * @param filterPath URL подраздела, например
     *                   {@code "/catalog/pitevaya_voda_19_litrov/v_odnorazovoy_tare/"}
     */
    @Step("Перейти по URL фильтра: {filterPath}")
    public CatalogFiltersPage navigateToFilterUrl(String filterPath) {
        log.info("Навигация к URL фильтра: {}", filterPath);
        open(filterPath);
        shouldBeLoaded();
        return this;
    }

    // ── Проверки ─────────────────────────────────────────────────────────────

    /**
     * Проверяет, что текущий URL содержит указанный slug (фильтр применён).
     *
     * @param urlPart подстрока, которую должен содержать текущий URL
     */
    @Step("URL должен содержать слаг фильтра: {urlPart}")
    public CatalogFiltersPage shouldBeFilterApplied(String urlPart) {
        String currentUrl = getCurrentUrl();
        log.info("Проверяю URL: '{}' содержит '{}'", currentUrl, urlPart);
        assertTrue(
            currentUrl.contains(urlPart),
            String.format("URL '%s' не содержит ожидаемый слаг фильтра '%s'", currentUrl, urlPart)
        );
        log.info("Фильтр применён ✓ (URL содержит '{}')", urlPart);
        return this;
    }

    /**
     * Проверяет, что chip-кнопка с указанным текстом присутствует на странице.
     * На результирующей странице подраздела chip-навигация также присутствует.
     *
     * @param chipText текст chip-кнопки для проверки
     */
    @Step("Chip-кнопка '{chipText}' должна быть видима")
    public CatalogFiltersPage chipShouldBeVisible(String chipText) {
        chipButtons.shouldBe(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(10));
        boolean found = false;
        for (SelenideElement chip : chipButtons) {
            if (chip.isDisplayed() && chip.getText().trim().equalsIgnoreCase(chipText.trim())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Chip-кнопка с текстом '" + chipText + "' не найдена среди видимых chips");
        log.info("Chip-кнопка '{}' видима ✓", chipText);
        return this;
    }

    /**
     * Проверяет, что список товаров не пуст (видны карточки товаров).
     */
    @Step("Список товаров не пуст (товары видны)")
    public CatalogFiltersPage productsShouldBeVisible() {
        productCards.shouldBe(
            CollectionCondition.sizeGreaterThan(0),
            Duration.ofSeconds(15)
        );
        log.info("Товары видны: {} шт. ✓", productCards.size());
        return this;
    }

    /**
     * Проверяет что каталог загружен (товары видны на странице).
     */
    @Step("Каталог загружен")
    public CatalogFiltersPage shouldBeLoaded() {
        productCards.shouldBe(
            CollectionCondition.sizeGreaterThan(0),
            Duration.ofSeconds(15)
        );
        log.info("Каталог загружен, товаров: {}", productCards.size());
        return this;
    }

    /** Возвращает количество видимых chip-кнопок */
    @Step("Получить количество chip-кнопок")
    public int getChipCount() {
        return (int) chipButtons.stream().filter(SelenideElement::isDisplayed).count();
    }

    /** Возвращает количество товаров на странице */
    @Step("Количество товаров на странице")
    public int getProductCount() {
        return productCards.size();
    }
}
