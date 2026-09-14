package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.CatalogFiltersPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;

/**
 * Тесты фильтрации в каталоге vodovoz.ru.
 *
 * <h3>Механизм фильтров (выявлен через DOM-инспектор):</h3>
 * <p>Фильтрация реализована через <b>chip-навигацию</b> — горизонтальные кнопки-ссылки
 * под заголовком раздела (class: {@code a.chip.chip--toggle}).
 * Каждая chip-кнопка ведёт на вложенный подраздел категории, например:
 * <ul>
 *   <li>«Хит» → /catalog/pitevaya_voda_19_litrov/hit/ (14 товаров)</li>
 *   <li>«В многоразовой таре» → /catalog/pitevaya_voda_19_litrov/v_mnogorazovoy_tare/</li>
 *   <li>«В одноразовой таре» → /catalog/pitevaya_voda_19_litrov/v_odnorazovoy_tare/</li>
 * </ul>
 * После перехода — URL меняется и chip-кнопки остаются на новой странице (навигация видима).
 * Assertion строится по URL (содержит slug подраздела) и наличию товаров.
 */
@Epic("vodovoz.ru")
@Feature("Каталог — Фильтры")
@DisplayName("Фильтрация товаров в каталоге")
@Tag("regression")
@Tag("catalog")
class CatalogFiltersTest extends BaseTest {

    private static final String CATALOG_19L = "/catalog/pitevaya_voda_19_litrov/";

    private final CatalogFiltersPage filtersPage = new CatalogFiltersPage();

    // ── Тест 1: Фильтр «В одноразовой таре» ────────────────────────────────────

    @Test
    @Story("Chip-навигация по типу тары")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Chip-фильтр 'В одноразовой таре': URL обновился, товары видны")
    @Description("""
            Шаги:
            1. Открыть раздел «Питьевая вода 19 литров»
            2. Перейти по URL подраздела «В одноразовой таре»
            3. Проверить что URL содержит слаг подраздела
            4. Проверить что chip-кнопки видимы на новой странице
            5. Проверить что список товаров не пуст
            """)
    void testSingleUseTareFilter() {
        final String filterSlug = "v_odnorazovoy_tare";
        final String filterUrl  = CATALOG_19L + filterSlug + "/";

        filtersPage.openCategory(CATALOG_19L);
        sleep(1000);

        filtersPage.navigateToFilterUrl(filterUrl);

        filtersPage.shouldBeFilterApplied(filterSlug);
        filtersPage.productsShouldBeVisible();
        log.info("Тест 'В одноразовой таре' прошёл ✓");
    }

    // ── Тест 2: Фильтр «В многоразовой таре» ───────────────────────────────────

    @Test
    @Story("Chip-навигация по типу тары")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Chip-фильтр 'В многоразовой таре': URL обновился, товары видны")
    @Description("""
            Шаги:
            1. Открыть раздел «Питьевая вода 19 литров»
            2. Перейти по URL подраздела «В многоразовой таре»
            3. Проверить что URL содержит слаг подраздела
            4. Проверить что chip-кнопки видимы (навигация присутствует)
            5. Проверить что список товаров не пуст
            """)
    void testReusableTareFilter() {
        final String filterSlug = "v_mnogorazovoy_tare";
        final String filterUrl  = CATALOG_19L + filterSlug + "/";

        filtersPage.openCategory(CATALOG_19L);
        sleep(1000);

        filtersPage.navigateToFilterUrl(filterUrl);

        filtersPage.shouldBeFilterApplied(filterSlug);
        filtersPage.productsShouldBeVisible();
        log.info("Тест 'В многоразовой таре' прошёл ✓");
    }
}
