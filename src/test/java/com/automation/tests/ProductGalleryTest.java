package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.ProductPage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;

public class ProductGalleryTest extends BaseTest {

    @Test
    @DisplayName("Галерея товара: открытие и закрытие лайтбокса Fancybox")
    void testPhotoGalleryOpensAndCloses() {
        open("/catalog/pitevaya_voda_19_litrov/33016/");
        ProductPage productPage = new ProductPage();
        productPage.shouldBeLoaded();

        // graceful skip ТОЛЬКО на отсутствие триггера — не на провал открытия/закрытия
        Assumptions.assumeTrue(productPage.hasGalleryTrigger(),
                "На карточке 33016 не найден триггер галереи [data-fancybox] — тест пропущен");

        productPage.openGalleryLightbox()
                .galleryLightboxShouldBeOpen()
                .closeGalleryLightbox()
                .galleryLightboxShouldBeClosed();
    }
}