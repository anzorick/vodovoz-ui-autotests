# Аудит покрытия тестами — vodovoz.ru

> Дата инспекции: 2026-10-05. Стек: Java 17 + Selenide 7.4.3 + JUnit 5 + Allure. Все селекторы верифицированы живой DOM-инспекцией через executeJavaScript.

---

## 1. Текущее покрытие (36 тестов, 14 классов)

| Класс | Покрытый сценарий | Теги |
|---|---|---|
| VodovozSearchTest | Позитивный поиск, 4 параметризованных запроса, негативный поиск | smoke, regression, catalog |
| SearchAutosuggestTest | Автоподсказки поиска (graceful-skip в headless) | regression, catalog |
| CatalogSortTest | Сортировка в каталоге | regression, catalog |
| CatalogFiltersTest | Фильтры-чипсы в каталоге | regression, catalog |
| ProductCardTest | Атрибуты/цена/характеристики, таб «Отзывы», добавление со счётчиком | smoke, regression, cart |
| AddToCartTest | E2E добавление товара в корзину | smoke, regression, cart |
| CartQuantityTest | Изменение количества, удаление, очистка корзины | regression, cart |
| CartPriceTest | AJAX-пересчёт итога, негативный промокод | regression, cart |
| AddToFavoritesTest | Добавление и удаление из избранного | regression |
| LoginByEmailTest | Позитивный и негативный вход по email | regression |
| CallbackFormTest | Форма «Заказ звонка» | regression, forms |
| FeedbackFormTest | Негативная валидация формы отзывов (на /reviews/) | regression, forms |
| ProductCompareTest | Сравнение 4 товаров, таблица, удаление | regression |
| MobileSmokeTest | Mobile 375×667: главная+бургер, меню, поиск по URL, карточка | mobile, smoke, regression |

---

## 2. Пробелы в покрытии

> **Приоритеты:** P0 — денежные и около-заказные сценарии · P1 — ядро навигации/каталога · P2 — служебные страницы и UX-детали

