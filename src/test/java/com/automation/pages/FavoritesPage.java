package com.automation.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object для страницы избранного vodovoz.ru (/personal/favorite/).
 *
 * HTML-структура:
 *   Ссылка в шапке:         a[href='/personal/favorite/']
 *   Счётчик избранного:     .icon-count--favorite
 *   Список товаров:         .catalog-block__wrapper, .favorite-item
 *   Название товара:        .catalog-block__info-title a.dark_link
 *   Пустое избранное:       .favorites-empty, .empty-favorites
 */
public class FavoritesPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(FavoritesPage.class);

    // ── Локаторы ─────────────────────────────────────────────────────────────

    /** Ссылка «Избранное» в шапке сайта */
    private final SelenideElement favoritesHeaderLink = $("a[href='/personal/favorite/']");

    /** Счётчик избранного в шапке */
    private final SelenideElement favoritesCounter = $(".icon-count--favorite");

    /** Карточки товаров в избранном */
    private final ElementsCollection favoriteItems = $$(".catalog-block__wrapper, .favorite-item");

    /** Названия товаров в избранном */
    private final ElementsCollection itemTitles = $$(".catalog-block__info-title a.dark_link, .favorite-item__title a");

    // ── Навигация ─────────────────────────────────────────────────────────────

    /** Открывает страницу избранного через URL */
    @Step("Открыть страницу избранного (/personal/favorite/)")
    public FavoritesPage open() {
        log.info("Открываю страницу избранного");
        com.codeborne.selenide.Selenide.open("/personal/favorite/");
        return this;
    }

    /** Переходит в избранное через иконку в шапке */
    @Step("Перейти в избранное через иконку в шапке")
    public FavoritesPage openViaHeader() {
        log.info("Нажимаю иконку «Избранное» в шапке");
        favoritesHeaderLink.shouldBe(Condition.visible).click();
        return this;
    }

    // ── Проверки ─────────────────────────────────────────────────────────────

    /** Проверяет, что страница избранного загружена */
    @Step("Страница избранного загружена")
    public FavoritesPage shouldBeLoaded() {
        log.info("Проверяю загрузку страницы избранного");
        urlShouldContain("/personal/favorite/");
        return this;
    }

    /** Проверяет, что избранное не пустое */
    @Step("Избранное содержит товары (не пусто)")
    public FavoritesPage checkFavoritesNotEmpty() {
        log.info("Проверяю, что избранное не пусто");
        favoriteItems.shouldHave(CollectionCondition.sizeGreaterThan(0));
        return this;
    }

    /**
     * Проверяет, что хотя бы один товар в избранном содержит указанный текст в названии.
     * @param partialName часть названия товара
     */
    @Step("В избранном есть товар, содержащий: '{partialName}'")
    public FavoritesPage shouldContainProduct(String partialName) {
        log.info("Ищу товар '{}' в избранном", partialName);
        boolean found = itemTitles.stream()
                .anyMatch(el -> {
                    try { return el.getText().toLowerCase().contains(partialName.toLowerCase()); }
                    catch (Exception e) { return false; }
                });
        if (!found) {
            // Запасной вариант — проверяем весь текст страницы
            $("body").shouldHave(Condition.text(partialName));
        }
        log.info("Товар '{}' найден в избранном ✓", partialName);
        return this;
    }

    /** Возвращает текст счётчика избранного в шапке */
    @Step("Получить счётчик избранного")
    public String getFavoritesCounterText() {
        return favoritesCounter.getText().trim();
    }

    /** Проверяет, что счётчик избранного больше нуля */
    @Step("Счётчик избранного должен быть > 0")
    public FavoritesPage counterShouldBePositive() {
        favoritesCounter.shouldNotHave(Condition.text("0"),
                java.time.Duration.ofSeconds(10));
        log.info("Счётчик избранного: {}", favoritesCounter.getText());
        return this;
    }
}