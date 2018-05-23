package ru.sbtqa.tag.stepdefs;

import cucumber.api.DataTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.pagefactory.exceptions.SwipeException;
import ru.sbtqa.tag.pagefactory.extensions.MobileExtension;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.qautils.i18n.I18N;
import ru.sbtqa.tag.qautils.strategies.DirectionStrategy;
import ru.sbtqa.tag.qautils.strategies.MatchStrategy;
import ru.yandex.qatools.htmlelements.element.Button;
import ru.yandex.qatools.htmlelements.element.CheckBox;
import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.element.Image;
import ru.yandex.qatools.htmlelements.element.Link;
import ru.yandex.qatools.htmlelements.element.Radio;
import ru.yandex.qatools.htmlelements.element.Table;
import ru.yandex.qatools.htmlelements.element.TextBlock;
import ru.yandex.qatools.htmlelements.element.TextInput;

/**
 * Basic step definitions, that should be available on every project Notations
 * used in this class: Block - a class that extends {@link HtmlElement} and has
 * {@link ru.sbtqa.tag.pagefactory.annotations.ElementTitle} annotation Action -
 * a method with {@link ru.sbtqa.tag.pagefactory.annotations.ActionTitle}
 * annotation in page object List - list of {@link WebElement}'s with
 * {@link ru.sbtqa.tag.pagefactory.annotations.ElementTitle} annotation on page
 * object
 * <p>
 * To pass a Cucumber {@link DataTable} as a parameter to method,
 * supply a table in the following format after a step ini feature:
 * <p>
 * | header 1| header 2 | | value 1 | value 2 |
 * <p>
 * This table will be converted to a {@link DataTable} object.
 * First line is not enforced to be a header.
 * <p>
 * To pass a list as parameter, use flattened table as follows: | value 1 | }
 * value 2 |
 *
 * @see <a href="https://cucumber.io/docs/reference#step-definitions">Cucumber
 * documentation</a>
 */
public class GenericStepDefs extends SetupSteps {

    private static final Logger LOG = LoggerFactory.getLogger(GenericStepDefs.class);
    
