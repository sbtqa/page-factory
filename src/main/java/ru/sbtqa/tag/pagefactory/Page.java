package ru.sbtqa.tag.pagefactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.openqa.selenium.Alert;
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
import ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException;
import ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.pagefactory.exceptions.WaitException;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.sbtqa.tag.qautils.strategies.MatchStrategy;
import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;

public abstract class Page {

    private static final Logger log = LoggerFactory.getLogger(Page.class);

    /**
     *
     * @param title TODO
     * @throws PageInitializationException TODO
     */
    @ActionTitle("отркывается страница")
    @ActionTitle("open page")
    public void initPage(String title) throws PageInitializationException {
        PageFactory.getPageFactory().getPage(title);
    }

    /**
     *
     * @param elementTitle a {@link java.lang.String} object.
     * @param text a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    @ActionTitle("заполняет поле")
    @ActionTitle("fill the field")
    public void fillField(String elementTitle, String text) throws PageException {
        WebElement webElement = PageFactory.getPageFactory().getCurrentPage().getElementByTitle(elementTitle);
        webElement.click();
        webElement.clear();
        webElement.sendKeys(text);
        Core.addToReport(elementTitle, text);
    }

    /**
     *
     * @param webElement an object to interact with
     * @param text string text to send to element
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
     *
     * @param elementTitle a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    @ActionTitle("нажимает кнопку")
    @ActionTitle("click on the button")
    public void clickButton(String elementTitle) throws PageException {
        clickLink(elementTitle);
    }

    /**
     *
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     */
    public void clickButton(WebElement webElement) {
        clickLink(webElement);
    }

    /**
     *
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     */
    public void clickLink(WebElement webElement) {
        webElement.click();
        Core.addToReport(webElement, " is clicked");
    }

    /**
     *
     * @param elementTitle a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    @ActionTitle("кликает по ссылке")
    @ActionTitle("click on the link")
    public void clickLink(String elementTitle) throws PageException {
        WebElement webElement;
        try {
            webElement = PageFactory.getPageFactory().getCurrentPage().getElementByTitle(elementTitle);
            DriverExtensions.waitForElementGetEnabled(webElement, PageFactory.getTimeOut());
        } catch (NoSuchElementException | WaitException e) {
            log.warn("Failed to find element by title {}", elementTitle, e);
            webElement = DriverExtensions.waitUntilElementAppearsInDom(By.partialLinkText(elementTitle));
        }
        clickLink(webElement);
    }

    /**
     *
     * @param keyName a {@link java.lang.String} object.
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
     *
     * @param keyName a {@link java.lang.String} object.
     * @param elementTitle a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException TODO
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
     *
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @param keyName a {@link org.openqa.selenium.Keys} object.
     */
    public void pressKey(WebElement webElement, Keys keyName) {
        webElement.sendKeys(keyName);
        Core.addToReport(webElement, " is pressed by key '" + keyName + "'");
    }

    /**
     *
     * @param elementTitle a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    @ActionTitle("отмечает признак")
    @ActionTitle("select CheckBox")
    public void selectCheckBox(String elementTitle) throws PageException {
        WebElement targetElement = PageFactory.getPageFactory().getCurrentPage().getElementByTitle(elementTitle);
        targetElement.click();
        Core.addToReport(elementTitle, " is checked");
    }

    /**
     * click checkbox according to states
     *
     * @param webElement a WebElemet object.
     * @param state a boolean object.
     */
    public void selectCheckBox(WebElement webElement, Boolean state) {
        if (null != state) {
            if (webElement.isSelected() ^ state) {
                webElement.click();
            }
        }
        Core.addToReport(webElement, " is turned to '" + state + "' state");
    }

    /**
     *
     * @param elementTitle TODO
     * @param option a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException TODO
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
     * Select an option that have matching the argument. By default matching strategy is exact.
     *
     * @param webElement SELECT element to interact
     * @param option the value to match against
     */
    public void select(WebElement webElement, String option) {
        select(webElement, option, MatchStrategy.EXACT);
    }

    /**
     * Select an option that have matching the argument in SELECT element specified by element
     * title.
     *
     * @param elementTitle the title of SELECT element to interact
     * @param option the value to match against
     * @param strategy the strategy to match value
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException if there are an error with
     * getting element by element title
     */
    public void select(String elementTitle, String option, MatchStrategy strategy) throws ElementNotFoundException {
        WebElement webElement = getElementByTitle(elementTitle);
        select(webElement, option, strategy);
    }

