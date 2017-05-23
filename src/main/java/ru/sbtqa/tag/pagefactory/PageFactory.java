package ru.sbtqa.tag.pagefactory;

import io.appium.java_client.AppiumDriver;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.drivers.TagMobileDriver;
import ru.sbtqa.tag.pagefactory.drivers.TagWebDriver;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.qautils.reflect.ClassUtilsExt;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.sbtqa.tag.videorecorder.VideoRecorder;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

public class PageFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PageFactory.class);

    private static final Map<Class<? extends Page>, Map<Field, String>> PAGES_REPOSITORY = new HashMap<>();

    private static Actions actions;
    private static PageWrapper PageWrapper;
    private static VideoRecorder videoRecorder;
    private static boolean aspectsDisabled = false;

    private static final String ENVIRONMENT = Props.get("driver.environment");
    private static final String PAGES_PACKAGE = Props.get("page.package");
    private static final String TIMEOUT = Props.get("page.load.timeout");
    private static final String ENVIRONMENT_WEB = "web";
    private static final String ENVIRONMENT_MOBILE = "mobile";
    private static final boolean VIDEO_ENABLED = Boolean.parseBoolean(Props.get("video.enabled", "false"));

    public static WebDriver getWebDriver() {
        return getDriver();
    }

    public static AppiumDriver getMobileDriver() {
        return (AppiumDriver) getDriver();
    }

    public static WebDriver getDriver() {
        switch (getEnvironment()) {
            case WEB:
                return TagWebDriver.getDriver();
            case MOBILE:
                return TagMobileDriver.getDriver();
            default:
                throw new FactoryRuntimeException("Failed to get driver");
        }
    }

    public static void dispose() {
        PageWrapper = null;
        switch (getEnvironment()) {
            case WEB:
                TagWebDriver.dispose();
                break;
            case MOBILE:
                TagMobileDriver.dispose();
                break;
            default:
                throw new FactoryRuntimeException("Failed to dispose");
        }
    }

    public static void initElements(WebDriver driver, Object page) {
        org.openqa.selenium.support.PageFactory.initElements(driver, page);
    }

    public static void initElements(FieldDecorator decorator, Object page) {
        org.openqa.selenium.support.PageFactory.initElements(decorator, page);
    }

    /**
     * Get PageFactory instance
     *
     * @return PageFactory
     */
    public static PageWrapper getInstance() {
        if (null == PageWrapper) {
            PageWrapper = new PageWrapper(PAGES_PACKAGE);

            Reflections reflections;
            reflections = new Reflections(PageFactory.getPagesPackage());

            Collection<String> allClassesString = reflections.getStore().get("SubTypesScanner").values();
            Set<Class<?>> allClasses = new HashSet();
            for (String clazz : allClassesString) {
                try {
                    allClasses.add(Class.forName(clazz));
                } catch (ClassNotFoundException e) {
                    LOG.warn("Cannot add all classes to set from package storage", e);
                }
            }

            for (Class<?> page : allClasses) {
                List<Class> supers = ClassUtilsExt.getSuperclassesWithInheritance(page);
                if (!supers.contains(Page.class) && !supers.contains(HtmlElement.class)) {
                    if (page.getName().contains("$")) {
                        continue; //We allow private additional classes but skip it if its not extends Page
                    } else {
                        throw new FactoryRuntimeException("Class " + page.getName() + " is not extended from Page class. Check you webdriver.pages.package property.");
                    }
                }
                List<Field> fields = FieldUtilsExt.getDeclaredFieldsWithInheritance(page);
                Map<Field, String> fieldsMap = new HashMap<>();
                for (Field field : fields) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.equals(WebElement.class)) {

                        ElementTitle titleAnnotation = field.getAnnotation(ElementTitle.class);
                        if (titleAnnotation != null) {
                            fieldsMap.put(field, titleAnnotation.value());
                        } else {
                            fieldsMap.put(field, field.getName());
                        }
                    }
                }

                PageFactory.getPageRepository().put((Class<? extends Page>) page, fieldsMap);
            }
        }
        return PageWrapper;
    }

    /**
     * Get driver actions
     *
     * @return Actions
     */
    public static Actions getActions() {
        if (null == actions) {
            actions = new Actions(getWebDriver());
        }
        return actions;
    }

    /**
     * @return the pagesPackage
     */
    public static String getPagesPackage() {
        return PAGES_PACKAGE;
    }

    /**
     * @return the timeOut
     */
    public static int getTimeOut() {
        return Integer.parseInt(TIMEOUT);
    }

    /**
     * @return the timeOut
     */
    public static int getTimeOutInSeconds() {
        return Integer.parseInt(TIMEOUT) / 1000;
    }

    /**
     * @return the pageRepository
     */
    public static Map<Class<? extends Page>, Map<Field, String>> getPageRepository() {
        return PAGES_REPOSITORY;
    }

    /**
     * Affects click and sendKeys aspects only
     *
     * @return the aspectsDisabled default false
     */
    public static boolean isAspectsDisabled() {
        return aspectsDisabled;
    }

    /**
     * Affects click and sendKeys aspects only
     *
     * @param aAspectsDisabled default false
     */
    public static void setAspectsDisabled(boolean aAspectsDisabled) {
        aspectsDisabled = aAspectsDisabled;
    }

    public static void setVideoRecorderToNull() {
        videoRecorder = null;
    }

    /**
     * Checks if video recording enabled
     *
     * @return true if video.enabled property defined as true
     */
    public static boolean isVideoRecorderEnabled() {
        return VIDEO_ENABLED;
    }

    public static Environment getEnvironment() {
        switch (ENVIRONMENT) {
            case ENVIRONMENT_WEB:
                return Environment.WEB;
            case ENVIRONMENT_MOBILE:
                return Environment.MOBILE;
            default:
                if (ENVIRONMENT.equals("")) {
                    throw new FactoryRuntimeException("Please add 'driver.environment = web' or 'driver.environment = mobile' to application.properties");
                } else {
                    throw new FactoryRuntimeException("Environment '" + ENVIRONMENT + "' is not supported");
                }
        }
    }
}
