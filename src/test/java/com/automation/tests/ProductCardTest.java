package com.automation.tests;

import com.automation.pages.ProductPage;
import com.automation.pages.SearchPage;
import com.automation.pages.SearchResultsPage;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.automation.base.BaseTest;


import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Интернет-магазин Vodovoz.ru")
@Feature("Карточка товара (PDP)")
public class ProductCardTest extends BaseTest {

    @Test
    @Story("Просмотр информации о товаре")
    @DisplayName("Проверка ключевых атрибутов карточки: название, цена, характеристики")
    @Description("Поиск товара -> переход в карточку по ссылке -> сверка H1, цены и блока деталей")
    void testProductCardDisplaysCoreInfo() {
        open("/");

        SearchPage searchPage = new SearchPage();
        SearchResultsPage resultsPage = searchPage.search("кулер");

        // Запоминаем название из выдачи
        String expectedTitle = resultsPage.getFirstProductTitle();

        // Открываем карточку
        ProductPage productPage = resultsPage.openFirstProduct();

        // Проверяем карточку
        productPage.shouldBeLoaded()
                .shouldHavePrice()
                .shouldHaveDetails();

        String actualTitle = productPage.getProductTitle();

        // Сверяем первые 10 символов названия, чтобы исключить расхождения в переносах строк
        String shortExpected = expectedTitle.substring(0, Math.min(expectedTitle.length(), 10)).toLowerCase();
        assertTrue(actualTitle.toLowerCase().contains(shortExpected),
                "Заголовок в карточке должен соответствовать выбранному товару. Ожидали часть: " + shortExpected + ", получили: " + actualTitle);
    }

    @Test
    @Story("Добавление в корзину со страницы товара")
    @DisplayName("Добавление товара в корзину из карточки с проверкой динамического счетчика")
    @Description("Открытие карточки кулера -> клик 'В корзину' -> проверка появления счетчика количества")
    void testAddToCartFromProductCard() {
        open("/");

        SearchPage searchPage = new SearchPage();
        SearchResultsPage resultsPage = searchPage.search("кулер");

        ProductPage productPage = resultsPage.openFirstProduct();
        productPage.shouldBeLoaded()
                .addToCart()
                .quantityCounterShouldBeVisible();
    }

}
