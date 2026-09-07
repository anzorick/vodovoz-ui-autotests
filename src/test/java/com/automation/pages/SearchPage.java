package com.automation.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Page Object для поисковой формы на главной странице vodovoz.ru.
 *
 * Реальные CSS-селекторы получены из HTML сайта:
 *   - Поле ввода:  #title-search-input
 *   - Кнопка поиска: button.btn-search
 *   - Кнопка сброса: button.btn-clear-search
 *   - Форма поиска: form.search (action="/catalog/")
 */
public class SearchPage extends BasePage {

    // Локаторы (приватные - тест не знает про CSS-классы)
    private final SelenideElement searchInput  = $("#title-search-input");
    private final SelenideElement searchButton = $("button.btn-search");
    private final SelenideElement clearButton  = $("button.btn-clear-search");

    // Навигация

    @Step("Открыть главную страницу vodovoz.ru")
    public SearchPage openPage() {
        open("/");
        return this;
    }

    // Действия

    @Step("Ввести поисковой запрос: {query}")
    public SearchPage enterQuery(String query) {
        searchInput.shouldBe(visible, enabled).setValue(query);
        return this;
    }

    @Step("Нажать кнопку 'Найти'")
    public SearchResultsPage clickSearch() {
        searchButton.shouldBe(enabled).click();
        return new SearchResultsPage();
    }

    @Step("Выполнить поиск: {query}")
    public SearchResultsPage search(String query) {
        return enterQuery(query).clickSearch();
    }

    @Step("Нажать кнопку сброса поиска")
    public SearchPage clickClear() {
        clearButton.shouldBe(visible).click();
        return this;
    }

    // Проверки

    @Step("Поисковая строка должна быть видна и активна")
    public SearchPage searchInputShouldBeReady() {
        searchInput.shouldBe(visible, enabled);
        return this;
    }

    @Step("Поле поиска должно быть пустым")
    public SearchPage searchInputShouldBeEmpty() {
        searchInput.shouldBe(visible);
        if (!searchInput.getValue().isEmpty()) {
            throw new AssertionError("Поле поиска должно быть пустым, но содержит: " + searchInput.getValue());
        }
        return this;
    }

    public String getSearchInputValue() {
        return searchInput.getValue();
    }
}