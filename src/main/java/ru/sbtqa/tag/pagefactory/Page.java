package ru.sbtqa.tag.pagefactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.datajack.Stash;
import ru.sbtqa.tag.pagefactory.annotations.ActionTitle;
import ru.sbtqa.tag.pagefactory.annotations.ActionTitles;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.annotations.PageEntry;
import ru.sbtqa.tag.pagefactory.annotations.RedirectsTo;
import ru.sbtqa.tag.pagefactory.annotations.ValidationRule;
import ru.sbtqa.tag.pagefactory.exceptions.*;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.sbtqa.tag.qautils.strategies.MatchStrategy;
import ru.yandex.qatools.htmlelements.element.CheckBox;
import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;

import static java.lang.String.format;

/**
 * Base page object class. Contains basic actions with elements, search methods
 */
public abstract class Page {

    private static final Logger log = LoggerFactory.getLogger(Page.class);

    /**
     * Initialize page with specified title and save its instance to {@link PageShell#currentPage} for further use
     *
     * @param title
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException if failed to execute corresponding page constructor
     */
    @ActionTitle("отркывается страница")
    @ActionTitle("open page")
    public void initPage(String title) throws PageInitializationException {
        PageFactory.getInstance().getPage(title);
    }

    /**
     * Find element with specified title annotation, and fill it with given text
     * Add elementTitle-&gt;text as parameter-&gt;value to corresponding step in allure report
     *
     * @param elementTitle element to fill
     * @param text         text to enter
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if page was not initialized, or required element couldn't be found
     */
    @ActionTitle("заполняет поле")
    @ActionTitle("fill the field")
    public void fillField(String elementTitle, String text) throws PageException {
        WebElement webElement = getElementByTitle(elementTitle);
        webElement.click();
        webElement.clear();
        webElement.sendKeys(text);
        Core.addToReport(elementTitle, text);
    }

    /**
     * Same as {@link #fillField(String, String)}, but accepts particular WebElement object and interacts with it
     *
     * @param webElement an object to interact with
     * @param text       string text to send to element
     */
    public void fillField(WebElement webElement, String text) {
        if (null != text) {
            try {
                webElement.clear();
            } catch (InvalidElementStateException | NullPointerException e) {
                log.debug("Failed to clear web element {}", webElement, e);
            }
            webElement.sendKeys(text);
        }

        Core.addToReport(webElement, text);
    }

    /**
     * Click the specified link element
     *
     * @param webElement a WebElement object to click
     */
    public void clickWebElement(WebElement webElement) {
        webElement.click();
        Core.addToReport(webElement, " is clicked");
    }

    /**
     * Find specified WebElement on a page, and click it
     * Add corresponding parameter to allure report
     *
     * @param elementTitle title of the element to click
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if page was not initialized, or required element couldn't be found
     */
    @ActionTitle("кликает по ссылке")
    @ActionTitle("click the link")
    @ActionTitle("нажимает кнопку")
    @ActionTitle("click the button")
    public void clickElementByTitle(String elementTitle) throws PageException {
        WebElement webElement;
        try {
            webElement = getElementByTitle(elementTitle);
            DriverExtensions.waitForElementGetEnabled(webElement, PageFactory.getTimeOut());
        } catch (NoSuchElementException | WaitException e) {
            log.warn("Failed to find element by title {}", elementTitle, e);
            webElement = DriverExtensions.waitUntilElementAppearsInDom(By.partialLinkText(elementTitle));
        }
        clickWebElement(webElement);
    }

    /**
     * Press specified key on a keyboard
     * Add corresponding parameter to allure report
     *
     * @param keyName name of the key. See available key names in {@link Keys}
     */
    @ActionTitle("нажимает клавишу")
    @ActionTitle("press the key")
    public void pressKey(String keyName) {
        Keys key = Keys.valueOf(keyName.toUpperCase());
        Actions actions = new Actions(PageFactory.getWebDriver());
        actions.sendKeys(key).perform();
        Core.addToReport(keyName, " is pressed");
    }

    /**
     * Focus a WebElement, and send specified key into it
     * Add corresponding parameter to allure report
     *
     * @param keyName      name of the key. See available key names in {@link Keys}
     * @param elementTitle title of WebElement that accepts key commands
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if couldn't find element with required  title
     */
    @ActionTitle("нажимает клавишу")
    @ActionTitle("press the key")
    public void pressKey(String keyName, String elementTitle) throws ElementNotFoundException {
        Keys key = Keys.valueOf(keyName.toUpperCase());
        Actions actions = new Actions(PageFactory.getWebDriver());
        actions.moveToElement(getElementByTitle(elementTitle));
        actions.click();
        actions.sendKeys(key);
        actions.build().perform();
        Core.addToReport(keyName, " is pressed on element " + elementTitle + "'");
    }

