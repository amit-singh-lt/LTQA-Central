package helper;

import org.apache.logging.log4j.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utility.Utilities;

import java.io.File;
import java.time.Duration;

import static utility.WaitConstant.*;

/**
 * The WebDriverHelper class provides utility methods for interacting with web elements using Selenium WebDriver.
 * It includes methods for locating elements, waiting for elements, navigating the browser, and executing JavaScript.
 */
public class WebDriverHelper extends Utilities {
  private final Logger ltLogger = LogManager.getLogger(WebDriverHelper.class);
  private final WebDriver driver;

  // Constants for locator strategies
  private static final String CLASS = "class";
  private static final String ID = "id";
  private static final String CSS = "css";
  private static final String XPATH = "xpath";
  private static final String NAME = "name";
  private static final String TAG_NAME = "tagname";

  /**
   * Constructor that initializes the WebDriver with a specific RemoteWebDriver instance.
   *
   * @param testDriver The RemoteWebDriver instance to use.
   */
  public WebDriverHelper(RemoteWebDriver testDriver) {
    this.driver = testDriver;
  }

  /**
   * Finds a WebElement locator using a specified strategy and value.
   *
   * @param locator An array where the first element is the locator strategy and the second is the locator value.
   * @return The By object representing the locator.
   */
  public By findElementBy(String[] locator) {
    ltLogger.info("Finding element using ['{}','{}']", locator[0], locator[1]);
    String using = locator[0].toLowerCase();
    String locatorValue = locator[1];
    return switch (using) {
      case ID -> By.id(locatorValue);
      case CLASS -> By.className(locatorValue);
      case NAME -> By.name(locatorValue);
      case XPATH -> By.xpath(locatorValue);
      case CSS -> By.cssSelector(locatorValue);
      case TAG_NAME -> By.tagName(locatorValue);
      default -> throw new IllegalArgumentException("Unsupported locator strategy: " + using);
    };
  }

  /**
   * Waits for an element to be present on the page and returns it.
   *
   * @param locator An array containing the locator strategy and value.
   * @param timeout The maximum time to wait for the element, in seconds.
   * @return The WebElement found by the locator.
   */
  public WebElement waitForElement(String[] locator, int timeout) {
    try {
      ltLogger.info("Waiting for element using ['{}','{}']", locator[0], locator[1]);
      this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
      return wait.until(ExpectedConditions.presenceOfElementLocated(findElementBy(locator)));
    } finally {
      this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(SHORT_WAIT_TIME));
    }
  }

  /**
   * Opens a specified URL in the browser.
   *
   * @param url The URL to open.
   */
  public void getURL(String url) {
    ltLogger.info("Opening URL: '{}'", url);
    this.driver.get(url);
  }

  /**
   * Gets the title of the current page.
   *
   * @return The title of the page.
   */
  public String getTitle() {
    return this.driver.getTitle();
  }

  /**
   * Gets the current URL of the browser.
   *
   * @return The current URL.
   */
  public String getCurrentURL() {
    return this.driver.getCurrentUrl();
  }

  /**
   * Refreshes the current page.
   */
  public void pageRefresh() {
    this.driver.navigate().refresh();
  }

  /**
   * Navigates forward in the browser's history.
   */
  public void pageForward() {
    this.driver.navigate().forward();
  }

  /**
   * Navigates backward in the browser's history.
   */
  public void pageBack() {
    this.driver.navigate().back();
  }

  /**
   * Finds and returns a WebElement using the specified locator and wait time.
   *
   * @param locator  An array containing the locator strategy and value.
   * @param waitTime The maximum time to wait for the element, in seconds.
   * @return The WebElement found by the locator.
   */
  public WebElement getElement(String[] locator, int waitTime) {
    ltLogger.info("Finding element using ['{}','{}']", locator[0], locator[1]);
    return waitForElement(locator, waitTime);
  }

  /**
   * Finds and returns a WebElement using the specified locator and a default wait time of 30 seconds.
   *
   * @param locator An array containing the locator strategy and value.
   * @return The WebElement found by the locator.
   */
  public WebElement getElement(String[] locator) {
    return getElement(locator, 30);
  }

  /**
   * Gets the text content of a WebElement.
   *
   * @param ele The WebElement from which to retrieve the text.
   * @return The text content of the WebElement.
   */
  public String getText(WebElement ele) {
    return ele.getText().trim();
  }

  /**
   * Finds a WebElement using the specified locator and returns its text content.
   *
   * @param locator An array containing the locator strategy and value.
   * @return The text content of the WebElement.
   */
  public String getText(String[] locator) {
    WebElement ele = getElement(locator);
    return getText(ele);
  }


  // JAVASCRIPT EXECUTION


  /**
   * Executes the given JavaScript script in the context of the currently selected frame or window.
   *
   * @param script The JavaScript script to execute.
   */
  public void javascriptExecution(String script) {
    ltLogger.info("Executing script: {}", script);
    try {
      ((JavascriptExecutor) this.driver).executeScript(script);
      ltLogger.info("Script executed successfully: {}", script);
    } catch (Exception e) {
      if (script.contains("console.error")) return;
      ltLogger.error("Script execution failed: {}", script);
      ltLogger.error("Exception: {}", e.toString());
      throw e;
    }
  }

  /**
   * Performs a hard refresh of the current page using JavaScript.
   */
  public void hardRefresh() {
    ((RemoteWebDriver) this.driver).executeScript("location.reload(true);");
  }


  // SCREEN SHOTS


  /**
   * Takes a screenshot of the entire page and returns it as a File.
   *
   * @return A File object representing the screenshot of the entire page.
   */
  public File getPageScreenShotAsFile() {
    return ((RemoteWebDriver) driver).getScreenshotAs(OutputType.FILE);
  }

  /**
   * Takes a screenshot of a specific element located by the provided locator and returns it as a File.
   * Logs the locator details before taking the screenshot.
   *
   * @param locator An array containing the locator type (e.g., "id", "xpath") and the locator value.
   * @return A File object representing the screenshot of the specified element.
   */
  public File getElementScreenshotAsFile(String[] locator) {
    ltLogger.info("get screenshot with locator using ['{}','{}']", locator[0], locator[1]);
    return getElement(locator).getScreenshotAs(OutputType.FILE);
  }

}
