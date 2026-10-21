package com.automation.tests;

import com.automation.base.BaseTest;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeaderNavTest extends BaseTest {

    @Test
    @DisplayName("Hover-дропдаун категорий в шапке открывается и содержит ссылки")
    @Description("Наведение на пункт меню с подменю -> выпадает панель -> внутри не менее 3 видимых ссылок")
    void testHoverDropdownOpens() {
        open("/");

        // Предпосылка (graceful skip): в шапке вообще есть пункты с дропдауном
        Assumptions.assumeTrue($$(".header-menu__item--dropdown").size() > 0,
                "В шапке нет пунктов с подменю — предпосылка не выполнена");

        // Результат 1: наведение реально раскрывает панель (падает красным, если не раскрылась)
        $$(".header-menu__item--dropdown").first()
                .shouldBe(visible, Duration.ofSeconds(10))
                .hover();
        $(".dropdown-menu-wrapper--visible")
                .shouldBe(visible, Duration.ofSeconds(10));

        // Результат 2: внутри панели есть содержимое (падает красным, если пусто)
        long visibleLinks = $$(".dropdown-menu-inner a").filter(visible).size();
        assertTrue(visibleLinks >= 3,
                "В дропдауне меньше 3 видимых ссылок: " + visibleLinks);
    }
}