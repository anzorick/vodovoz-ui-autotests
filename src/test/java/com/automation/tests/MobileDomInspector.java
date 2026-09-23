package com.automation.tests;
import com.automation.base.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.ElementsCollection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import java.time.Duration;
@Tag("diagnostic")
class MobileDomInspector extends BaseTest {
    @Test void inspect() {
        Configuration.browserSize = "375x667";
        open("/");
        $("body").shouldBe(visible, Duration.ofSeconds(10));
        Selenide.sleep(2000);
        // Dump burger/menu selectors
        String[] queries = {
            // Burger button candidates
            "document.querySelectorAll('.mobilemenu__toggle,.burger,.hamburger,[class*=burger],[class*=mobile-menu],[class*=mmenu],[data-name=mobile],[class*=menu-toggle]').length",
            "Array.from(document.querySelectorAll('.mobilemenu__toggle,.burger,.hamburger,[class*=burger-btn],[class*=menu-btn]')).filter(e=>e.offsetWidth>0).map(e=>e.className+'|'+e.tagName+'|'+e.outerHTML.substring(0,150)).join('\\n')",
            // Mobile menu overlay/drawer
            "Array.from(document.querySelectorAll('[class*=mobilemenu],[class*=mobile-nav],[class*=mmenu],[class*=drawer],[class*=offcanvas]')).filter(e=>e.offsetWidth>0).map(e=>e.className.substring(0,80)).join('\\n')",
            // Search on mobile
            "Array.from(document.querySelectorAll('[class*=search],[class*=Search]')).filter(e=>e.offsetWidth>0).slice(0,8).map(e=>e.className.substring(0,80)+'|'+e.tagName).join('\\n')",
            // Cart icon
            "Array.from(document.querySelectorAll('[class*=cart],[class*=basket],[class*=Cart]')).filter(e=>e.offsetWidth>0).slice(0,5).map(e=>e.className.substring(0,80)+'|'+e.tagName).join('\\n')",
            // Product cards
            "Array.from(document.querySelectorAll('.catalog-block__wrapper,.catalog-item,article.product')).slice(0,2).map(e=>e.className.substring(0,80)).join('\\n')",
            // UserAgent
            "navigator.userAgent"
        };
        for(String q:queries){
            try{
                Object r=executeJavaScript("return "+q+";");
                log.info("Q[{}...]: {}", q.substring(0,Math.min(q.length(),50)), r!=null?r.toString().substring(0,Math.min(r.toString().length(),500)):"null");
                log.info("---");
            }catch(Exception e){log.warn("ERR: {}",e.getMessage());}
        }
    }
}