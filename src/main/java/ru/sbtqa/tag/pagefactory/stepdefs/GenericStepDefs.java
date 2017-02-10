package ru.sbtqa.tag.pagefactory.stepdefs;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.exceptions.DirectionException;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.pagefactory.exceptions.SwipeException;
import ru.sbtqa.tag.pagefactory.extensions.MobileExtension;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.qautils.strategies.DirectionStrategy;

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
 * To pass a Cucumber {@link cucumber.api.DataTable} as a parameter to method,
 * supply a table in the following format after a step ini feature:
 * <p>
 * | header 1| header 2 | | value 1 | value 2 |
 * <p>
 * This table will be converted to a {@link cucumber.api.DataTable} object.
 * First line is not enforced to be a header.
 * <p>
 * To pass a list as parameter, use flattened table as follows: | value 1 | }
 * value 2 |
 *
 * @see <a href="https://cucumber.io/docs/reference#step-definitions">Cucumber
 * documentation</a>
 */
public class GenericStepDefs {

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
    @And("userActionInBlockNoParams")
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
    @And("userActionInBlockTableParam")
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
    @And("userActionInBlockOneParam")
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
    @And("userActionInBlockTwoParams")
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
    @And("findElementInBlock")
    public void findElementInBlock(String block, String elementType, String elementTitle) throws PageException {
        Class<? extends WebElement> clazz;
        switch (elementType) {
            case "element":
            case "элемент":
                clazz = WebElement.class;
                break;
            case "textinput":
            case "текстовое поле":
                clazz = TextInput.class;
                break;
            case "checkbox":
            case "чекбокс":
                clazz = CheckBox.class;
                break;
            case "radiobutton":
            case "радиобатон":
                clazz = Radio.class;
                break;
            case "table":
            case "таблицу":
                clazz = Table.class;
                break;
            case "header":
            case "заголовок":
                clazz = TextBlock.class;
                break;
            case "button":
            case "кнопку":
                clazz = Button.class;
                break;
            case "link":
            case "ссылку":
                clazz = Link.class;
                break;
            case "image":
            case "изображение":
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
    @And("findElementInList")
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
    @And("openPage")
    public void openPage(String title) throws PageInitializationException {
        if (PageFactory.getEnvironment() != Environment.MOBILE && 
	      !PageFactory.getWebDriver().getWindowHandles().isEmpty()) {
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
    @And("userActionNoParams")
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
    @And("userActionOneParam")
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
    @And("userActionTwoParams")
    public void userActionTwoParams(String action, String param1, String param2) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, param1, param2);
    }

    /**
     * Execute action with three parameters User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param param1 first parameter
     * @param param2 second patrameter
     * @param param3 third parameter
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    @And("userActionThreeParams")
    public void userActionThreeParams(String action, String param1, String param2, String param3) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, param1, param2, param3);
    }

    /**
     * Execute action with parameters from given {@link cucumber.api.DataTable}
     * User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param dataTable table of parameters
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    @And("userActionTableParam")
    public void userActionTableParam(String action, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, dataTable);
    }

    /**
     * Execute action with string parameter and {@link cucumber.api.DataTable}
     * User|he keywords are optional
     *
     * @param action title of the action to execute
     * @param param parameter
     * @param dataTable table of parameters
     * @throws PageInitializationException if current page is not initialized
     * @throws NoSuchMethodException if corresponding method doesn't exist
     */
    @And("userDoActionWithObject")
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
    @And("userActionListParam")
    public void userActionListParam(String action, List<String> list) throws PageInitializationException, NoSuchMethodException {
        PageFactory.getInstance().getCurrentPage().executeMethodByTitle(action, list);
    }

    /**
     * Open a copy for current page in a new browser tab User|he keywords are
     * optional
     */
    @And("openCopyPage")
    public void openCopyPage() {
        String pageUrl = PageFactory.getWebDriver().getCurrentUrl();
        ((JavascriptExecutor) PageFactory.getWebDriver()).executeScript("window.open('" + pageUrl + "', '_blank')");
        List<String> tabs = new ArrayList<>(PageFactory.getWebDriver().getWindowHandles());
        PageFactory.getWebDriver().switchTo().window(tabs.get(tabs.size() - 1));
        Assert.assertEquals("Fails to open a new page. "
                + "URL is different from the expected: ", pageUrl, PageFactory.getWebDriver().getCurrentUrl());
    }

    /**
     * Switch to a neighbour browser tab
     */
    @And("switchesToNextTab")
    public void switchesToNextTab() {
        List<String> tabs = new ArrayList<>(PageFactory.getWebDriver().getWindowHandles());
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).equals(PageFactory.getWebDriver().getWindowHandle())) {
                PageFactory.getWebDriver().switchTo().window(tabs.get(i + 1));
            }
        }
    }

    /**
     * Check that current URL matches the inputted one
     *
     * @param url url for comparison
     */
    @And("urlMatches")
    public void urlMatches(String url) {
        Assert.assertEquals("URL is different from the expected: ", url, PageFactory.getWebDriver().getCurrentUrl());
    }

    /**
     * Close current browser tab and open a tab with given name
     *
     * @param title title of the page to open
     */
    @And("closingCurrentWin")
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
    @And("backPage")
    public void backPage() {
        PageFactory.getWebDriver().navigate().back();
    }

    /**
     * Initialize a page with corresponding URL
     *
     * @param url value of the
     * {@link ru.sbtqa.tag.pagefactory.annotations.PageEntry#url} to search for
     * @throws PageInitializationException if page with corresponding URL is
     * absent or couldn't be initialized
     */
    @And("goToPageByUrl")
    public void goToPageByUrl(String url) throws PageInitializationException {
        PageFactory.getInstance().changeUrlByTitle(url);
    }

    /**
     * Refresh browser page
     */
    @And("reInitPage")
    public void reInitPage() {
        PageFactory.getWebDriver().navigate().refresh();
    }
    
    /**
     * Swipe to text
     * @param direction direction to swipe
     * @param text text on page to swipe to
     * @throws DirectionException if specified unsupported direction
     * @throws SwipeException if the text is not found or swipe depth is reached
     */
    @And("swipeToText")
    public void swipeToText(String direction, String text) throws DirectionException, SwipeException {
        MobileExtension.swipeToText(DirectionStrategy.valueOf(direction.toUpperCase()), text);
    }
    
    
}