    /**
     * Send key to element and add corresponding parameter to allure report
     *
     * @param webElement WebElement to send keys in
     * @param keyName    name of the key. See available key names in {@link Keys}
     */
    public void pressKey(WebElement webElement, Keys keyName) {
        webElement.sendKeys(keyName);
        Core.addToReport(webElement, " is pressed by key '" + keyName + "'");
    }

    /**
     * Find web element with corresponding title, if it is a check box, select it
     * If it's a WebElement instance, check whether it is already selected, and click if it's not
     * Add corresponding parameter to allure report
     *
     * @param elementTitle WebElement that is supposed to represent checkbox
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if page was not initialized, or required element couldn't be found
     */
    @ActionTitle("отмечает признак")
    @ActionTitle("select CheckBox")
    public void setCheckBox(String elementTitle) throws PageException {
        WebElement targetElement = getElementByTitle(elementTitle);
        if (targetElement.getClass().isAssignableFrom(CheckBox.class)) {
            ((CheckBox) targetElement).select();
        } else {
            setCheckBoxState(targetElement, true);
        }
        Core.addToReport(elementTitle, " is checked");
    }

    /**
     * Check whether specified element is selected, if it isn't, click it
     * isSelected() doesn't guarantee correct behavior if given element is not a selectable (checkbox,dropdown,radiobtn)
     *
     * @param webElement a WebElemet object.
     * @param state      a boolean object.
     */
    public void setCheckBoxState(WebElement webElement, Boolean state) {
        if (null != state) {
            if (webElement.isSelected() != state) {
                webElement.click();
            }
        }
        Core.addToReport(webElement, " is turned to '" + state + "' state");
    }

    /**
     * Find element with required title, perform {@link #select(WebElement, String, MatchStrategy)} on found element
     * Use exact match strategy
     *
     * @param elementTitle WebElement that is supposed to be selectable
     * @param option       option to select
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if required element couldn't be found
     */
    @ActionTitle("выбирает")
    @ActionTitle("select")
    public void select(String elementTitle, String option) throws ElementNotFoundException {
        WebElement webElement = getElementByTitle(elementTitle);
        if (null != option) {
            select(webElement, option, MatchStrategy.EXACT);
        }
    }

    /**
     * Find element with required title, perform {@link #select(WebElement, String, MatchStrategy)} on found element
     * Use given match strategy
     *
     * @param elementTitle the title of SELECT element to interact
     * @param option       the value to match against
     * @param strategy     the strategy to match value
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if required element couldn't be found
     */
    public void select(String elementTitle, String option, MatchStrategy strategy) throws ElementNotFoundException {
        WebElement webElement = getElementByTitle(elementTitle);
        select(webElement, option, strategy);
    }

    /**
     * Try to extract selectable options form given WebElement, and select required one
     * Add corresponding parameter to allure report
     *
     * @param webElement WebElement for interaction. Element is supposed to be selectable, i.e. have select options
     * @param option     the value to match against
     * @param strategy   the strategy to match value. See {@link MatchStrategy} for available values
     */
    public void select(WebElement webElement, String option, MatchStrategy strategy) {
        String jsString = ""
                + "var content=[]; "
                + "var options = arguments[0].getElementsByTagName('option'); "
                + " for (var i=0; i<options.length;i++){"
                + " content.push(options[i].text)"
                + "}"
                + "return content";
        List<String> options = (ArrayList<String>) ((JavascriptExecutor) PageFactory.getWebDriver()).
                executeScript(jsString, webElement);

        boolean isSelectionMade = false;
        for (int index = 0; index < options.size(); index++) {
            boolean isCurrentOption = false;
            String optionText = options.get(index).replaceAll("\\s+", "");
            String needOptionText = option.replaceAll("\\s+", "");

            if (strategy.equals(MatchStrategy.CONTAINS)) {
                isCurrentOption = optionText.contains(needOptionText);
            } else if (strategy.equals(MatchStrategy.EXACT)) {
                isCurrentOption = optionText.equals(needOptionText);
            }

            if (isCurrentOption) {
                Select select = new Select(webElement);
                select.selectByIndex(index);
                isSelectionMade = true;
                break;
            }
        }

        if (!isSelectionMade) {
            try {
                throw new AutotestError("There is no element '" + option + "' in " + getElementTitle(webElement));
            } catch (ElementDescriptionException ex) {
                throw new AutotestError("There is no element '" + option + "' in " + webElement, ex);
            }
        }

        Core.addToReport(webElement, option);
    }

    /**
     * Wait for an alert with specified text, and accept it
     *
     * @param text alert message
     * @throws WaitException in case if alert didn't appear during default wait timeout
     */
    @ActionTitle("принимает уведомление")
    @ActionTitle("accepts alert")
    public void acceptAlert(String text) throws WaitException {
        DriverExtensions.interactWithAlert(text, true);
    }

