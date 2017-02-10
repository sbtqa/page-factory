package ru.sbtqa.tag.pagefactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.exceptions.UnsupportedBrowserException;
import ru.sbtqa.tag.pagefactory.support.DesiredCapabilitiesParser;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class PageFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PageFactory.class);

    private static WebDriver webDriver;
    private static Actions actions;
    private static PageWrapper PageFactoryCore;
    private static VideoRecorder videoRecorder;
    private static BrowserMobProxy proxy; // for use proxy, use Props proxy.enable = true
    private static final Map<Class<? extends Page>, Map<Field, String>> PAGES_REPOSITORY = new HashMap<>();

    private static final String PAGES_PACKAGE = Props.get("webdriver.pages.package");
    private static final String BROWSER_NAME = Props.get("browser.name");
    private static final String TIMEOUT = Props.get("webdriver.page.load.timeout");
    private static final String INITIAL_URL = Props.get("webdriver.url");
    private static final int ATTEMPTS_TO_START_WEBDRIVER = Integer.parseInt(Props.get("webdriver.create.attempts", "3"));

    private static final String WEBDRIVER_PATH = "src/test/resources/webdrivers/";

    private static boolean aspectsDisabled = false;

    /**
     * <p>
     * Getter for the field <code>webDriver</code>.</p>
     *
     * @return a {@link org.openqa.selenium.WebDriver} object.
     */
    public static WebDriver getWebDriver() {
        if (null == webDriver) {
            if (Boolean.valueOf(Props.get("video.enable"))) {
                VideoRecorder.getInstance().startRecording();
            }

            for (int i = 1; i <= ATTEMPTS_TO_START_WEBDRIVER; i++) {
                LOG.info("Attempt #" + i + " to start web driver");
                try {
                    createWebDriver();
                    break;
                } catch (UnreachableBrowserException e) {
                    LOG.warn("Failed to create web driver. Attempt number {}", i, e);
                    if (null != webDriver) {
                        // Don't dispose when driver is already null, cus it causes new driver creation at Init.getWebDriver()
                        dispose();
                    }
                } catch (UnsupportedBrowserException e) {
                    LOG.error("Failed to create web driver", e);
                    break;
                }
            }
        }
        return webDriver;
    }

    private static void createWebDriver() throws UnsupportedBrowserException {
        DesiredCapabilities capabilities = new DesiredCapabilitiesParser().parse();

        if (Props.get("webdriver.remote.host").isEmpty()) {

            //Local proxy available on local webdriver instances only
            if (!Props.get("proxy.enable").isEmpty()) {
                setProxy(new BrowserMobProxyServer());
                proxy.start(0);
                Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
                capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
            }
            switch (BROWSER_NAME) {
                case "Firefox":
                    capabilities.setBrowserName("firefox");
                    setWebDriver(new FirefoxDriver(capabilities));
                    break;
                case "Safari":
                    capabilities.setBrowserName("safari");
                    setWebDriver(new SafariDriver(capabilities));
                    break;
                case "Chrome":
                    File chromeDriver = new File(WEBDRIVER_PATH + "chromedriver.exe");
                    System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
                    capabilities.setBrowserName("chrome");
                    setWebDriver(new ChromeDriver(capabilities));
                    break;
                case "Opera":
                    throw new UnsupportedOperationException("Opera browser is not supported yet.");
                case "IE":
                    File IEdriver = new File(WEBDRIVER_PATH + "IEDriverServer.exe");
                    System.setProperty("webdriver.ie.driver", IEdriver.getAbsolutePath());
                    capabilities.setBrowserName("internet explorer");
                    setWebDriver(new InternetExplorerDriver(capabilities));
                    break;
                default:
                    throw new UnsupportedBrowserException("'" + BROWSER_NAME + "' is not supported yet");
            }
        } else {

            switch (BROWSER_NAME) {
                case "Firefox":
                    capabilities.setBrowserName("firefox");
                    break;
                case "Safari":
                    capabilities.setBrowserName("safari");
                    break;
                case "Chrome":
                    File chromeDriver = new File(WEBDRIVER_PATH + "chromedriver.exe");
                    System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
                    capabilities.setBrowserName("chrome");
                    break;
                case "Opera":
                    throw new UnsupportedOperationException("Opera browser supported as Chrome So change config to chrome.");
                case "IE":
                    File IEdriver = new File(WEBDRIVER_PATH + "IEDriverServer.exe");
                    System.setProperty("webdriver.ie.driver", IEdriver.getAbsolutePath());
                    capabilities.setBrowserName("internet explorer");
                    break;
                default:
                    throw new UnsupportedBrowserException("'" + BROWSER_NAME + "' is not supported yet");
            }
            try {
                URL remoreUrl = new URL("http://" + Props.get("webdriver.remote.host") + ":4444/wd/hub");
                setWebDriver(new RemoteWebDriver(remoreUrl, capabilities));
            } catch (MalformedURLException e) {
                LOG.error("Can not parse remote url. Check webdriver.remote.host property");
            }
        }
        webDriver.manage().timeouts().pageLoadTimeout(getTimeOutInSeconds(), TimeUnit.SECONDS);
        webDriver.manage().window().maximize();
        webDriver.get(INITIAL_URL);
    }

    /**
     *
     */
    public static void dispose() {
        try {
            LOG.info("Checking any alert opened");
            WebDriverWait alertAwaiter = new WebDriverWait(webDriver, 2);
            alertAwaiter.until(ExpectedConditions.alertIsPresent());
            Alert alert = webDriver.switchTo().alert();
            LOG.info("Got an alert: " + alert.getText() + "\n Closing it.");
            alert.dismiss();
        } catch (WebDriverException e) {
            LOG.debug("No alert opened. Closing webdriver.", e);
        }

        Set<String> windowHandlesSet = webDriver.getWindowHandles();
        try {
            if (windowHandlesSet.size() > 1) {
                for (String winHandle : windowHandlesSet) {
                    webDriver.switchTo().window(winHandle);
                    ((JavascriptExecutor) webDriver).executeScript(
                            "var objWin = window.self;"
                            + "objWin.open('','_self','');"
                            + "objWin.close();");
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to kill all of the iexplore windows", e);
        }

        try {
            if ("IE".equals(BROWSER_NAME)
                    && Boolean.parseBoolean(Props.get("browser.ie.killOnDispose", "true"))) {
                // Kill IE by Windows means instead of webdriver.quit()
                Runtime.getRuntime().exec("taskkill /f /im iexplore.exe").waitFor();
                Runtime.getRuntime().exec("taskkill /f /im IEDriverServer.exe").waitFor();
            } else {
                webDriver.quit();
            }
        } catch (WebDriverException | IOException | InterruptedException e) {
            LOG.warn("Failed to quit web driver", e);
        } finally {
            try {
                //TODO take out into a separate method
                // Wait for processes disappear, this might take a few seconds
                if (SystemUtils.IS_OS_WINDOWS) {
                    String brwsrNm = BROWSER_NAME.toLowerCase().trim();
                    if ("ie".equals(brwsrNm)) {
                        brwsrNm = "iexplore";
                    }
                    int i = 0;
                    while (i <= 10) {
                        if (Runtime.getRuntime().exec("tasklist | findstr " + brwsrNm).waitFor() == 0) {
                            Thread.sleep(1000);
                        } else {
                            i = 10;
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                LOG.warn("Failed to wait for browser processes finish", e);
            }
        }

        setWebDriver(null);
        PageFactoryCore = null;
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
}
