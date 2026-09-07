# Web Automation Project

Test automation framework for web applications built with Java 17, Selenide, JUnit 5, and Allure.

## Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Maven | latest | Build tool |
| JUnit 5 (Jupiter) | 5.11.3 | Test framework |
| Selenide | 7.4.3 | UI automation |
| WebDriverManager | 5.9.2 | Driver management |
| Allure Framework | 2.29.0 | Test reports |

## Project Structure

```
d:\java auto\
├── pom.xml
├── .gitignore
├── README.md
└── src/
    ├── main/java/com/automation/
    │   ├── config/
    │   │   └── Configuration.java      # Reads config.properties + system props
    │   └── utils/
    │       └── ScreenshotUtils.java    # Screenshot helper
    └── test/
        ├── java/com/automation/
        │   ├── base/
        │   │   └── BaseTest.java       # Browser setup / teardown + Allure listener
        │   ├── pages/
        │   │   ├── BasePage.java       # Common page methods (@Step)
        │   │   ├── LoginPage.java      # Login page object
        │   │   └── HomePage.java       # Home/Dashboard page object
        │   └── tests/
        │       └── LoginTest.java      # Demo tests
        └── resources/
            ├── config.properties       # Browser & URL settings
            ├── allure.properties       # Allure results directory
            └── logback-test.xml        # Logging config
```

## Run Tests

### Run all tests
```bash
mvn test
```

### Run a specific test class
```bash
mvn test -Dtest=LoginTest
```

### Run a specific test method
```bash
mvn test -Dtest=LoginTest#testLoginWithInvalidCredentials
```

### Run with a specific browser
```bash
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
```

### Run in headless mode (CI-friendly)
```bash
mvn test -Dheadless=true
```

### Run against a different URL
```bash
mvn test -Dbase.url=https://staging.example.com
```

### Combine options
```bash
mvn test -Dbrowser=chrome -Dheadless=true -Dbase.url=https://staging.example.com
```

## Allure Reports

### Generate report after test run
```bash
mvn allure:report
# Report: target/site/allure-maven-plugin/index.html
```

### Run tests + open report in browser immediately
```bash
mvn test allure:serve
```

### Open previously generated report
```bash
mvn allure:serve
```

## Configuration

Edit `src/test/resources/config.properties` to change defaults.  
Any property can also be overridden at runtime with `-D<property>=<value>`.

| Property          | Default                | Description                  |
|-------------------|------------------------|------------------------------|
| `browser`         | `chrome`               | Browser: chrome / firefox / edge |
| `headless`        | `false`                | Run without UI (true/false)  |
| `browser.timeout` | `10`                   | Element wait timeout (sec)   |
| `base.url`        | `https://example.com`  | Base URL of the application  |

## Adding a New Page

1. Create `src/test/java/com/automation/pages/MyPage.java` extending `BasePage`
2. Add private locators as `SelenideElement` fields
3. Add `@Step`-annotated action and assertion methods
4. Use the page in a test class extending `BaseTest`

```java
public class MyPage extends BasePage {
    private final SelenideElement header = $("h1");

    @Step("Open my page")
    public MyPage openPage() {
        open("/my-page");
        return this;
    }

    @Step("Header should contain: {text}")
    public MyPage headerShouldContain(String text) {
        header.shouldHave(text(text));
        return this;
    }
}
```
