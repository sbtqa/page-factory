package ru.sbtqa.tag.pagefactory;

import java.util.Optional;
import java.util.Set;

import cucumber.runtime.junit.Assertions;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.exceptions.GetValueException;
import ru.sbtqa.tag.pagefactory.exceptions.WaitException;
import ru.sbtqa.tag.qautils.managers.DateManager;

public class DriverExtensions {

    private static final Logger log = LoggerFactory.getLogger(DriverExtensions.class);

    /**
     * Get outer element text. Used for get text from checkboxes and radio buttons
     *
     * @param webElement TODO
     * @return text of element
     * @throws ru.sbtqa.tag.pagefactory.exceptions.GetValueException TODO
     */
    public static String getElementValue(WebElement webElement) throws GetValueException {
        String elementValue = "Cannot parse element";
        String elementId = webElement.getAttribute("id");

        if (elementId == null) {
            throw new GetValueException("Getting value is not support in element without id");
        }

        WebElement possibleTextMatcher = PageFactory.getWebDriver().findElement(By.xpath("//*[@id='" + elementId + "']/.."));
        if (possibleTextMatcher.getText().isEmpty()) {
            possibleTextMatcher = PageFactory.getWebDriver().findElement(By.xpath("//*[@id='" + elementId + "']/../.."));
            if ("tr".equals(possibleTextMatcher.getTagName())) {
                elementValue = possibleTextMatcher.getText();
            }
        } else {
            elementValue = possibleTextMatcher.getText();
        }
        return elementValue;
    }