    /**
     * Execute action with no parameters inside block element User|he keywords
     * are optional
     *
     * @param block path or name of the block
     * @param action title of the action to execute
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist in
     * specified block
     * @throws NoSuchElementException if block with given name couldn't be found
     */
    public void userActionInBlockNoParams(String block, String action) throws PageInitializationException,
            NoSuchMethodException, NoSuchElementException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitleInBlock(block, action);
    }

    /**
     * Execute action with parameters taken from specified {@link DataTable}
     * inside block element User|he keywords are optional
     *
     * @param block path or name of the block
     * @param action title of the action to execute
     * @param dataTable table of parameters
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist in
     * specified block
     * @throws NoSuchElementException if block with given name couldn't be found
     */
    public void userActionInBlockTableParam(String block, String action, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitleInBlock(block, action, dataTable);
    }

    /**
     * Execute action with one parameter inside block element User|he keywords
     * are optional
     *
     * @param block path or name of the block
     * @param action title of the action to execute
     * @param param parameter
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist in
     * specified block
     * @throws NoSuchElementException if block with given name couldn't be found
     */
    public void userActionInBlockOneParam(String block, String action, String param) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitleInBlock(block, action, param);
    }

    /**
     * Execute action with two parameters inside block element User|he keywords
     * are optional
     *
     * @param block path or name of the block
     * @param action title of the action to execute
     * @param param1 first parameter
     * @param param2 second parameter
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist in
     * specified block
     * @throws NoSuchElementException if block with given name couldn't be found
     */
    public void userActionInBlockTwoParams(String block, String action, String param1, String param2) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitleInBlock(block, action, param1, param2);
    }

    /**
     * Find element inside given block. Element name itself is a parameter, and
     * defines type of the element to search for User|he keywords are optional
     *
     * @param block path or name of the block
     * @param elementType type of the searched element. Could be one of Yandex
     * element types types
     * @param elementTitle title of the element to search
     * @throws PageException if current page is not initialized, or element
     * wasn't found
     */
    public void findElementInBlock(String block, String elementType, String elementTitle) throws PageException {
        String[] packages = this.getClass().getCanonicalName().split("\\."); 
        String currentLanguage = packages[packages.length - 2]; 
        I18N i18n = I18N.getI18n(GenericStepDefs.class, new Locale(currentLanguage));
        String key = i18n.getKey(elementType);
        Class<? extends WebElement> clazz;
        switch (key) {
            case "ru.sbtqa.tag.pagefactory.type.element":
                clazz = WebElement.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.textinput":
                clazz = TextInput.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.checkbox":
                clazz = CheckBox.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.radiobutton":
                clazz = Radio.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.table":
                clazz = Table.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.header":
                clazz = TextBlock.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.button":
                clazz = Button.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.link":
                clazz = Link.class;
                break;
            case "ru.sbtqa.tag.pagefactory.type.image":
                clazz = Image.class;
                break;
            default:
                clazz = WebElement.class;
        }
        PageFactory.getInstance().getCurrentPage().findElementInBlockByTitle(block, elementTitle, clazz);
    }

    /**
     * Find element with given value in specified list User|he keywords are
     * optional
     *
     * @param listTitle title of the list to search for
     * @param value required value of the element. for text elements value is
     * being checked via getText() method
     * @throws PageException if page wasn't initialized of required list wasn't
     * found
     */
    public void findElementInList(String listTitle, String value) throws PageException {
        boolean found = false;
        for (WebElement webElement : PageFactory.getInstance().getCurrentPage().findListOfElements(listTitle)) {
            if (webElement.getText().equals(value)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new AutotestError(String.format("Element with text '%s' is absent in list '%s'", value, listTitle));
        }
    }

    /**
     * Initialize a page with corresponding title (defined via
     * {@link ru.sbtqa.tag.pagefactory.annotations.PageEntry} annotation)
     * User|he keywords are optional
     *
     * @param title of the page to initialize
     * @throws PageInitializationException if page initialization failed
     */
    public void openPage(String title) throws PageInitializationException {
        if (PageFactory.getEnvironment() != Environment.MOBILE
                && !PageFactory.getWebDriver().getWindowHandles().isEmpty()) {
            for (String windowHandle : PageFactory.getWebDriver().getWindowHandles()) {
                PageFactory.getWebDriver().switchTo().window(windowHandle);
            }
        }
        PageFactory.getInstance().getPage(title);
    }

    /**
     * Execute action with no parameters User|he keywords are optional
     *
     * @param action title of the action to execute
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userActionNoParams(String action) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action);
    }

    /**
     * Execute action with one parameter User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param param parameter
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userActionOneParam(String action, String param) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, param);
    }

    /**
     * Execute action with two parameters User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param param1 first parameter
     * @param param2 second parameter
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userActionTwoParams(String action, String param1, String param2) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, param1, param2);
    }

    /**
     * Execute action with three parameters User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param param1 first parameter
     * @param param2 second parameter
     * @param param3 third parameter
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userActionThreeParams(String action, String param1, String param2, String param3) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, param1, param2, param3);
    }

    /**
     * Execute action with parameters from given {@link DataTable}
     * User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param dataTable table of parameters
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userActionTableParam(String action, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, dataTable);
    }

    /**
     * Execute action with string parameter and {@link DataTable}
     * User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param param parameter
     * @param dataTable table of parameters
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userDoActionWithObject(String action, String param, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, param, dataTable);
    }

    /**
     * Execute action with parameters taken from list User|he keywords are
     * optional
     *
     * @param action title of the action to execute
     * @param list parameters list
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    public void userActionListParam(String action, List<String> list) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, list);
    }

    /**
     * Open a copy for current page in a new browser tab User|he keywords are
     * optional
     */
    public void openCopyPage() {
        String pageUrl = PageFactory.getWebDriver().getCurrentUrl();
        ((JavascriptExecutor) PageFactory.getWebDriver()).executeScript("window.open('" + pageUrl + "', '_blank')");
        List<String> tabs = new ArrayList<>(PageFactory.getWebDriver().getWindowHandles());
        PageFactory.getWebDriver().switchTo().window(tabs.get(tabs.size() - 1));
    }

    /**
     * Switch to a neighbour browser tab
     */
    public void switchesToNextTab() {
        String currentTab = PageFactory.getWebDriver().getWindowHandle();
        List<String> tabs = new ArrayList<>(PageFactory.getWebDriver().getWindowHandles());
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).equals(currentTab)) {
                PageFactory.getWebDriver().switchTo().window(tabs.get(i + 1));
                return;
            }
        }
    }

    /**
     * Check that current URL matches the inputted one
     *
     * @param url url for comparison
     */
    public void urlMatches(String url) {
        Assert.assertEquals("URL is different from the expected: ", url, PageFactory.getWebDriver().getCurrentUrl());
    }

    /**
     * Close current browser tab and open a tab with given name
     *
     * @param title title of the page to open
     */
    public void closingCurrentWin(String title) {
        PageFactory.getWebDriver().close();
        for (String windowHandle : PageFactory.getWebDriver().getWindowHandles()) {
            PageFactory.getWebDriver().switchTo().window(windowHandle);
            if (PageFactory.getWebDriver().getTitle().equals(title)) {
                return;
            }
        }
        throw new AutotestError("Unable to return to the previously opened page: " + title);
    }

    /**
     * Return to previous location (via browser "back" button)
     */
    public void backPage() {
        PageFactory.getWebDriver().navigate().back();
    }

    /**
     * Go to specified url
     *
     * @param url url to go to
     */
    public void goToUrl(String url) {
        PageFactory.getWebDriver().get(url);
    }

    /**
     * Initialize a page with corresponding URL
     *
     * @param url value of the
     * {@link ru.sbtqa.tag.pagefactory.annotations.PageEntry#url} to search for
     * @throws PageInitializationException if page with corresponding URL is
     * absent or couldn't be initialized
     */
    public void goToPageByUrl(String url) throws PageInitializationException {
        PageFactory.getInstance().changeUrlByTitle(url);
    }

    /**
     * Refresh browser page
     */
    public void reInitPage() {
        PageFactory.getWebDriver().navigate().refresh();
    }

    /**
     * Swipe until text is visible
     *
     * @param direction direction to swipe
     * @param text text on page to swipe to
     * @throws SwipeException if the text is not found or swipe depth is reached
     */
    public void swipeToText(String direction, String text) throws SwipeException {
        MobileExtension.swipeToText(DirectionStrategy.valueOf(direction.toUpperCase()), text);
    }

    /**
     * Swipe until text is visible for Android
     *
     * @param strategy contains or exact
     * @param text text on page to swipe to
     * @throws SwipeException if the text is not found
     */
    public void swipeToTextAndroid(String text, String strategy) throws SwipeException {
        MobileExtension.swipeToText(MatchStrategy.valueOf(strategy), text);
    }

    /**
     * Element is focused
     *
     * @param element element to focus on
     */
    public void isElementFocused(String element) {
        LOG.warn("Note that isElementFocused method is still an empty!");
    }
}
