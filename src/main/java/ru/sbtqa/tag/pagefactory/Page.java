package ru.sbtqa.tag.pagefactory;

import java.io.File;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
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
import ru.sbtqa.tag.pagefactory.drivers.TagMobileDriver;
import ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException;
import ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.pagefactory.exceptions.WaitException;
import ru.sbtqa.tag.pagefactory.extensions.DriverExtension;
import ru.sbtqa.tag.pagefactory.extensions.WebExtension;
import ru.sbtqa.tag.pagefactory.support.AdbConsole;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.qautils.i18n.I18N;
import ru.sbtqa.tag.qautils.i18n.I18NRuntimeException;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.sbtqa.tag.qautils.strategies.MatchStrategy;
import ru.yandex.qatools.htmlelements.annotations.Name;
import ru.yandex.qatools.htmlelements.element.CheckBox;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

/**
 * Base page object class. Contains basic actions with elements, search methods
 */
public abstract class Page {

    private static final Logger LOG = LoggerFactory.getLogger(Page.class);

    private static boolean isUsedBlock = false;
    private static WebElement usedBlock = null;

    /**
     * Find element with specified title annotation, and fill it with given text
     * Add elementTitle-&gt;text as parameter-&gt;value to corresponding step in
     * allure report
     *
     * @param elementTitle element to fill
     * @param text text to enter
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if page was not
     * initialized, or required element couldn't be found
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.fill.field")
    public void fillField(String elementTitle, String text) throws PageException {
        WebElement webElement = getElementByTitle(elementTitle);
        webElement.click();

        if (PageFactory.getEnvironment() == Environment.WEB) {
            webElement.clear();
        }

        if (PageFactory.getEnvironment() == Environment.MOBILE && TagMobileDriver.getAppiumClickAdb()) {
            // set ADBKeyBoard as default
            AdbConsole.execute("ime set com.android.adbkeyboard/.AdbIME");
            // send broadcast intent via adb
            AdbConsole.execute(String.format("am broadcast -a ADB_INPUT_TEXT --es msg '%s'", text));
        } else {
            webElement.sendKeys(text);
        }
        ParamsHelper.addParam("\"%s\" is filled with text \"%s\"", new String[]{elementTitle, text});
    }

    /**
     * Same as {@link #fillField(String, String)}, but accepts particular
     * WebElement object and interacts with it
     *
     * @param webElement an object to interact with
     * @param text string text to send to element
     */
    public void fillField(WebElement webElement, String text) {
        if (null != text) {
            try {
                webElement.clear();
            } catch (InvalidElementStateException | NullPointerException e) {
                LOG.debug("Failed to clear web element {}", webElement, e);
            }
            webElement.sendKeys(text);
        }
        ParamsHelper.addParam("\"%s\" is filled with text \"%s\"", new String[]{getElementTitle(webElement), text});
    }

    /**
     * Click the specified link element
     *
     * @param webElement a WebElement object to click
     */
    public void clickWebElement(WebElement webElement) {
        if (PageFactory.getEnvironment() == Environment.MOBILE && TagMobileDriver.getAppiumClickAdb()) {
            // get center point of element to tap on it
            int x = webElement.getLocation().getX() + webElement.getSize().getWidth() / 2;
            int y = webElement.getLocation().getY() + webElement.getSize().getHeight() / 2;
            AdbConsole.execute(String.format("input tap %s %s", x, y));
        } else {
            webElement.click();
        }
        ParamsHelper.addParam("\"%s\" is clicked", new String[]{getElementTitle(webElement)});
    }

    /**
     * Find specified WebElement on a page, and click it Add corresponding
     * parameter to allure report
     *
     * @param elementTitle title of the element to click
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if page was not
     * initialized, or required element couldn't be found
     */
    @ActionTitles({
            @ActionTitle("ru.sbtqa.tag.pagefactory.click.link"),
            @ActionTitle("ru.sbtqa.tag.pagefactory.click.button")})
    public void clickElementByTitle(String elementTitle) throws PageException {
        WebElement webElement;
        try {
            webElement = getElementByTitle(elementTitle);
            DriverExtension.waitForElementGetEnabled(webElement, PageFactory.getTimeOut());
        } catch (NoSuchElementException | WaitException | ElementNotFoundException e) {
            LOG.warn("Failed to find element by title {}", elementTitle, e);
            webElement = DriverExtension.waitUntilElementAppearsInDom(By.partialLinkText(elementTitle));
        }
        clickWebElement(webElement);
    }

