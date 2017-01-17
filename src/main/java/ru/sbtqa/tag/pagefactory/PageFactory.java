package ru.sbtqa.tag.pagefactory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.lightbody.bmp.BrowserMobProxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.drivers.WDriver;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class PageFactory {

    private static final Logger log = LoggerFactory.getLogger(PageFactory.class);

    
    private static AppiumDriver<AndroidElement> mobileDriver;
    private static Actions actions;
    private static PageWrapper PageFactoryCore;
    private static VideoRecorder videoRecorder;
    private static BrowserMobProxy proxy; // for use proxy, use Props proxy.enable = true
    private static final Map<Class<? extends Page>, Map<Field, String>> PAGES_REPOSITORY = new HashMap<>();

    private static final String ENVIRONMENT = Props.get("driver.environment");
    private static final String PAGES_PACKAGE = Props.get("page.package");
    private static final String BROWSER_NAME = Props.get("browser.name");
    private static final String TIMEOUT = Props.get("page.load.timeout");
    private static final String INITIAL_URL = Props.get("driver.url");
    
//    private static final boolean isScreenshotTaken = false;

    private static final String WEBDRIVER_PATH = "src/test/resources/webdrivers/";

    private static boolean aspectsDisabled = false;

    public static <T> T getDriver() {
        switch (getEnvironment()) {
            case WEB:
                return (T) WDriver.getWebDriver();
            case MOBILE:
                return (T) getMobileDriver();
            default:
                throw new FactoryRuntimeException("Failed to get driver");
        }
    }

    public static WebDriver getWebDriver() {
        return getDriver();
    }
    
    public static AppiumDriver getMobileDriver() {
        return getDriver();
    }
    
    public static void dispose() {
        switch (getEnvironment()) {
            case WEB:
                disposeWeb();
                break;
            case MOBILE:
                disposeMobile();
                break;
            default:
                throw new FactoryRuntimeException("Failed to dispose");
        }
    }

    

    

    /**
     *
     * @param driver TODO
     * @param page TODO
     */
    public static void initElements(WebDriver driver, Object page) {
        org.openqa.selenium.support.PageFactory.initElements(driver, page);
    }

    /**
     *
     * @param decorator TODO
     * @param page TODO
     */
    public static void initElements(FieldDecorator decorator, Object page) {
        org.openqa.selenium.support.PageFactory.initElements(decorator, page);
    }

    /**
     * Get PageFactory instance
     *
     * @return PageFactory
     */
    public static PageWrapper getInstance() {
        if (null == PageFactoryCore) {
            PageFactoryCore = new PageWrapper(PAGES_PACKAGE);
        }
        return PageFactoryCore;
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
     * @param aWebDriver the webDriver to set
     */
    public static void setWebDriver(WebDriver aWebDriver) {
        webDriver = aWebDriver;
    }

    /**
     * @return the browserName
     */
    public static String getBrowserName() {
        return BROWSER_NAME;
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
     * @param aProxy the proxy to set
     */
    public static void setProxy(BrowserMobProxy aProxy) {
        proxy = aProxy;
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

    /**
     * @return the videoRecorder
     */
//    public static VideoRecorder getVideoRecorder() {
//        return videoRecorder;
//    }
    /**
     *
     */
    public static void setVideoRecorderToNull() {
        videoRecorder = null;
    }

    public static Environment getEnvironment() {
        switch (ENVIRONMENT) {
            case "web":
                return Environment.WEB;
            case "mobile":
                return Environment.MOBILE;
            default:
                throw new FactoryRuntimeException("Environment '" + ENVIRONMENT + "' is not supported");
        }
    }
}
