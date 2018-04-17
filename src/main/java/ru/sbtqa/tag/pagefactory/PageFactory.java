package ru.sbtqa.tag.pagefactory;

import io.appium.java_client.AppiumDriver;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.validator.routines.IntegerValidator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.drivers.TagMobileDriver;
import ru.sbtqa.tag.pagefactory.drivers.TagWebDriver;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class PageFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PageFactory.class);

    private static final Map<Class<? extends Page>, Map<Field, String>> PAGES_REPOSITORY = new HashMap<>();

    private static Actions actions;
    private static PageWrapper PageWrapper;
    private static VideoRecorder videoRecorder;
    private static boolean aspectsDisabled = false;
    private static String defaultTimeout = "20771";

    private static final String ENVIRONMENT = Props.get("driver.environment");
    private static final String PAGES_PACKAGE = Props.get("page.package");
    private static String TIMEOUT;
    private static final String ENVIRONMENT_WEB = "web";
    private static final String ENVIRONMENT_MOBILE = "mobile";
    private static final boolean VIDEO_ENABLED = Boolean.parseBoolean(Props.get("video.enabled", "false"));
    private static boolean isSharingProcessing = false;

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
        actions = null;
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
            PageWrapper = new PageWrapper(getPagesPackage());
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
        if(PAGES_PACKAGE.isEmpty()) {
            throw new FactoryRuntimeException("Please add 'page.package = page package path' to application.properties");
        }

        return PAGES_PACKAGE;
    }

    /**
     * @return the timeOut
     */
    public static int getTimeOut() {
        if (TIMEOUT == null) {
            if (Props.get("page.load.timeout").isEmpty()) {
                LOG.warn("Set timeout in your properties file, key 'page.load.timeout'. Now using default value {} milliseconds.", defaultTimeout);
            }
            TIMEOUT = Props.get("page.load.timeout", defaultTimeout);
        }

        if (!IntegerValidator.getInstance().isValid(TIMEOUT)) {
            throw new FactoryRuntimeException("Incorrect value in property 'page.load.timeout', please set a numeric value. Now value is " + TIMEOUT);
        }

        return Integer.parseInt(TIMEOUT);
    }

    /**
     * @return the timeOut
     */
    public static int getTimeOutInSeconds() {
        return getTimeOut() / 1000;
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

    /**
     * @return the isSharingProcessing
     */
    public static boolean isSharingProcessing() {
        return isSharingProcessing;
    }

    /**
     * @param aIsSharingProcessing the isSharingProcessing to set
     */
    public static void setSharingProcessing(boolean aIsSharingProcessing) {
        isSharingProcessing = aIsSharingProcessing;
    }

    /**
     * Return true if webDriver or apiumDriver were initialized
     * @return {java.boolean} 
     */
    public static boolean isDriverInitialized(){
        return TagWebDriver.isDriverInitialized() || TagMobileDriver.isDriverInitialized();
    }
}