    /**
     * Press specified key on a keyboard Add corresponding parameter to allure
     * report
     *
     * @param keyName name of the key. See available key names in {@link Keys}
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.press.key")
    public void pressKey(String keyName) {
        Keys key = Keys.valueOf(keyName.toUpperCase());
        Actions actions = PageFactory.getActions();
        actions.sendKeys(key).perform();
        ParamsHelper.addParam("\"%s\" is pressed", new String[]{keyName});
    }

    /**
     * Focus a WebElement, and send specified key into it Add corresponding
     * parameter to allure report
     *
     * @param keyName name of the key. See available key names in {@link Keys}
     * @param elementTitle title of WebElement that accepts key commands
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if
     * couldn't find element with required title
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.press.key")
    public void pressKey(String keyName, String elementTitle) throws PageException {
        Keys key = Keys.valueOf(keyName.toUpperCase());
        Actions actions = PageFactory.getActions();
        actions.moveToElement(getElementByTitle(elementTitle));
        actions.click();
        actions.sendKeys(key);
        actions.build().perform();
        ParamsHelper.addParam("\"%s\" is pressed by key \"%s\"", new String[]{elementTitle, keyName});
    }

    /**
     * Send key to element and add corresponding parameter to allure report
     *
     * @param webElement WebElement to send keys in
     * @param keyName name of the key. See available key names in {@link Keys}
     */
    public void pressKey(WebElement webElement, Keys keyName) {
        webElement.sendKeys(keyName);
        ParamsHelper.addParam("\"%s\" is pressed by key \"%s\"", new String[]{getElementTitle(webElement), keyName.toString()});
    }

    /**
     * Find web element with corresponding title, if it is a check box, select
     * it If it's a WebElement instance, check whether it is already selected,
     * and click if it's not Add corresponding parameter to allure report
     *
     * @param elementTitle WebElement that is supposed to represent checkbox
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if page was not
     * initialized, or required element couldn't be found
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.select.checkBox")
    public void setCheckBox(String elementTitle) throws PageException {
        WebElement targetElement = getElementByTitle(elementTitle);
        if (targetElement.getClass().isAssignableFrom(CheckBox.class)) {
            ((CheckBox) targetElement).select();
        } else {
            setCheckBoxState(targetElement, true);
        }
        ParamsHelper.addParam("'%s is checked'", new String[]{elementTitle});
    }

    /**
     * Check whether specified element is selected, if it isn't, click it
     * isSelected() doesn't guarantee correct behavior if given element is not a
     * selectable (checkbox,dropdown,radiobtn)
     *
     * @param webElement a WebElemet object.
     * @param state a boolean object.
     */
    public void setCheckBoxState(WebElement webElement, Boolean state) {
        if (null != state) {
            if (webElement.isSelected() != state) {
                webElement.click();
            }
            ParamsHelper.addParam("\"%s\" turned to \"%s\" state", new String[]{getElementTitle(webElement), state.toString()});
        }
    }

    /**
     * Find element with required title, perform
     * {@link #select(WebElement, String, MatchStrategy)} on found element Use
     * exact match strategy
     *
     * @param elementTitle WebElement that is supposed to be selectable
     * @param option option to select
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if required
     * element couldn't be found, or current page isn't initialized
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.select")
    public void select(String elementTitle, String option) throws PageException {
        WebElement webElement = getElementByTitle(elementTitle);
        if (null != option) {
            select(webElement, option, MatchStrategy.EXACT);
        }
    }

    /**
     * Find element with required title, perform
     * {@link #select(WebElement, String, MatchStrategy)} on found element Use
     * given match strategy
     *
     * @param elementTitle the title of SELECT element to interact
     * @param option the value to match against
     * @param strategy the strategy to match value
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if required
     * element couldn't be found, or current page isn't initialized
     */
    public void select(String elementTitle, String option, MatchStrategy strategy) throws PageException {
        WebElement webElement = getElementByTitle(elementTitle);
        select(webElement, option, strategy);
    }