    /**
     * Wait for an alert with specified text, and dismiss it
     *
     * @param text alert message
     * @throws WaitException in case if alert didn't appear during default wait timeout
     */
    @ActionTitle("отклоняет уведомление")
    @ActionTitle("dismisses alert")
    public void dismissAlert(String text) throws WaitException {
        DriverExtensions.interactWithAlert(text, false);
    }

    /**
     * Wait for appearance of the required text in current DOM model. Text will be space-trimmed, so only non-space
     * characters will matter.
     *
     * @param text text to search
     * @throws WaitException if text didn't appear on the page during the timeout
     */
    @ActionTitle("текст появляется на странице")
    @ActionTitle("text appears on the page")
    public void assertTextAppears(String text) throws WaitException {
        long timeOutTime = System.currentTimeMillis() + PageFactory.getTimeOut();
        Throwable exception = null;
        String body;
        while (timeOutTime > System.currentTimeMillis()) {
            try {
                Thread.sleep(1000);
                body = PageFactory.getWebDriver().findElement(By.tagName("body")).getText().replaceAll("\\s+", "");
                Assert.assertTrue(body.contains(text.replaceAll("\\s+", "")));
            } catch (Throwable e) {
                exception = e;
            }
        }
        throw new WaitException("Timeout while waiting for the text '" + text + "' on page '" + this.getTitle() + "'",
                exception);
    }

    /**
     * Wait for a new browser window, then wait for a specific text inside the appeared window
     * List of previously opened windows is being saved before each click, so if modal window appears without click,
     * this method won't catch it.
     * Text is being waited by {@link #assertTextAppears}, so it will be space-trimmed as well
     *
     * @param text text that will be searched inside of the window
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException if
     */
    @ActionTitle("появляется модальное окно с текстом")
    @ActionTitle("modal window with text appears")
    public void assertModalWindowAppears(String text) throws WaitException {
        try {
            String popupHandle = DriverExtensions.findNewWindowHandle(Stash.getValue("beforeClickHandles"));
            if (null != popupHandle && !popupHandle.isEmpty()) {
                PageFactory.getWebDriver().switchTo().window(popupHandle);
            }
            assertTextAppears(text);
        } catch (Exception ex) {
            throw new WaitException("Modal window with text '" + text + "' didn't appear during timeout", ex);
        }
    }