    /**
     * Select an option that have matching the argument.
     *
     * @param webElement SELECT element to interact
     * @param option the value to match against
     * @param strategy the strategy to match value
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
     *
     * @param elementTitle a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException TODO
     */
    @ActionTitle("отображается значение")
    @ActionTitle("value is visible")
    public String showValue(String elementTitle) throws ElementNotFoundException {
        return getElementByTitle(elementTitle).getText();
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     */
    @ActionTitle("появляется уведомление")
    @ActionTitle("notification appears")
    public void assertNotificationAppears(String text) {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds(), 1000).until(ExpectedConditions.alertIsPresent());
        Alert alert = PageFactory.getWebDriver().switchTo().alert();
        Assert.assertTrue(alert.getText().contains(text));
        alert.accept();
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    @ActionTitle("появляется сообщение")
    @ActionTitle("message appears")
    public void assertMessageAppears(String message) {
        assertMessageAppears(message, "");
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param comment a {@link java.lang.String} object.
     */
    @ActionTitle("появляется сообщение")
    @ActionTitle("message appears")
    public void assertMessageAppears(String message, String comment) {
        long timeOutTime = System.currentTimeMillis() + PageFactory.getTimeOut();
        Throwable exception = null;
        String body;
        while (timeOutTime > System.currentTimeMillis()) {
            try {
                Thread.sleep(1000);
                body = PageFactory.getWebDriver().findElement(By.tagName("body")).getText().replaceAll("\\s+", "");
                Assert.assertTrue(body.contains(message.replaceAll("\\s+", "")));
                return;
            } catch (Exception | AssertionError e) {
                exception = e;
            }
        }
        throw new AutotestError("Timeout of wating the message with text '" + message + "' has expired. " + comment, exception);
    }

    /**
     *
     * @param text TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    @ActionTitle("появляется модальное окно с текстом")
    @ActionTitle("modal window with text appears")
    public void assertModalWindowAppears(String text) throws PageException {
        try {
            String popupHandle = DriverExtensions.findNewWindowHandle((Set<String>) Stash.getValue("beforeClickHandles"));
            if (null != popupHandle && !popupHandle.isEmpty()) {
                PageFactory.getWebDriver().switchTo().window(popupHandle);
            }
            assertMessageAppears(text);
        } catch (Exception ex) {
            throw new PageException("TODO", ex);
        }
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     */
    @ActionTitle("не появляется сообщение")
    @ActionTitle("message doesn’t appear")
    public void assertMessageDoesNotAppear(String text) {
        new WebDriverWait(PageFactory.getWebDriver(), PageFactory.getTimeOutInSeconds()).
                until(ExpectedConditions.visibilityOf(PageFactory.getWebDriver().findElement(By.tagName("body"))));
        try {
            Assert.assertFalse(PageFactory.getWebDriver().findElement(By.tagName("body")).getText().replaceAll("\\s+", "").contains(text.replaceAll("\\s+", "")));
        } catch (AssertionError exception) {
            throw new AutotestError("The message with text '" + text + "' has appeared", exception);
        }
    }

    /**
     *
     * @param link a {@link java.lang.String} object.
     */
    @ActionTitle("отображается ссылка")
    @ActionTitle("link is visible")
    public void assertLinkIsVisible(String link) {
        assertMessageAppears(link);
    }

    /**
     *
     * @param link a {@link java.lang.String} object.
     */
    @ActionTitle("не отображается ссылка")
    @ActionTitle("link is not visible")
    public void assertLinkIsNotVisible(String link) {
        assertMessageDoesNotAppear(link);
    }

    /**
     *
     * @param values TODO
     */
    public void fillForm(Map<String, String> values) {
        throw new NoSuchElementException("There is no form defined in " + getTitle() + " page object");
    }

    /**
     *
     *
     */
    @ActionTitle("заполняет форму")
    @ActionTitle("fill the form")
    public void fillForm() {
        throw new NoSuchElementException("There is no form defined in " + getTitle() + " page object");
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     * @param elementTitle a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageException TODO
     */
    @ActionTitle("проверяет значение")
    @ActionTitle("check value")
    public void checkValue(String elementTitle, String text) throws PageException {
        checkValue(text, PageFactory.getPageFactory().getCurrentPage().getElementByTitle(elementTitle), MatchStrategy.EXACT);
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    public void checkValue(String text, WebElement webElement) throws ElementDescriptionException {
        checkValue(text, webElement, MatchStrategy.EXACT);
    }

    /**
     *
     * @param text a {@link java.lang.String} object.
     * @param webElement a {@link org.openqa.selenium.WebElement} object.
     * @param searchStrategy a MatchStrategy object.
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
     *
     * @param elementTitle TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     */
    @ActionTitle("проверяет что поле непустое")
    @ActionTitle("check that field is not empty")
    public void checkFieldIsNotEmpty(String elementTitle) throws PageException {
        WebElement webElement = PageFactory.getPageFactory().getCurrentPage().getElementByTitle(elementTitle);
        checkFieldIsNotEmpty(webElement);
    }

    /**
     *
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
     *
     * @param text a {@link java.lang.String} object.
     * @param elementTitle TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    @ActionTitle("проверяет несовпадение значения")
    @ActionTitle("check that values are not equal")
    public void checkValuesAreNotEqual(String text, String elementTitle) throws PageException {
        WebElement webElement = PageFactory.getPageFactory().getCurrentPage().getElementByTitle(elementTitle);
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

    private static class Core {

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

        private static Boolean isActionTitleContainsInAnnotation(Method method, String title) {
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

        private static <T> T getElementByField(Object object, Field field) throws ElementNotFoundException {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(object);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new NoSuchElementException("Represented field '" + field + "' is inaccessible or specified "
                        + "object is not an instance of the class or interface declaring the underlying field", e);
            }

            if (isChildOf(TypifiedElement.class, field)) {
                return (T) ((TypifiedElement) value).getWrappedElement();
            }

            return (T) value;
        }

        private static <T> T getElementByField(Object object, Field field, Field rootField) throws ElementNotFoundException {
            rootField.setAccessible(true);
            Object rootValue;
            try {
                rootValue = rootField.get(object);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new NoSuchElementException("Represented field '" + field + "' is inaccessible or specified "
                        + "object is not an instance of the class or interface declaring the underlying field", e);
            }

            return getElementByField(rootValue, field);
        }

        private static void addToReport(WebElement webElement, String text) {
            try {
                String elementTitle = PageFactory.getPageFactory().getCurrentPage().getElementTitle(webElement);
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
     * @param <T> TODO
     * @param element TODO
     * @return a {@link java.lang.String} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    public <T extends Object> String getElementTitle(T element) throws ElementDescriptionException {
        Page currentPage = null;
        try {
            currentPage = PageFactory.getPageFactory().getCurrentPage();
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
     * @param <T> TODO
     * @param <TypifiedElement> TODO
     * @param element TODO
     * @return TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementDescriptionException TODO
     */
    public <T extends WebElement, TypifiedElement> Class<? extends Page> getElementRedirect(T element) throws ElementDescriptionException {
        try {
            Page currentPage = PageFactory.getPageFactory().getCurrentPage();
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
     *
     * @param title a {@link java.lang.String} object.
     * @return a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException TODO
     */
    public WebElement getElementByTitle(String title) throws ElementNotFoundException {
        List<Field> fieldList = FieldUtilsExt.getDeclaredFieldsWithInheritance(this.getClass());

        List<Field> htmlElementFields = new ArrayList<>();

        for (Field field : fieldList) {
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof ElementTitle
                        && ((ElementTitle) annotation).value().equals(title)) {
                    return Core.getElementByField(this, field);
                }
            }
            if (field.getType().getSuperclass() == HtmlElement.class) {
                htmlElementFields.add(field);
            }
        }
        for (Field field : htmlElementFields) {
            for (Field f : FieldUtilsExt.getDeclaredFieldsWithInheritance(field.getType())) {
                for (ElementTitle a : f.getAnnotationsByType(ElementTitle.class)) {
                    if (a.value().equals(title)) {
                        return Core.getElementByField(this, f, field);
                    }
                }
            }
        }

        throw new NoSuchElementException(String.format("Элемент '%s' отсутствует странице '%s'", title, this.getTitle()));
    }

    /**
     *
     * @param <T> TODO
     * @param title a {@link java.lang.String} object.
     * @return a {@link org.openqa.selenium.WebElement} object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.ElementNotFoundException TODO
     */
    @SuppressWarnings(value = "unchecked")
    public <T extends TypifiedElement> T getTypifiedElementByTitle(String title) throws ElementNotFoundException {

        List<Field> fieldList = FieldUtilsExt.getDeclaredFieldsWithInheritance(this.getClass());

        for (Field field : fieldList) {
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof ElementTitle
                        && ((ElementTitle) annotation).value().equals(title)) {
                    if (Core.isChildOf(TypifiedElement.class, field)) {
                        return (T) Core.getElementByField(this, field);
                    }
                }
            }
        }

        throw new NoSuchElementException("Element '" + title + "' is not present on current page '" + this.getTitle() + "'");
    }

    /**
     * Execute method by MethodTitle
     *
     * @param <T> TODO
     * @param title MethodTitle name
     * @param type type return value
     * @param param TODO
     * @return TODO
     * @throws java.lang.NoSuchMethodException TODO
     */
    public <T extends Object> T executeMethodByTitle(String title, Class<T> type, Object... param) throws NoSuchMethodException {
        List<Method> methods = Core.getDeclaredMethods(this.getClass());
        for (Method method : methods) {
            if (Core.isActionTitleContainsInAnnotation(method, title)) {
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
     * @param <T> TODO
     * @param title MethodTitle name
     * @param param TODO
     * @return TODO
     * @throws java.lang.NoSuchMethodException TODO
     */
    public <T extends Object> T executeMethodByTitle(String title, Object... param) throws NoSuchMethodException {
        return executeMethodByTitle(title, (Class<T>) this.getClass(), param);
    }

    /**
     *
     * @param title a {@link java.lang.String} object.
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
        throw new PageInitializationException("There is no '" + title + "' validation rule in '" + getTitle() + "' page.");
    }
}