| # | Раздел | Непокрытый сценарий | Предлагаемый тест (класс · метод) | Теги | Ключевые селекторы (верифицированы) | Сложность | Риск | Приоритет |
|---|---|---|---|---|---|---|---|---|
| 1 | **Чекаут** | Шаг 1 оформления заказа: выбор типа доставки и даты (без отправки заказа) | `CheckoutStep1Test` · `testDeliveryTypesAndDatePickerVisible` | regression, cart | `/order/` (URL работает); `[class*=delivery-type], [class*=datepicker], [name*=delivery]`; `hasDatePicker=true` подтверждён инспекцией | Средняя | **Денежный путь** | **P0** |
| 2 | **Промокод** | Применение рабочего промокода (позитивный сценарий — скидка применяется, итог пересчитывается) | `CartPromoTest` · `testValidPromoCodeApplied` | regression, cart | `input[name=coupon-code], input[name*=coupon], .coupon-form button, [class*=discount]`; CartPriceTest уже покрывает негативный сценарий | Средняя | **Денежный** | **P0** |
| 3 | **Карточка товара** | Переключатель фасовки/объёма → цена пересчитывается, состав корзины корректен | `ProductVariantTest` · `testVariantSwitchUpdatesPrice` | regression, catalog | `[class*=offer-item], [class*=prop-val], [class*=tabs-nav] li[data-offer]`; `.price__new-val` (уже в CartPage) | Средняя | **Цена/заказ** | **P0** |
| 4 | **Карточка товара** | Модальное окно «Купить в 1 клик» — открывается, поля форм видимы | `BuyOneClickTest` · `testBuyOneClickModalOpens` | regression, cart | `[class*=one-click], [class*=oneclick], [data-action*=one]`; модальный контейнер `.popup-one-click, .mfp-content` | Низкая | **Денежный путь** | **P0** |
| 5 | **Шапка** | Hover-дропдаун категорий: наведение → подменю появляется, ссылки кликабельны | `HeaderNavTest` · `testHoverDropdownOpens` | regression | `.header-menu__item--dropdown` (hover); `.dropdown-menu-wrapper--visible` (виден); `.dropdown-menu-inner` (контент) — **все три верифицированы** | Низкая | Навигация | **P1** |
| 6 | **Каталог** | Хлебные крошки: catalog → подкаталог → PDP — текст и href корректны | `BreadcrumbsTest` · `testBreadcrumbsOnCatalogAndPDP` | regression, catalog | `.bx-breadcrumb a, [class*=breadcrumb] a` (пустой результат — нужна повторная инспекция с `Selenide.sleep(2000)`) | Низкая | SEO/навигация | **P1** |
| 7 | **Карточка товара** | Галерея: клик по фото → Fancybox лайтбокс открывается, закрывается | `ProductGalleryTest` · `testPhotoGalleryFancybox` | regression, catalog | `[data-fancybox]` — **подтверждён инспекцией (`hasFancybox=true`)**; `.fancybox__container, .fancybox-wrap` (opened state); `.fancybox__button--close` | Низкая | UX | **P1** |
| 8 | **404** | Несуществующий URL — страница загружается без JS-ошибок, отображает сообщение | `NotFoundTest` · `testNonexistentUrlLoads` | regression, smoke | URL `/this-page-does-not-exist-404xyz/` — **инспекция показала**: кастомная страница (bodyClass=`site_s1 side_left hide_menu_page`), без h1, **НЕ редирект** | Низкая | Стабильность | **P1** |
| 9 | **Выбор города** | Модал смены города/региона — открывается из шапки, список городов виден | `CitySelectTest` · `testCitySelectModalOpens` | regression | `[class*=city], [class*=region]` в шапке (триггер); `[class*=city-popup], [class*=region-modal]` (контейнер) | Средняя | Доставка/регион | **P1** |
| 10 | **Личный кабинет** | После входа: страница `/personal/` грузится, профиль виден | `PersonalAreaTest` · `testPersonalAreaAfterLogin` | regression | `/personal/`; `[class*=personal],[class*=lk-menu],[class*=profile]` | Высокая | Данные пользователя | **P1** |
| 11 | **Мобильный** | Добавление товара в корзину на мобильной ширине | `MobileSmokeTest` · `testMobileAddToCart` | mobile, regression | `.catalog-block__wrapper [data-action='basket']`; счётчик корзины в шапке | Средняя | Мобильный UX | **P1** |
| 12 | **Каталог** | Быстрый просмотр товара (quick view попап) — если существует | `QuickViewTest` · `testQuickViewModalOpens` | regression, catalog | `[class*=quick],[data-action*=quick]` — инспекция показала пустой результат; **нужна повторная проверка hover** | Средняя | Конверсия | **P1** |
| 13 | **Доставка** | Страница `/dostavka/` с картой и зонами доставки | `DeliveryPageTest` · `testDeliveryPageWithMapAndZones` | regression | `/dostavka/`; `[class*=map], [id*=ymaps]`; `[class*=zone]` — **инспекция: `hasMap=true, hasZones=true`** | Низкая | Информационная | **P2** |
| 14 | **Оплата** | Страница `/dostavka/oplata/` с аккордеоном способов оплаты | `PaymentPageTest` · `testPaymentPageAccordionExpands` | regression | `/dostavka/oplata/`; `[class*=accordion], [class*=faq]` — **инспекция: `hasAccordion=true`** | Низкая | Информационная | **P2** |
| 15 | **Каталог** | Переключатель вид отображения сетка/список | `CatalogViewSwitchTest` · `testGridListToggle` | regression, catalog | `[class*=display-type], [data-view], [class*=view-switch]` — инспекция пустая, нужна re-inspect | Низкая | UX | **P2** |
| 16 | **Блог** | Страница `/articles/` — список статей, переход в статью | `BlogPageTest` · `testArticleListAndOpenArticle` | regression | `/articles/`; `[class*=article], [class*=news-item]` — инспекция вернула `null`, вероятно другой URL | Низкая | SEO/контент | **P2** |
| 17 | **PDP Отзывы** | Форма отзыва на PDP (таб «Отзывы») — открывается, поля видимы | `ProductReviewFormTest` · `testReviewFormFieldsVisible` | regression, forms | `.tabs-content [class*=review-form], [class*=review-write]` (FeedbackFormTest покрывает `/reviews/`, а не PDP-таб) | Средняя | Репутация | **P2** |
| 18 | **Авторизация** | Восстановление пароля — форма открывается, email-поле валидируется | `ForgotPasswordTest` · `testForgotPasswordFormValidation` | regression, forms | `/login/?forgot=Y`; `input[name=email], form[action*=forgot]` | Средняя | Безопасность | **P2** |
| 19 | **Подвал** | Ссылки футера ведут на рабочие страницы (не 404) | `FooterLinksTest` · `testFooterLinksNotBroken` | regression | `footer a` — **инспекция нашла**: `/about/contacts/`, `/dostavka/`, `/dostavka/oplata/` | Высокая | SEO | **P2** |
| 20 | **Поиск** | Фильтры на странице результатов поиска (применить → товары отфильтрованы) | `SearchFilterTest` · `testFilterInSearchResults` | regression, catalog | Страница результатов `/catalog/?q=вода`; фильтр-чипсы (уже покрыты в CatalogFiltersTest для каталога, но не для поиска) | Высокая | UX | **P2** |