    /**
     * Wait until element present
     *
     * @param webElement Desired web element
     */
    public static void waitUntilElementPresent(WebElement webElement) {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds()).
                until(ExpectedConditions.visibilityOf(webElement));
    }

    /**
     * Wait until element present
     *
     * @param webElement Desired web element
     * @param timeout Timeout in seconds
     */
    public static void waitUntilElementPresent(WebElement webElement, int timeout) {
        new WebDriverWait(PageFactory.getWebDriver(), timeout).
                until(ExpectedConditions.visibilityOf(webElement));
    }

    /**
     * Wait until element present
     *
     * @param webElement Desired web element
     */
    public static void waitUntilElementToBeClickable(WebElement webElement) {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds()).
                until(ExpectedConditions.elementToBeClickable(webElement));
    }

    /**
     * Wait until element present
     *
     * @param webElement Desired web element
     * @param timeout Timeout in seconds
     */
    public static void waitUntilElementToBeClickable(WebElement webElement, int timeout) {
        new WebDriverWait(PageFactory.getWebDriver(), timeout).
                until(ExpectedConditions.elementToBeClickable(webElement));
    }

    /**
     * Wait until element is present and enable to check page prepare to work
     *
     * @param webElement Desired web element
     */
    public static void waitUntilPagePrepared(WebElement webElement) {
        try {
            new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds() / 2).
                    until(ExpectedConditions.visibilityOf(webElement));
        } catch (Exception | AssertionError e) {
            log.debug("Element {} does not become visible after timeout", webElement, e);
            PageFactory.getWebDriver().navigate().refresh();
            log.debug("Page refreshed");
            new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds()).
                    until(ExpectedConditions.visibilityOf(webElement));
        }
    }

    /**
     * Wait for page prepared with javascript
     *
     * @param stopRecursion TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException TODO
     */
    public static void waitForPageToLoad(boolean... stopRecursion) throws WaitException {
        long timeoutTime = System.currentTimeMillis() + PageFactory.getTimeOut();
        while (timeoutTime > System.currentTimeMillis()) {
            try {
                if ("complete".equals((String) ((JavascriptExecutor) PageFactory.getWebDriver()).executeScript("return document.readyState"))) {
                    return;
                }
                sleep(1);
            } catch (Exception | AssertionError e) {
                log.debug("Page does not become to ready state", e);
                PageFactory.getWebDriver().navigate().refresh();
                log.debug("Page refreshed");
                if ((stopRecursion.length == 0) || (stopRecursion.length > 0 && !stopRecursion[0])) {
                    waitForPageToLoad(true);
                }
            }
        }

        throw new WaitException("Timed out after " + PageFactory.getTimeOutInSeconds() + " seconds waiting for preparedness of page");
    }

    /**
     * Wait until element present
     *
     * @param by a {@link org.openqa.selenium.By} object.
     * @return return appeared WebElement
     */
    public static WebElement waitUntilElementAppearsInDom(By by) {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds())
                .until(ExpectedConditions.presenceOfElementLocated(by));

        return PageFactory.getWebDriver().findElement(by);
    }

    /**
     * Wait until element present
     *
     * @param by a {@link org.openqa.selenium.By} object.
     * @param timeout timeout in seconds
     * @return return appeared WebElement
     */
    public static WebElement waitUntilElementAppearsInDom(By by, long timeout) {
        new WebDriverWait(PageFactory.getWebDriver(), timeout)
                .until(ExpectedConditions.presenceOfElementLocated(by));

        return PageFactory.getWebDriver().findElement(by);
    }

    /**
     * Wait until element gone from dom
     *
     * @param timeout in milliseconds
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     */
    public static void waitUntilElementGoneFromDom(WebElement webElement, long timeout) {
        Long start = DateManager.getCurrentTimestamp();
        while (DateManager.getCurrentTimestamp() < start + timeout) {
            try {
                if (!webElement.isDisplayed()) {
                    return;
                }
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                log.debug("There is no element {} in dom", webElement, e);
                return;
            }
            sleep(1);
        }
        throw new NoSuchElementException("Timed out after " + timeout + " milliseconds waiting for web element '" + webElement.toString() + "' gone from DOM");
    }

    /**
     *
     * @param element a {@link org.openqa.selenium.WebElement} object.
     */
    public static void waitUntilElementGetInvisible(WebElement element) {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds())
                .until(ExpectedConditions.not(ExpectedConditions.visibilityOf(element)));
    }

    /**
     *
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @param timeout in milliseconds
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException TODO
     */
    public static void waitForTextInInputExists(WebElement webElement, long timeout) throws WaitException {
        long timeoutTime = DateManager.getCurrentTimestamp() + timeout;
        while (timeoutTime > DateManager.getCurrentTimestamp()) {
            sleep(1);
            if (!webElement.getAttribute("value").isEmpty()) {
                return;
            }
        }
        throw new WaitException("Timed out after '" + timeout + "' milliseconds waiting for existence of '" + webElement + "'");
    }

    /**
     *
     * @param text text to search in page source
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException TODO
     */
    public static void waitForTextPresentInPageSource(String text) throws WaitException {
        long timeoutTime = System.currentTimeMillis() + PageFactory.getTimeOut();
        while (timeoutTime > System.currentTimeMillis()) {
            sleep(1);
            if (PageFactory.getWebDriver().getPageSource().contains(text)) {
                return;
            }
        }
        throw new WaitException("Timed out after '" + PageFactory.getTimeOutInSeconds()+ "' seconds waiting for presence of '" + text + "' in page source");
    }

    /**
     * <p>
     * waitForElementGetEnabled.</p>
     *
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @param timeout a long.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException TODO
     */
    public static void waitForElementGetEnabled(WebElement webElement, long timeout) throws WaitException {
        long timeoutTime = DateManager.getCurrentTimestamp() + timeout;
        while (timeoutTime > DateManager.getCurrentTimestamp()) {
            sleep(1);
            try {
                if (webElement.isEnabled()) {
                    return;
                }
            } catch (Exception e) {
                log.debug("Target element still not enable", e);
            }

        }
        throw new WaitException("Timed out after '" + timeout + "' milliseconds waiting for availability of '" + webElement + "'");
    }

    /**
     *
     * @param existingHandles TODO
     * @param timeout TODO
     * @return TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException TODO
     */
    public static String findNewWindowHandle(Set<String> existingHandles, int timeout) throws WaitException {
        long timeoutTime = System.currentTimeMillis() + timeout;

        while (timeoutTime > System.currentTimeMillis()) {
            Set<String> currentHandles = PageFactory.getWebDriver().getWindowHandles();

            if (currentHandles.size() != existingHandles.size()
                    || (currentHandles.size() == existingHandles.size() && !currentHandles.equals(existingHandles))) {
                for (String currentHandle : currentHandles) {
                    if (!existingHandles.contains(currentHandle)) {
                        return currentHandle;
                    }
                }
            }
            sleep(1);
        }

        throw new WaitException("Timed out after '" + timeout + "' milliseconds waiting for new modal window");
    }

    /**
     *
     * @param existingHandles TODO
     * @return TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException TODO
     */
    public static String findNewWindowHandle(Set<String> existingHandles) throws WaitException {
        return findNewWindowHandle(existingHandles, PageFactory.getTimeOut());
    }

    /**
     * Accept any alert regardless of its message
     *
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException if alert didn't appear during timeout
     */
    public static void acceptAlert() throws WaitException {
        interactWithAlert("", true);
    }

    /**
     * Dismiss any alert regardless of its message
     *
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException if alert didn't appear during timeout
     */
    public static void dismissAlert() throws WaitException {
        interactWithAlert("", false);
    }

    /**
     * Wait for an alert with corresponding text (if specified). Depending on the decision, either accept it or decline
     * If messageText is empty, text doesn't matter
     *
     * @param messageText text of an alert. If empty string is provided, it is being ignored
     * @param decision true - accept, false - dismiss
     * @throws WaitException in case if alert didn't appear during default wait timeout
     */
    public static void interactWithAlert(String messageText, boolean decision) throws WaitException {
        long timeoutTime = System.currentTimeMillis() + PageFactory.getTimeOut();

        while (timeoutTime > System.currentTimeMillis()) {
            try {
                Alert alert = PageFactory.getWebDriver().switchTo().alert();
                if (!messageText.isEmpty()) {
                    Assert.assertEquals(alert.getText(), messageText);
                }
                if (decision) {
                    alert.accept();
                } else {
                    alert.dismiss();
                }
                return;
            } catch (Exception e) {
                log.debug("Alert has not appeared yet", e);
            }
            sleep(1);
        }
        throw new WaitException("Timed out after '" + PageFactory.getTimeOutInSeconds() + "' seconds waiting for alert to accept");
    }

    /**
     * Turn on element highlight
     *
     * @param webElement TODO
     * @return initial element style
     */
    public static String highlightElementOn(WebElement webElement) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) PageFactory.getWebDriver();
            String style = (String) js.executeScript("return arguments[0].style.border", webElement);
            js.executeScript("arguments[0].style.border='3px solid red'", webElement);
            return style;
        } catch (Exception e) {
            log.debug("Something went wrong with element highlight", e);
            return null;
        }
    }

    /**
     * Turn off element highlight
     *
     * @param webElement TODO
     * @param style element style to set
     */
    public static void highlightElementOff(WebElement webElement, String style) {
        if (style == null) {
            return;
        }
        try {
            JavascriptExecutor js = (JavascriptExecutor) PageFactory.getWebDriver();
            js.executeScript("arguments[0].style.border='" + style + "'", webElement);
        } catch (Exception e) {
            log.debug("Something went wrong with element highlight", e);
        }
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     * @param timeout a {int} object. wait text during sec period
     * @return true if exists
     */
    public static boolean checkElementWithTextIsPresent(String text, int timeout) {
        try {
            new WebDriverWait(PageFactory.getWebDriver(), timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), '" + text + "')]")));
            return true;
        } catch (TimeoutException e) {
            log.debug("Element with text {} is not located on page", text, e);
            return false;
        }
    }

    /**
     *
     * @param sec a int.
     */
    private static void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException e) {
            log.warn("Error while thread is sleeping", e);
            Thread.currentThread().interrupt();
        }
    }
}