    /**
     * Try to extract selectable options form given WebElement, and select
     * required one Add corresponding parameter to allure report
     *
     * @param webElement WebElement for interaction. Element is supposed to be
     * selectable, i.e. have select options
     * @param option the value to match against
     * @param strategy the strategy to match value. See {@link MatchStrategy}
     * for available values
     */
    @SuppressWarnings("unchecked")
    public void select(WebElement webElement, String option, MatchStrategy strategy) {
        String jsString = ""
                + "var content=[]; "
                + "var options = arguments[0].getElementsByTagName('option'); "
                + " for (var i=0; i<options.length;i++){"
                + " content.push(options[i].text)"
                + "}"
                + "return content";
        List<String> options = (ArrayList<String>) ((JavascriptExecutor) PageFactory.getDriver()).
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
            throw new AutotestError("There is no element '" + option + "' in " + getElementTitle(webElement));
        }
        ParamsHelper.addParam("In the select \"%s\" is selected option \"%s\"", new String[]{getElementTitle(webElement), option});
    }

    /**
     * Wait for an alert with specified text, and accept it
     *
     * @param text alert message
     * @throws WaitException in case if alert didn't appear during default wait
     * timeout
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.accept.alert")
    public void acceptAlert(String text) throws WaitException {
        DriverExtension.interactWithAlert(text, true);
    }

    /**
     * Wait for an alert with specified text, and dismiss it
     *
     * @param text alert message
     * @throws WaitException in case if alert didn't appear during default wait
     * timeout
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.dismiss.alert")
    public void dismissAlert(String text) throws WaitException {
        DriverExtension.interactWithAlert(text, false);
    }

    /**
     * Wait for appearance of the required text in current DOM model. Text will
     * be space-trimmed, so only non-space characters will matter.
     *
     * @param text text to search
     * @throws WaitException if text didn't appear on the page during the
     * timeout
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.text.appears.on.page")
    public void assertTextAppears(String text) throws WaitException {
        WebExtension.waitForTextPresenceInPageSource(text, true);
    }

    /**
     * Check whether specified text is absent on the page. Text is being
     * space-trimmed before assertion, so only non-space characters will matter
     *
     * @param text text to search for
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.text.absent.on.page")
    public void assertTextIsNotPresent(String text) {
        WebExtension.waitForTextPresenceInPageSource(text, false);
    }

    /**
     * Wait for a new browser window, then wait for a specific text inside the
     * appeared window List of previously opened windows is being saved before
     * each click, so if modal window appears without click, this method won't
     * catch it. Text is being waited by {@link #assertTextAppears}, so it will
     * be space-trimmed as well
     *
     * @param text text that will be searched inside of the window
     * @throws ru.sbtqa.tag.pagefactory.exceptions.WaitException if
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.modal.window.with.text.appears")
    public void assertModalWindowAppears(String text) throws WaitException {
        try {
            String popupHandle = WebExtension.findNewWindowHandle((Set<String>) Stash.getValue("beforeClickHandles"));
            if (null != popupHandle && !popupHandle.isEmpty()) {
                PageFactory.getWebDriver().switchTo().window(popupHandle);
            }
            assertTextAppears(text);
        } catch (Exception ex) {
            throw new WaitException("Modal window with text '" + text + "' didn't appear during timeout", ex);
        }
    }

    /**
     * Perform {@link #checkValue(String, WebElement, MatchStrategy)} for the
     * WebElement with corresponding title on a current page. Use exact match
     * strategy
     *
     * @param text string value that will be searched inside of the element
     * @param elementTitle title of the element to search
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if
     * couldn't find element by given title, or current page isn't initialized
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.check.value")
    public void checkValue(String elementTitle, String text) throws PageException {
        checkValue(text, getElementByTitle(elementTitle), MatchStrategy.EXACT);
    }

    /**
     * Action for upload file.
     *
     * @param filePath path to file
     * @param elementTitle field name, usually 'input' type
     * @throws PageException if file cant be uploaded
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.upload.file")
    public void uploadFile(String filePath, String elementTitle) throws PageException {
        if (Props.get("webdriver.upload.dir") != null && Props.get("webdriver.upload.dir") != "") {
            filePath = Props.get("webdriver.upload.dir") + filePath;
        }

        File file = new File(filePath);
        WebElement element = getElementByTitle(elementTitle);
        element.sendKeys(file.getAbsolutePath());
    }

    /**
     * Perform {@link #checkValue(String, WebElement, MatchStrategy)} for the
     * specified WebElement. Use exact match strategy
     *
     * @param text string value that will be searched inside of the element
     * @param webElement WebElement to check
     */
    public void checkValue(String text, WebElement webElement) {
        checkValue(text, webElement, MatchStrategy.EXACT);
    }

    /**
     * Define a type of given WebElement, and check whether it either contains,
     * or exactly matches given text in its value. Currently supported elements
     * are text input and select box TODO: use HtmlElements here, to define
     * which element we are dealing with
     *
     * @param text string value that will be searched inside of the element
     * @param webElement WebElement to check
     * @param searchStrategy match strategy. See available strategies in
     * {@link MatchStrategy}
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
     * Find element by given title, and check whether it is not empty See
     * {@link #checkFieldIsNotEmpty(WebElement)} for details
     *
     * @param elementTitle title of the element to check
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if current page
     * was not initialized, or element wasn't found on the page
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.check.field.not.empty")
    public void checkFieldIsNotEmpty(String elementTitle) throws PageException {
        WebElement webElement = getElementByTitle(elementTitle);
        checkFieldIsNotEmpty(webElement);
    }

    /**
     * Check that given WebElement has a value attribute, and it is not empty
     *
     * @param webElement WebElement to check
     */
    public void checkFieldIsNotEmpty(WebElement webElement) {
        String value = webElement.getText();
        if (value.isEmpty()) {
            value = webElement.getAttribute("value");
        }
        try {
            Assert.assertFalse(value.replaceAll("\\s+", "").isEmpty());
        } catch (Exception | AssertionError e) {
            throw new AutotestError("The field" + getElementTitle(webElement) + " is empty", e);
        }
    }

    /**
     * Find element with corresponding title, and make sure that its value is
     * not equal to given text Text, as well as element value are being
     * space-trimmed before comparison, so only non-space characters matter
     *
     * @param text element value for comparison
     * @param elementTitle title of the element to search
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if current page
     * wasn't initialized, or element with required title was not found
     */
    @ActionTitle("ru.sbtqa.tag.pagefactory.check.values.not.equal")
    public void checkValuesAreNotEqual(String text, String elementTitle) throws PageException {
        WebElement webElement = this.getElementByTitle(elementTitle);
        if (checkValuesAreNotEqual(text, webElement)) {
            throw new AutotestError("'" + text + "' is equal with '" + elementTitle + "' value");
        }
    }

    /**
     * Extract value from the given WebElement, and compare the it with the
     * given text Text, as well as element value are being space-trimmed before
     * comparison, so only non-space characters matter
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
     * Perform a check that there is an element with required text on current
     * page
     *
     * @param text a {@link java.lang.String} object.
     */
    @ActionTitles({
            @ActionTitle("ru.sbtqa.tag.pagefactory.check.element.with.text.present"),
            @ActionTitle("ru.sbtqa.tag.pagefactory.check.text.visible")})
    public void checkElementWithTextIsPresent(String text) {
        if (!DriverExtension.checkElementWithTextIsPresent(text, PageFactory.getTimeOutInSeconds())) {
            throw new AutotestError("Text '" + text + "' is not present");
        }
    }

    /**
     * Find element of required type in the specified block, or block chain on
     * the page. If blockPath is separated by &gt; delimiters, it will be
     * treated as a block path, so element could be found only in the described
     * chain of blocks. Otherwise, given block will be searched recursively on
     * the page
     *
     * @param <T> Any element
     * @param blockPath block or block chain where element will be searched
     * @param title value of ElementTitle annotation of required element
     * @param type type of the searched element
     * @return web element of the required type
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if
     * element was not found
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException if element was not found, but with the wrong type
     */
    public <T extends WebElement> T findElementInBlockByTitle(String blockPath, String title, Class<T> type)
            throws PageException {
        for (HtmlElement block : findBlocks(blockPath)) {
            T found = Core.findElementInBlock(block, title, type);
            if (null != found) {
                return found;
            }
        }
        throw new ElementNotFoundException(String.format("Couldn't find element '%s' in '%s'", title, blockPath));
    }

    /**
     * Acts exactly like
     * {@link #findElementInBlockByTitle(String, String, Class)}, but return a
     * WebElement instance. It still could be casted to HtmlElement and
     * TypifiedElement any class that extend them.
     *
     * @param blockPath block or block chain where element will be searched
     * @param title value of ElementTitle annotation of required element
     * @return WebElement
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if
     * element was not found
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException if element was not found, but with the wrong type
     */
    public WebElement findElementInBlockByTitle(String blockPath, String title) throws PageException {
        return findElementInBlockByTitle(blockPath, title, WebElement.class);
    }

    /**
     * Finds elements list in context of current page See
     * ${@link Core#findListOfElements(String, Class, Object)} for detailed
     * description
     *
     * @param listTitle value of ElementTitle annotation of required element
     * @param type type of elements in list that is being searched for
     * @param <T> type of elements in returned list
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
     * @param blockPath full path or just a name of the block to search
     * @param listTitle value of ElementTitle annotation of required element
     * @param type type of elements in list that is being searched for
     * @param <T> type of elements in returned list
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
     * @param blockPath full path or just a name of the block to search
     * @param listTitle value of ElementTitle annotation of required element
     * @return list of WebElement's
     * @throws PageException if nothing found or current page is not initialized
     */
    public List<WebElement> findListOfElementsInBlock(String blockPath, String listTitle) throws PageException {
        return findListOfElementsInBlock(blockPath, listTitle, WebElement.class);
    }

    /**
     * See {@link Core#findBlocks(String, Object, boolean)} for description.
     * This wrapper finds only one block. Search is being performed on a current
     * page
     *
     * @param blockPath full path or just a name of the block to search
     * @return first found block object
     * @throws java.util.NoSuchElementException if couldn't find any block
     */
    public HtmlElement findBlock(String blockPath) throws NoSuchElementException {
        try {
            List<HtmlElement> blocks = Core.findBlocks(blockPath, this, true);
            if (blocks.isEmpty()) {
                throw new java.util.NoSuchElementException(String.format("Couldn't find block '%s' on a page '%s'",
                        blockPath, this.getTitle()));
            }
            return blocks.get(0);
        } catch (IllegalAccessException ex) {
            throw new FactoryRuntimeException(String.format("Internal error during attempt to find block '%s'", blockPath), ex);
        }
    }

    /**
     * See {@link Core#findBlocks(String, Object, boolean)} for description.
     * Search is being performed on a current page
     *
     * @param blockPath full path or just a name of the block to search
     * @return list of objects that were found by specified path
     */
    public List<HtmlElement> findBlocks(String blockPath) throws NoSuchElementException {
        try {
            return Core.findBlocks(blockPath, this, false);
        } catch (IllegalAccessException ex) {
            throw new FactoryRuntimeException(String.format("Internal error during attempt to find a block '%s'", blockPath), ex);
        }
    }

    /**
     * Execute parameter-less method inside of the given block element.
     *
     * @param block block title, or a block chain string separated with '-&gt;'
     * symbols
     * @param actionTitle title of the action to execute
     * @throws java.lang.NoSuchMethodException if required method couldn't be
     * found
     */
    public void executeMethodByTitleInBlock(String block, String actionTitle) throws NoSuchMethodException {
        executeMethodByTitleInBlock(block, actionTitle, new Object[0]);
    }

    /**
     * Execute method with one or more parameters inside of the given block
     * element !BEWARE! If there are several elements found by specified block
     * path, a first one will be used!
     *
     * @param blockPath block title, or a block chain string separated with
     * '-&gt;' symbols
     * @param actionTitle title of the action to execute
     * @param parameters parameters that will be passed to method
     * @throws java.lang.NoSuchMethodException if required method couldn't be
     * found
     */
    public void executeMethodByTitleInBlock(String blockPath, String actionTitle, Object... parameters) throws NoSuchMethodException {
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
                    return;
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new FactoryRuntimeException(String.format("Failed to execute method '%s' in the following block: '%s'",
                            actionTitle, blockPath), e);
                }
            }
        }

        isUsedBlock = true;
        usedBlock = block;
        List<Method> methodList = Core.getDeclaredMethods(this.getClass());
        for (Method method : methodList) {
            if (Core.isRequiredAction(method, actionTitle)) {
                try {
                    method.setAccessible(true);
                    MethodUtils.invokeMethod(this, method.getName(), parameters);
                    return;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new FactoryRuntimeException(String.format("Failed to execute method '%s' in the following block: '%s'",
                            actionTitle, blockPath), e);
                }
            }
        }

        throw new NoSuchMethodException(String.format("There is no '%s' method in block '%s'", actionTitle, blockPath));
    }

    /**
     * Get title of current page obect
     *
     * @return the title
     */
    public String getTitle() {
        return this.getClass().getAnnotation(PageEntry.class).title();
    }

    /**
     * Search for the given WebElement in page repository storage, that is being
     * generated during preconditions to all tests. If element is found, return
     * its title annotation. If nothing found, log debug message and return
     * toString() of corresponding element
     *
     * @param element WebElement to search
     * @return title of the given element
     */
    public String getElementTitle(WebElement element) {
        for (Map.Entry<Field, String> entry : PageFactory.getPageRepository().get(this.getClass()).entrySet()) {
            try {
                if (Core.getElementByField(this, entry.getKey()) == element) {
                    ElementTitle elementTitle = entry.getKey().getAnnotation(ElementTitle.class);
                    if (elementTitle != null && !elementTitle.value().isEmpty()) {
                        return elementTitle.value();
                    }
                    return entry.getValue();
                }
            } catch (NoSuchElementException | StaleElementReferenceException | ElementDescriptionException ex) {
                LOG.debug("Failed to get element '" + element + "' title", ex);
            }
        }
        return element.toString();
    }

    /**
     * Return class for redirect if annotation contains and null if not present
     *
     * @param element element, redirect for which is being searched
     * @return class of the page object, element redirects to
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException if failed to find redirect
     */
    public Class<? extends Page> getElementRedirect(WebElement element) throws ElementDescriptionException {
        try {
            Page currentPage = PageFactory.getInstance().getCurrentPage();
            if (null == currentPage) {
                LOG.warn("Current page not initialized yet. You must initialize it by hands at first time only.");
                return null;
            }
            return Core.findRedirect(currentPage, element);
        } catch (IllegalArgumentException | PageInitializationException ex) {
            throw new ElementDescriptionException("Failed to get element redirect", ex);
        }
    }

    /**
     * Find specified WebElement by title annotation among current page fields
     *
     * @param title title of the element to search
     * @return WebElement found by corresponding title
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if failed to
     * find corresponding element or element type is set incorrectly
     */
    public WebElement getElementByTitle(String title) throws PageException {
        if (!isUsedBlock) {
            for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(this.getClass())) {
                if (Core.isRequiredElement(field, title)) {
                    return Core.getElementByField(this, field);
                }
            }
        } else {
            for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(usedBlock)) {
                if (Core.isRequiredElementInBlock(field, title)) {
                    return Core.getElementByField(usedBlock, field);
                }
            }
        }

        throw new ElementNotFoundException(String.format("Element \"%s\" is not present on current page \"%s\"'", title, this.getTitle()));
    }

    /**
     * Find specified element with given type and title annotation among current
     * page fields fields
     *
     * @param <T> type of the element
     * @param title a {@link java.lang.String} object.
     * @param type object under the field of given name
     * @return WebElement found by corresponding title
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if failed to
     * find corresponding element or element type is set incorrectly
     */
    public <T extends WebElement> T getElementByTitle(String title, Class<T> type) throws PageException {
        for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(this.getClass())) {
            if (Core.isRequiredElement(field, title) && field.getType().equals(type)) {
                return Core.getElementByField(this, field);
            }
        }
        throw new ElementNotFoundException(String.format("Element \"%s\" is not present on current page \"%s\"'", title, this.getTitle()));
    }

    /**
     * Find method with corresponding title on current page, and execute it
     *
     * @param title title of the method to call
     * @param param parameters that will be passed to method
     * @throws java.lang.NoSuchMethodException if required method couldn't be
     * found
     */
    public void executeMethodByTitle(String title, Object... param) throws NoSuchMethodException {
        List<Method> methods = Core.getDeclaredMethods(this.getClass());
        for (Method method : methods) {
            if (Core.isRequiredAction(method, title)) {
                try {
                    method.setAccessible(true);
                    MethodUtils.invokeMethod(this, method.getName(), param);
                    return;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new FactoryRuntimeException("Error while executing action '" + title + "' on " + method.getDeclaringClass().getSimpleName() + " . See the caused exception below", ExceptionUtils.getRootCause(e));
                }
            }
        }

        throw new NoSuchMethodException("There is no '" + title + "' method on '" + this.getTitle() + "' page object");
    }

    /**
     * Find a method with {@link ValidationRule} annotation on current page, and
     * call it
     *
     * @param title title of the validation rule
     * @param params parameters passed to called method
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException if couldn't
     * find corresponding validation rule
     */
    public void fireValidationRule(String title, Object... params) throws PageException {
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (null != method.getAnnotation(ValidationRule.class)
                    && method.getAnnotation(ValidationRule.class).title().equals(title)) {
                try {
                    method.invoke(this, params);
                } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
                    LOG.debug("Failed to invoke method {}", method, e);
                    throw new FactoryRuntimeException("Failed to invoke method", e);
                }
                return;
            }
        }
        throw new PageException("There is no '" + title + "' validation rule in '" + this.getTitle() + "' page.");
    }

    /**
     * Helper methods for manipulations on fields and objects
     */
    private static class Core {

        /**
         * Return a list of methods declared tin the given class and its super
         * classes
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
         * Check whether given method has {@link ActionTitle} or
         * {@link ActionTitles} annotation with required title
         *
         * @param method method to check
         * @param title required title
         * @return true|false
         */
        private static Boolean isRequiredAction(Method method, final String title) {
            ActionTitle actionTitle = method.getAnnotation(ActionTitle.class);
            ActionTitles actionTitles = method.getAnnotation(ActionTitles.class);
            List<ActionTitle> actionList = new ArrayList<>();

            if (actionTitles != null) {
                actionList.addAll(Arrays.asList(actionTitles.value()));
            }
            if (actionTitle != null) {
                actionList.add(actionTitle);
            }

            I18N i18n = null;
            try {
                i18n = I18N.getI18n(method.getDeclaringClass(), ScenarioContext.getScenario());
            } catch (I18NRuntimeException e) {
                LOG.debug("There is no bundle for translation class. Leave it as is", e);
            }

            for (ActionTitle action : actionList) {
                String actionValue;
                actionValue = (i18n != null) ? i18n.get(action.value()) : action.value();

                if (actionValue.equals(title)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Check whether given field is a child of specified class
         *
         * @param parent class that is supposed to be parent
         * @param field field to check
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
         * Finds blocks by required path/name in the given context. Block is a
         * class that extends HtmlElement. If blockPath contains delimiters, it
         * will be treated as a full path, and block should be located by the
         * exactly that path. Otherwise, recursive search via all blocks is
         * being performed
         *
         * @param blockPath full path or just a name of the block to search
         * @param context object where the search will be performed
         * @param returnFirstFound whether the search should be stopped on a
         * first found block (for faster searches)
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
                    currentField.setAccessible(true);

                    if (Core.isRequiredElement(currentField, blockChain[0])) {
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
         * Find list of elements of the specified type with required title in
         * the given context. Context is either a page object itself, or a block
         * on the page. !BEWARE! field.get() will actually query browser to
         * evaluate the list, so this method might reduce performance!
         *
         * @param listTitle value of ElementTitle annotation of required element
         * @param type type of elements inside of the list
         * @param context object where search should be performed
         * @param <T> type of elements in returned list
         * @return list of WebElement's or its derivatives
         * @throws PageException if didn't find any list or current page wasn't
         * initialized
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
                        throw new FactoryRuntimeException(
                                String.format("Internal error during attempt to find list '%s'", listTitle), e);
                    }
                }
            }
            throw new ElementNotFoundException(String.format("Couldn't find elements list '%s' on page '%s'", listTitle, PageFactory.getInstance().getCurrentPageTitle()));
        }

        /**
         * Find element with required title and type inside of the given block.
         * Return null if didn't find any
         *
         * @param block block object
         * @param elementTitle value of ElementTitle annotation of required
         * element
         * @param type type of element to return
         * @param <T> any WebElement or its derivative
         * @return found element or null (exception should be thrown by a caller
         * that could no find any elements)
         */
        private static <T extends WebElement> T findElementInBlock(HtmlElement block, String elementTitle, Class<T> type)
                throws ElementDescriptionException {
            for (Field f : FieldUtils.getAllFields(block.getClass())) {
                if (Core.isRequiredElement(f, elementTitle) && type.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    try {
                        return type.cast(f.get(block));
                    } catch (IllegalAccessException iae) {
                        // Since we explicitly set the field to be accessible, this exception is unexpected.
                        // It might mean that we try to get field in context of an object it doesn't belong to
                        throw new FactoryRuntimeException(
                                String.format("Internal error during attempt to find element '%s' in block '%s'",
                                        elementTitle, block.getName()), iae);
                    } catch (ClassCastException cce) {
                        throw new ElementDescriptionException(
                                String.format("Element '%s' was found in block '%s', but it's type is incorrect."
                                                + "Requested '%s', but got '%s'",
                                        elementTitle, block.getName(), type.getName(), f.getType()), cce);
                    }
                }
            }
            return null;
        }

        /**
         * Check if corresponding field is a block. I.e. it has
         * {@link ElementTitle} annotation and extends {@link HtmlElement} class
         * directly
         *
         * @param field field that is being checked
         * @return true|false
         */
        private static boolean isBlockElement(Field field) {

            return (null != field.getAnnotation(ElementTitle.class))
                    && Core.isChildOf(HtmlElement.class, field);
        }

        /**
         * Check whether {@link ElementTitle} annotation of the field has a
         * required value
         *
         * @param field field to check
         * @param title value of ElementTitle annotation of required element
         * @return true|false
         */
        private static boolean isRequiredElement(Field field, String title) {
            return getFieldTitle(field).equals(title);
        }

        /**
         * Check whether {@link Name} annotation of the field has a
         * required value
         *
         * @param field field to check
         * @param title value of ElementTitle annotation of required element
         * @return true|false
         */
        private static boolean isRequiredElementInBlock(Field field, String title) {
            return getFieldTitleInBlock(field).equals(title);
        }

        /**
         * Return value of {@link ElementTitle} annotation for the field. If
         * none present, return empty string
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

        /**
         * Return value of {@link Name} or {@link ElementTitle} annotation for the field. If
         * none present, return empty string
         *
         * @param field field to check
         * @return either an element title, or an empty string
         */
        private static String getFieldTitleInBlock(Field field) {
            for (Annotation a : field.getAnnotations()) {
                if (a instanceof Name) {
                    return ((Name) a).value();
                }
                if (a instanceof ElementTitle) {
                    return ((ElementTitle) a).value();
                }
            }
            return "";
        }

        /**
         * Search for the given given element among the parent object fields,
         * check whether it has a {@link
         * RedirectsTo} annotation, and return a redirection page class, if so.
         * Search goes in recursion if it meets HtmlElement field, as given
         * element could be inside of the block
         *
         * @param element element that is being checked for redirection
         * @param parent parent object
         * @return class of the page, this element redirects to
         */
        private static Class<? extends Page> findRedirect(Object parent, Object element) {
            if (PageFactory.getPageRepository().get(parent.getClass()) == null) {
                return null;
            }

            for (Map.Entry<Field, String> entry : PageFactory.getPageRepository().get(parent.getClass()).entrySet()) {
                Field field = entry.getKey();
                RedirectsTo redirect = field.getAnnotation(RedirectsTo.class);

                if (redirect != null) {
                    try {
                        field.setAccessible(true);
                        Object targetField = field.get(parent);
                        if (targetField != null) {
                            if (targetField == element) {
                                return redirect.page();
                            }
                        }
                    } catch (NoSuchElementException | StaleElementReferenceException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                        LOG.debug("Failed to get page destination to redirect for element", ex);
                    }
                }
                if (Core.isChildOf(HtmlElement.class, field)) {
                    field.setAccessible(true);
                    Class<? extends Page> redirects = null;
                    try {
                        redirects = findRedirect(field.get(parent), element);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        LOG.debug("Failed to get page destination to redirect for html element", ex);
                    }
                    if (redirects != null) {
                        return redirects;
                    }
                }
            }
            return null;
        }

        /**
         * Get object from a field of specified parent
         *
         * @param parentObject object that contains(must contain) given field
         * @param field field to get
         * @param <T> supposed type of the field. if field cannot be cast into
         * this type, it will fail
         * @return element of requested type
         * @throws ElementDescriptionException in case if field does not belong
         * to the object, or element could not be cast to specified type
         */
        @SuppressWarnings("unchecked")
        private static <T> T getElementByField(Object parentObject, Field field) throws ElementDescriptionException {
            field.setAccessible(true);
            Object element;
            try {
                element = field.get(parentObject);
                isUsedBlock = false;
                usedBlock = null;
                return (T) element;
            } catch (IllegalArgumentException | IllegalAccessException iae) {
                throw new ElementDescriptionException("Specified parent object is not an instance of the class or "
                        + "interface, declaring the underlying field: '" + field + "'", iae);
            } catch (ClassCastException cce) {
                throw new ElementDescriptionException("Requested type is incompatible with field '" + field.getName()
                        + "' of '" + parentObject.getClass().getCanonicalName() + "'", cce);
            }
        }
    }
}