    /**
     * Check whether specified text is basent on the page. Text is being space-trimmed before assertion, so only
     * non-space characters will matter
     *
     * @param text text to search for
     * @throws AutotestError if text is present on the page
     */
    @ActionTitle("текст отсутствует на странице")
    @ActionTitle("text is absent on the page")
    public void assertTextIsNotPresent(String text) throws AutotestError {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds()).
                until(ExpectedConditions.visibilityOf(PageFactory.getWebDriver().findElement(By.tagName("body"))));
        try {
            Assert.assertFalse(PageFactory.getWebDriver().findElement(By.tagName("body")).getText().replaceAll("\\s+", "").contains(text.replaceAll("\\s+", "")));
        } catch (AssertionError exception) {
            throw new AutotestError("The message with text '" + text + "' has appeared", exception);
        }
    }

    /**
     * Perform {@link #checkValue(String, WebElement, MatchStrategy)} for the WebElement with corresponding title
     * on a current page. Use exact match strategy
     *
     * @param text         string value that will be searched inside of the element
     * @param elementTitle title of the element to search
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if couldn't find element by given title
     * @throws ru.sbtqa.tag.qautils.errors.AutotestError                    if found element doesn't contain required text
     */
    @ActionTitle("проверяет значение")
    @ActionTitle("checks value")
    public void checkValue(String elementTitle, String text) throws ElementNotFoundException, AutotestError {
        checkValue(text, getElementByTitle(elementTitle), MatchStrategy.EXACT);
    }

    /**
     * Perform {@link #checkValue(String, WebElement, MatchStrategy)} for the specified WebElement.
     * Use exact match strategy
     *
     * @param text       string value that will be searched inside of the element
     * @param webElement WebElement to check
     * @throws ru.sbtqa.tag.qautils.errors.AutotestError if element doesn't contain required text
     */
    public void checkValue(String text, WebElement webElement) throws AutotestError {
        checkValue(text, webElement, MatchStrategy.EXACT);
    }

    /**
     * Define a type of given WebElement, and check whether it either contains, or exactly matches given text
     * in its value. Currently supported elements are text input and select box
     * TODO: use HtmlElements here, to define which element we are dealing with
     *
     * @param text           string value that will be searched inside of the element
     * @param webElement     WebElement to check
     * @param searchStrategy match strategy. See available strategies in {@link MatchStrategy}
     */
    public void checkValue(String text, WebElement webElement, MatchStrategy searchStrategy) {
        String value = "";
        switch (searchStrategy) {
            case EXACT:
                try {
                    switch (webElement.getTagName()) {
                        case "input":
                            value = webElement.getAttribute("value");
                            Assert.assertEquals(text.replaceAll("\\s+", ""), value.replaceAll("\\s+", ""));
                            break;
                        case "select":
                            value = webElement.getAttribute("title");
                            if (value.isEmpty() || !value.replaceAll("\\s+", "").equals(text.replaceAll("\\s+", ""))) {
                                value = webElement.getText();
                            }
                            Assert.assertEquals(text.replaceAll("\\s+", ""), value.replaceAll("\\s+", ""));
                            break;
                        default:
                            value = webElement.getText();
                            Assert.assertEquals(text.replaceAll("\\s+", ""), value.replaceAll("\\s+", ""));
                            break;
                    }
                } catch (Exception | AssertionError exception) {
                    throw new AutotestError("The actual value '" + value + "' of WebElement '" + webElement + "' are not equal to expected text '" + text + "'", exception);
                }
                break;
            case CONTAINS:
                try {
                    switch (webElement.getTagName()) {
                        case "input":
                            value = webElement.getAttribute("value");
                            Assert.assertTrue(value.replaceAll("\\s+", "").contains(text.replaceAll("\\s+", "")));
                            break;
                        case "select":
                            value = webElement.getAttribute("title");
                            if (value.isEmpty() || !value.replaceAll("\\s+", "").contains(text.replaceAll("\\s+", ""))) {
                                value = webElement.getText();
                            }
                            Assert.assertTrue(value.replaceAll("\\s+", "").contains(text.replaceAll("\\s+", "")));
                            break;
                        default:
                            value = webElement.getText();
                            Assert.assertTrue(value.replaceAll("\\s+", "").contains(text.replaceAll("\\s+", "")));
                            break;
                    }
                } catch (Exception | AssertionError exception) {
                    throw new AutotestError("The actual value '" + value + "' of WebElement '" + webElement + "' are not equal to expected text '" + text + "'", exception);
                }
                break;
        }

    }

    /**
     * Check
     *
     * @param elementTitle TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException
     * @throws ru.sbtqa.tag.qautils.errors.AutotestError
     */
    @ActionTitle("проверяет что поле непустое")
    @ActionTitle("checks that the field is not empty")
    public void checkFieldIsNotEmpty(String elementTitle) throws PageException {
        WebElement webElement = getElementByTitle(elementTitle);
        checkFieldIsNotEmpty(webElement);
    }

    /**
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    public void checkFieldIsNotEmpty(WebElement webElement) throws ElementDescriptionException {
        String value = webElement.getText();
        if (value.isEmpty()) {
            value = webElement.getAttribute("value");
        }

        try {
            Assert.assertNotEquals("", value.replaceAll("\\s+", ""));
        } catch (Exception | AssertionError e) {
            throw new AutotestError("The field" + getElementTitle(webElement) + " is empty", e);
        }
    }

    /**
     * @param text         a {@link java.lang.String} object.
     * @param elementTitle TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    @ActionTitle("проверяет несовпадение значения")
    @ActionTitle("check that values are not equal")
    public void checkValuesAreNotEqual(String text, String elementTitle) throws PageException {
        WebElement webElement = PageFactory.getInstance().getCurrentPage().getElementByTitle(elementTitle);
        if (checkValuesAreNotEqual(text, webElement)) {
            throw new AutotestError("'" + text + "' is equal with '" + elementTitle + "' value");
        }
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @return a boolean.
     */
    public boolean checkValuesAreNotEqual(String text, WebElement webElement) {
        if ("input".equals(webElement.getTagName())) {
            return webElement.getAttribute("value").replaceAll("\\s+", "").equals(text.replaceAll("\\s+", ""));
        } else {
            return webElement.getText().replaceAll("\\s+", "").equals(text.replaceAll("\\s+", ""));
        }
    }

    /**
     * Perform a check that there is an element with required text on current page
     *
     * @param text a {@link java.lang.String} object.
     */
    @ActionTitle("существует элемент с текстом")
    @ActionTitle("отображается текст")
    @ActionTitle("check that element with text is present")
    @ActionTitle("check the text is visible")
    public void checkElementWithTextIsPresent(String text) {
        if (!DriverExtensions.checkElementWithTextIsPresent(text, PageFactory.getTimeOutInSeconds())) {
            throw new AutotestError("Text '" + text + "' is not present");
        }
    }

    /**
     * Finds elements list in context of current page See
     * ${@link Core#findListOfElements(String, Class, Object)} for detailed
     * description
     *
     * @param listTitle value of ElementTitle annotation of required element
     * @param type      type of elements in list that is being searched for
     * @param <T>       type of elements in returned list
     * @return list of elements of particular type
     * @throws PageException if nothing found or current page is not initialized
     */
    public <T extends WebElement> List<T> findListOfElements(String listTitle, Class<T> type)
            throws PageException {
        return Core.findListOfElements(listTitle, type, this);
    }

    /**
     * Finds elements list in context of current page See
     * ${@link Core#findListOfElements(String, Class, Object)} for detailed
     * description
     *
     * @param listTitle value of ElementTitle annotation of required element
     * @return list of WebElement's
     * @throws PageException if nothing found or current page is not initialized
     */
    public List<WebElement> findListOfElements(String listTitle) throws PageException {
        return Core.findListOfElements(listTitle, WebElement.class, this);
    }

    /**
     * Find elements list in context of required block See
     * ${@link Core#findListOfElements(String, Class, Object)} for detailed
     * description
     *
     * @param listTitle value of ElementTitle annotation of required element
     * @param type      type of elements in list that is being searched for
     * @param <T>       type of elements in returned list
     * @return list of elements of particular type
     * @throws PageException if nothing found or current page is not initialized
     */
    public <T extends WebElement> List<T> findListOfElementsInBlock(String blockPath, String listTitle, Class<T> type)
            throws PageException {
        Object block = findBlock(blockPath);
        return Core.findListOfElements(listTitle, type, block);
    }

    /**
     * Finds elements list in context of required block See
     * ${@link Core#findListOfElements(String, Class, Object)} for detailed
     * description
     *
     * @param listTitle value of ElementTitle annotation of required element
     * @return list of WebElement's
     * @throws PageException if nothing found or current page is not initialized
     */
    public List<WebElement> findListOfElementsInBlock(String blockPath, String listTitle) throws PageException {
        return findListOfElementsInBlock(blockPath, listTitle, WebElement.class);
    }

    /**
     * See {@link Core#findBlocks(String, Object, boolean)} for description. This
     * wrapper finds only one block. Search is being performed on a current page
     *
     * @param blockPath full path or just a name of the block to search
     * @return first found block object
     * @throws java.util.NoSuchElementException if couldn't find any block
     */
    public HtmlElement findBlock(String blockPath) throws java.util.NoSuchElementException {
        try {
            List<HtmlElement> blocks = Core.findBlocks(blockPath, this, true);
            if (blocks.isEmpty()) {
                throw new java.util.NoSuchElementException(format("Couldn't find block '%s' on a page '%s'",
                        blockPath, this.getTitle()));
            }
            return blocks.get(0);
        } catch (IllegalAccessException ex) {
            throw new FactoryRuntimeException(format("Internal error during attempt to find block '%s'", blockPath), ex);
        }
    }

    /**
     * See {@link Core#findBlocks(String, Object, boolean)} for description. Search
     * is being performed on a current page
     *
     * @param blockPath full path or just a name of the block to search
     * @return list of objects that were found by specified path
     */
    public List<HtmlElement> findBlocks(String blockPath) throws java.util.NoSuchElementException {
        try {
            return Core.findBlocks(blockPath, this, false);
        } catch (IllegalAccessException ex) {
            throw new FactoryRuntimeException(format("Internal error during attempt to find a block '%s'", blockPath), ex);
        }
    }

    /**
     * Execute parameter-less method inside of the given block element.
     *
     * @param block       block title, or a block chain string separated with '-&gt;'
     *                    symbols
     * @param actionTitle title of the action to execute
     * @throws AutotestError if block (block chain) wasn't found, or there is no
     *                       such method in block
     */
    public void takeActionInBlock(String block, String actionTitle)
            throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        takeActionInBlock(block, actionTitle, new Object[0]);
    }

    /**
     * Execute method with one or more parameters inside of the given block
     * element !BEWARE! If there are several elements found by specified block
     * path, a first one will be used!
     *
     * @param blockPath   block title, or a block chain string separated with
     *                    '-&gt;' symbols
     * @param actionTitle title of the action to execute
     * @param parameters  parameters that will be passed to method
     * @throws AutotestError if block (block chain) wasn't found, or there is no
     *                       such method in block
     */
    public void takeActionInBlock(String blockPath, String actionTitle, Object... parameters)
            throws AutotestError, InvocationTargetException, IllegalAccessException {
        HtmlElement block = findBlock(blockPath);
        Method[] methods = block.getClass().getMethods();
        for (Method method : methods) {
            if (Core.isRequiredAction(method, actionTitle)) {
                try {
                    method.setAccessible(true);
                    if (parameters == null || parameters.length == 0) {
                        MethodUtils.invokeMethod(block, method.getName());
                    } else {
                        MethodUtils.invokeMethod(block, method.getName(), parameters);
                    }
                    break;
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new AutotestError(format("Could not find action '%s' in block the following block: '%s'",
                            actionTitle, blockPath));
                }
            }
        }
    }

    /**
     * Helper methods for manipulations on fields and objects
     */
    private static class Core {

        /**
         * Return a list of methods declared tin the given class and its superclasses
         *
         * @param clazz class to check
         * @return list of methods. could be empty list
         */
        private static List<Method> getDeclaredMethods(Class clazz) {
            List<Method> methods = new ArrayList<>();

            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

            Class supp = clazz.getSuperclass();

            while (supp != java.lang.Object.class) {
                methods.addAll(Arrays.asList(supp.getDeclaredMethods()));
                supp = supp.getSuperclass();
            }

            return methods;
        }

        /**
         * Check whether given method has {@link ActionTitle} or {@link ActionTitles} annotation with required title
         *
         * @param method method to check
         * @param title  required title
         * @return true|false
         */
        private static Boolean isRequiredAction(Method method, String title) {
            ActionTitle actionTitle = method.getAnnotation(ActionTitle.class);
            ActionTitles actionTitles = method.getAnnotation(ActionTitles.class);
            List<ActionTitle> actionList = new ArrayList<>();
            if (actionTitles != null) {
                actionList.addAll(Arrays.asList(actionTitles.value()));
            }
            if (actionTitle != null) {
                actionList.add(actionTitle);
            }

            return actionList.stream().filter(action -> action.value().equals(title)).findFirst().isPresent();
        }

        /**
         * Check whether given field is a child of specified class
         *
         * @param parent class that is supposed to be parent
         * @param field  field to check
         * @return true|false
         */
        private static boolean isChildOf(Class<?> parent, Field field) {
            Class<?> fieldType = field.getType();
            while (fieldType != null && fieldType != Object.class) {
                if (fieldType == parent) {
                    return true;
                }
                fieldType = fieldType.getSuperclass();
            }

            return false;
        }

        /**
         * Finds blocks by required path/name in the given context. Block is a class
         * that extends HtmlElement. If blockPath contains delimiters, it will be
         * treated as a full path, and block should be located by the exactly that
         * path. Otherwise, recursive search via all blocks is being performed
         *
         * @param blockPath        full path or just a name of the block to search
         * @param context          object where the search will be performed
         * @param returnFirstFound whether the search should be stopped on a first
         *                         found block (for faster searches)
         * @return list of found blocks. could be empty
         * @throws IllegalAccessException if called with invalid context
         */
        private static List<HtmlElement> findBlocks(String blockPath, Object context, boolean returnFirstFound)
                throws IllegalAccessException {
            String[] blockChain;
            if (blockPath.contains("->")) {
                blockChain = blockPath.split("->");
            } else {
                blockChain = new String[]{blockPath};
            }
            List<HtmlElement> found = new ArrayList<>();
            for (Field currentField : FieldUtilsExt.getDeclaredFieldsWithInheritance(context.getClass())) {
                if (Core.isBlockElement(currentField)) {
                    if (Core.isRequiredElement(currentField, blockChain[0])) {
                        currentField.setAccessible(true);
                        // isBlockElement() ensures that this is a HtmlElement instance
                        HtmlElement foundBlock = (HtmlElement) currentField.get(context);
                        if (blockChain.length == 1) {
                            // Found required block directly inside the context
                            found.add(foundBlock);
                            if (returnFirstFound) {
                                return found;
                            }
                        } else {
                            // Continue to search in the element chain, reducing its length by the first found element
                            // +2 because '->' adds 2 symbols
                            String reducedPath = blockPath.substring(blockChain[0].length() + 2);
                            found.addAll(findBlocks(reducedPath, foundBlock, returnFirstFound));
                        }
                    } else if (blockChain.length == 1) {
                        found.addAll(findBlocks(blockPath, currentField.get(context), returnFirstFound));
                    }
                }
            }
            return found;
        }

        /**
         * Find list of elements of the specified type with required title in the
         * given context. Context is either a page object itself, or a block on the
         * page. !BEWARE! field.get() will actually query browser to evaluate the
         * list, so this method might reduce performance!
         *
         * @param listTitle value of ElementTitle annotation of required element
         * @param type      type of elements inside of the list
         * @param context   object where search should be performed
         * @param <T>       type of elements in returned list
         * @return list of WebElement's or its derivatives
         * @throws PageException if didn't find any list or current page wasn't initialized
         */
        @SuppressWarnings("unchecked")
        private static <T extends WebElement> List<T> findListOfElements(String listTitle, Class<T> type, Object context)
                throws PageException {
            for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(context.getClass())) {
                if (Core.isRequiredElement(field, listTitle) && List.class.isAssignableFrom(field.getType())
                        && field.getGenericType() instanceof ParameterizedType
                        && ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].equals(type)) {
                    field.setAccessible(true);
                    try {
                        return (List<T>) field.get(context);
                    } catch (IllegalAccessException e) {
                        throw new InternalError(format("Internal error during attempt to find list '%s'", listTitle), e);
                    }
                }
            }
            throw new ElementNotFoundException(format("Couldn't find elements list '%s' on page '%s'", listTitle, PageFactory.getInstance().getCurrentPage().getTitle()));
        }

        /**
         * Find element with required title and type inside of the given block. Return null if didn't find any
         *
         * @param block        block object
         * @param elementTitle value of ElementTitle annotation of required element
         * @param type         type of element to return
         * @param <T>          any WebElement or its derivative
         * @return found element or null (exception should be thrown by a caller
         * that could no find any elements)
         */
        private static <T extends WebElement> T findElementInBlock(HtmlElement block, String elementTitle, Class<T> type)
                throws InternalError {
            for (Field f : FieldUtils.getAllFields(block.getClass())) {
                if (Core.isRequiredElement(f, elementTitle) && f.getType().equals(type)) {
                    f.setAccessible(true);
                    try {
                        return type.cast(f.get(block));
                    } catch (IllegalAccessException e) {
                        // Since we explicitly set the field to be accessible, this exception is unexpected.
                        // It might mean that we try to get field in context of an object it doesn't belong to
                        throw new InternalError(format("Internal error during attempt to find element '%s' in block '%s'",
                                elementTitle, block.getName()), e);
                    }
                }
            }
            return null;
        }

        /**
         * Check if corresponding field is a block. I.e. it has {@link ElementTitle}
         * annotation and extends {@link HtmlElement} class directly
         *
         * @param field field that is being checked
         * @return true|false
         */
        private static boolean isBlockElement(Field field) {
            return field.getAnnotationsByType(ElementTitle.class).length > 0
                    && Core.isChildOf(HtmlElement.class, field);
        }

        /**
         * Check whether {@link ElementTitle} annotation of the field has a required
         * value
         *
         * @param field field to check
         * @param title value of ElementTitle annotation of required element
         * @return true|false
         */
        private static boolean isRequiredElement(Field field, String title) {
            return getFieldTitle(field).equals(title);
        }

        /**
         * Return value of {@link ElementTitle} annotation for the field.
         * If none present, return empty string
         *
         * @param field field to check
         * @return either an element title, or an empty string
         */
        private static String getFieldTitle(Field field) {
            for (Annotation a : field.getAnnotations()) {
                if (a instanceof ElementTitle) {
                    return ((ElementTitle) a).value();
                }
            }
            return "";
        }

        private static Class<? extends Page> findRedirect(Class<?> parentClass, Object element, Object parent) {
            List<Field> fields = FieldUtilsExt.getDeclaredFieldsWithInheritance(parentClass);

            for (Field field : fields) {
                RedirectsTo redirect = field.getAnnotation(RedirectsTo.class);
                if (redirect != null) {
                    try {
                        field.setAccessible(true);
                        Object targetField = field.get(parent);
                        if (targetField != null) {
                            if (targetField.equals(element)) {
                                return redirect.page();
                            }
                        }
                    } catch (NoSuchElementException | StaleElementReferenceException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                        log.debug("Failed to get page destination to redirect for element", ex);
                    }
                }
                if (Core.isChildOf(HtmlElement.class, field)) {
                    field.setAccessible(true);
                    Class<? extends Page> redirects = null;
                    try {
                        redirects = findRedirect(field.getType(), element, field.get(parent));
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        log.debug("Failed to get page destination to redirect for html element", ex);
                    }
                    if (redirects != null) {
                        return redirects;
                    }
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private static <T> T getElementByField(Object object, Field field) throws ElementNotFoundException {
            field.setAccessible(true);
            Object element;
            try {
                element = field.get(object);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new NoSuchElementException("Represented field '" + field + "' is inaccessible or specified "
                        + "object is not an instance of the class or interface declaring the underlying field", e);
            }
            return (T) element;
        }

        private static void addToReport(WebElement webElement, String text) {
            try {
                String elementTitle = PageFactory.getInstance().getCurrentPage().getElementTitle(webElement);
                addToReport(elementTitle, text);
            } catch (PageInitializationException | ElementDescriptionException e) {
                log.warn("Failed to add element " + webElement + " to report", e);
            }
        }

        private static void addToReport(String header, String explanation) {
            ParamsHelper.addParam(header, explanation);
            log.debug("Add '" + header + explanation + "' to report for page '" + header + "'");
        }
    }

    /**
     * Get a title of the page object
     *
     * @return the title
     */
    public String getTitle() {
        return this.getClass().getAnnotation(PageEntry.class).title();
    }

    /**
     * Get element title by reflection. Inspecting class of the object to get title annotation of
     * this object
     *
     * @param <T>     TODO
     * @param element TODO
     * @return a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    public <T extends Object> String getElementTitle(T element) throws ElementDescriptionException {
        Page currentPage = null;
        try {
            currentPage = PageFactory.getInstance().getCurrentPage();
        } catch (PageInitializationException ex) {
            throw new ElementDescriptionException("Failed to get current page", ex);
        }
        if (null == currentPage) {
            log.warn("Current page not initialized yet. You must initialize it by hands at first time only.");
            return null;
        }
        Map<Field, String> fields = PageFactory.getPageRepository().get(currentPage.getClass());

        Set<Map.Entry<Field, String>> entrySet = fields.entrySet();
        for (Map.Entry<Field, String> entry : entrySet) {
            try {
                WebElement webElementByField = Core.getElementByField(currentPage, entry.getKey());
                if (webElementByField == element) {
                    return entry.getValue();
                }
            } catch (java.util.NoSuchElementException | StaleElementReferenceException | NullPointerException | ElementNotFoundException ex) {
                log.debug("Failed to get element '" + element + "' title", ex);
            }
        }
        return null;
    }

//    /**
//     *
//     * @param fieldName TODO
//     * @return TODO
//     */
//    public String getElementTitle(String fieldName) {
//        return PageFactory.getPageRepository().get(this.getClass()).get(fieldName);
//    }

    /**
     * Return class for redirect if annotation contains and null if not present
     *
     * @param <T>               TODO
     * @param <TypifiedElement> TODO
     * @param element           TODO
     * @return TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    public <T extends WebElement, TypifiedElement> Class<? extends Page> getElementRedirect(T element) throws ElementDescriptionException {
        try {
            Page currentPage = PageFactory.getInstance().getCurrentPage();
            if (null == currentPage) {
                log.warn("Current page not initialized yet. You must initialize it by hands at first time only.");
                return null;
            }
            return Core.findRedirect(currentPage.getClass(), element, currentPage);
        } catch (IllegalArgumentException | PageInitializationException ex) {
            throw new ElementDescriptionException("Failed to get element redirect", ex);
        }
    }

    /**
     * @param title a {@link java.lang.String} object.
     * @return a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if failed to find corresponding element
     */
    public WebElement getElementByTitle(String title) throws ElementNotFoundException {
        for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(this.getClass())) {
            if (Core.isRequiredElement(field, title)) {
                return Core.getElementByField(this, field);
            }
        }
        throw new ElementNotFoundException(format("Element '%s' is not present on current page '%s''", title, this.getTitle()));
    }

    /**
     * @param <T>   TODO
     * @param title a {@link java.lang.String} object.
     * @return a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException TODO
     */
    @SuppressWarnings(value = "unchecked")
    public <T extends TypifiedElement> T getTypifiedElementByTitle(String title) throws ElementNotFoundException {
        for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(this.getClass())) {
            if (Core.isRequiredElement(field, title) && Core.isChildOf(TypifiedElement.class, field)) {
                return (T) Core.getElementByField(this, field);
            }
        }
        throw new ElementNotFoundException(format("Element '%s' is not present on current page '%s''", title, this.getTitle()));
    }

    /**
     * Execute method by MethodTitle
     *
     * @param <T>   TODO
     * @param title MethodTitle name
     * @param type  type return value
     * @param param TODO
     * @return TODO
     * @throws java.lang.NoSuchMethodException TODO
     */
    public <T extends Object> T executeMethodByTitle(String title, Class<T> type, Object... param) throws NoSuchMethodException {
        List<Method> methods = Core.getDeclaredMethods(this.getClass());
        for (Method method : methods) {
            if (Core.isRequiredAction(method, title)) {
                try {
                    method.setAccessible(true);
                    return type.cast(MethodUtils.invokeMethod(this, method.getName(), param));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    log.error("Failed to invoke method '" + title + "'", ex);
                }
            }
        }

        throw new NoSuchMethodException(
                "There is no " + title + " method on " + this.getTitle() + " page object.");
    }

    /**
     * Execute method by MethodTitle
     *
     * @param <T>   TODO
     * @param title MethodTitle name
     * @param param TODO
     * @return TODO
     * @throws java.lang.NoSuchMethodException TODO
     */
    public <T extends Object> T executeMethodByTitle(String title, Object... param) throws NoSuchMethodException {
        return executeMethodByTitle(title, (Class<T>) this.getClass(), param);
    }

    /**
     * @param title  a {@link java.lang.String} object.
     * @param params a {@link java.lang.Object} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    //TODO refactor throws throwable
    public void fireValidationRule(String title, Object... params) throws PageException {
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (null != method.getAnnotation(ValidationRule.class)
                    && method.getAnnotation(ValidationRule.class).title().equals(title)) {
                try {
                    method.invoke(this, params);
                } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
                    log.debug("Failed to invoke method {}", method, e);
                    throw new WebDriverException("Failed to invoke method", e);
                }
                return;
            }
        }
        throw new PageInitializationException("There is no '" + title + "' validation rule in '" + this.getTitle() + "' page.");
    }
}
