package com.automation.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Page Object for the Home / Dashboard page.
 */
public class HomePage extends BasePage {

    private final SelenideElement pageHeader   = $("h1.dashboard-title");
    private final SelenideElement userGreeting = $(".user-greeting");
    private final SelenideElement logoutButton = $("[data-testid='logout']");
    private final SelenideElement navMenu      = $("nav.main-menu");

    @Step("Open home page")
    public HomePage openPage() {
        open("/");
        return this;
    }

    @Step("Click 'Logout'")
    public void logout() {
        logoutButton.shouldBe(visible).click();
    }

    @Step("Home page should be loaded")
    public HomePage shouldBeLoaded() {
        pageHeader.shouldBe(visible);
        navMenu.shouldBe(visible);
        return this;
    }

    @Step("Greeting should contain: {username}")
    public HomePage greetingShouldContain(String username) {
        userGreeting.shouldHave(text(username));
        return this;
    }

    @Step("Page header should contain: {expectedText}")
    public HomePage headerShouldContain(String expectedText) {
        pageHeader.shouldHave(text(expectedText));
        return this;
    }
}