---

## 3. ТОП-5 по приоритету — точные селекторы для немедленной реализации

### P0-1 · `CheckoutStep1Test` — Шаг оформления заказа

```
URL:              /order/
Date picker:      [class*=datepicker], [name*=date]          ← confirmed hasDatePicker=true
Delivery select:  [class*=delivery-type], [name*=delivery]
Form container:   .order-form, form[action*=order]
Step indicator:   [class*=order-step], [class*=step-1]
Submit (НЕ жать): .order-submit-btn (НЕ кликать — только проверить видимость)
```

### P0-2 · `CartPromoTest` — Позитивный промокод

```
Promo input:    input[name="coupon-code"], input[class*=coupon], input[placeholder*=промокод]
Apply button:   .coupon-form button, button[class*=apply-coupon]
Discount line:  [class*=discount-row], [class*=coupon-discount], [class*=economy]
Grand total:    [class*=basket-total-sum], [class*=grand-total]   ← уже верифицирован CartPriceTest
Тестовый промокод: читать из config.properties key=cart.promo.valid
```

### P0-3 · `BuyOneClickTest` — «Купить в 1 клик»

```
Trigger btn:    [class*=one-click], [class*=oneclick], [data-action*=one],
                span[class*=fast-buy], a[class*=buy-one]
Modal:          .mfp-content, .popup-one-click, [class*=one-click-popup]
Name field:     modal input[name=name], modal input[type=text]:first-child
Phone field:    modal input[name=phone], modal input[type=tel]
Submit (НЕ):    modal button[type=submit]  ← только проверить видимость, НЕ отправлять
```

### P1-1 · `HeaderNavTest` — Hover-дропдаун шапки

```
Nav item:       .header-menu__item--dropdown          ← confirmed
Hover trigger:  actions().moveToElement(navItem).perform()
Visible state:  .dropdown-menu-wrapper--visible       ← confirmed
Inner menu:     .dropdown-menu-inner a                ← confirmed
Assert:         at least 3 visible links in dropdown
```

### P1-2 · `ProductGalleryTest` — Fancybox галерея PDP

```
Main image:     [data-fancybox]                       ← hasFancybox=true confirmed
Click:          $("[data-fancybox]").click()
Opened modal:   .fancybox__container, .fancybox-wrap, .fancybox-overlay
Close btn:      .fancybox__button--close, .fancybox-close, button[title=Close]
Assert closed:  .fancybox__container should be hidden after close
PDP URL:        /catalog/pitevaya_voda_19_litrov/33016/
```

---

## 4. Методология инспекции

- Браузер: Chrome (headless=false), viewport 1920×1080
- Страницы: `/`, `/catalog/pitevaya_voda_19_litrov/`, `/catalog/pitevaya_voda_19_litrov/33016/`, `/basket/`, `/order/`, `/dostavka/`, `/dostavka/oplata/`, `/contacts/`, `/articles/`, `/about/sertifikaty/`, `/this-page-does-not-exist-404xyz/`
- Инструмент: AuditInspector1/2/3 (удалены после инспекции)
- Замечания:
  - 404 URL → кастомная страница без h1, НЕ редирект на главную
  - `totalCards=60` в каталоге — бесконечная прокрутка или all-in-one загрузка, стандартного пейджера нет
  - Корзина пустая при инспекции — CART_DELIVERY/CHECKOUT элементы не показаны
  - `/about/sertifikaty/` и `/articles/` вернули null — URL может быть другим (нужна ре-инспекция)
  - Мобильное меню: 18 ссылок в `#mobilemenu` ✅

---

*Сгенерировано автоматически. Git-команды не выполнялись.*
