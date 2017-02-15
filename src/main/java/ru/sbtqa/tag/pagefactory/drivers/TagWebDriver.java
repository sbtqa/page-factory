package ru.sbtqa.tag.pagefactory.drivers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.PageFactory;
import static ru.sbtqa.tag.pagefactory.PageFactory.getTimeOutInSeconds;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.exceptions.UnsupportedBrowserException;
import ru.sbtqa.tag.pagefactory.support.DesiredCapabilitiesParser;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class TagWebDriver {

    private static final Logger log = LoggerFactory.getLogger(TagWebDriver.class);

    private static WebDriver webDriver;
    private static BrowserMobProxy proxy;
    private static final int WEBDRIVER_CREATE_ATTEMPTS = Integer.parseInt(Props.get("webdriver.create.attempts", "3"));
    private static final String WEBDRIVER_PATH = "src/test/resources/webdrivers/";
    private static final String WEBDRIVER_URL = Props.get("webdriver.url");
    private static final String WEBDRIVER_STARTING_URL = Props.get("webdriver.starting.url");
    private static final String WEBDRIVER_PROXY_ENABLE = Props.get("webdriver.proxy.enabled", "false");
    private static final String WEBDRIVER_BROWSER_IE_KILLONDISPOSE = Props.get("webdriver.browser.ie.killOnDispose", "false");
    private static final String VIDEO_ENABLE = Props.get("video.enabled", "false");
    

    public static org.openqa.selenium.WebDriver getDriver() {
	if (Environment.WEB != PageFactory.getEnvironment()) {
	    throw new FactoryRuntimeException("Failed to get web driver while environment is not web");
	}
	
        if (null == webDriver) {
            if (Boolean.valueOf(VIDEO_ENABLE)) {
                VideoRecorder.getInstance().startRecording();
            }

            for (int i = 1; i <= WEBDRIVER_CREATE_ATTEMPTS; i++) {
                log.info("Attempt #" + i + " to start web driver");
                try {
                    createDriver();
                    break;
                } catch (UnreachableBrowserException e) {
                    log.warn("Failed to create web driver. Attempt number {}", i, e);
                    if (null != webDriver) {
                        // Don't dispose when driver is already null, cuz it causes new driver creation at Init.getWebDriver()
                        dispose();
                    }
                } catch (UnsupportedBrowserException e) {
                    log.error("Failed to create web driver", e);
                    break;
                }
            }
        }
        return webDriver;
    }

    private static void createDriver() throws UnsupportedBrowserException {
        DesiredCapabilities capabilities = new DesiredCapabilitiesParser().parse();

        if (WEBDRIVER_URL.isEmpty()) {

            //Local proxy available on local webdriver instances only
            if (!WEBDRIVER_PROXY_ENABLE.isEmpty()) {
                setProxy(new BrowserMobProxyServer());
                proxy.start(0);
                Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
                capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
            }
            switch (PageFactory.getBrowserName()) {
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
                    throw new UnsupportedBrowserException("'" + PageFactory.getBrowserName() + "' is not supported yet");
            }
        } else {

            switch (PageFactory.getBrowserName()) {
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
                    throw new UnsupportedBrowserException("'" + PageFactory.getBrowserName() + "' is not supported yet");
            }
            try {
                URL remoteUrl = new URL(WEBDRIVER_URL);
                setWebDriver(new RemoteWebDriver(remoteUrl, capabilities));
            } catch (MalformedURLException e) {
                log.error("Could not parse remote url. Check 'webdriver.url' property");
            }
        }
        webDriver.manage().timeouts().pageLoadTimeout(getTimeOutInSeconds(), TimeUnit.SECONDS);
        webDriver.manage().window().maximize();
        webDriver.get(WEBDRIVER_STARTING_URL);
    }

    public static void dispose() {
        try {
            log.info("Checking any alert opened");
            WebDriverWait alertAwaiter = new WebDriverWait(webDriver, 2);
            alertAwaiter.until(ExpectedConditions.alertIsPresent());
            Alert alert = webDriver.switchTo().alert();
            log.info("Got an alert: " + alert.getText() + "\n Closing it.");
            alert.dismiss();
        } catch (WebDriverException e) {
            log.debug("No alert opened. Closing webdriver.", e);
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
            log.warn("Failed to kill all of the iexplore windows", e);
        }

        try {
            if ("IE".equals(PageFactory.getBrowserName())
                  && Boolean.parseBoolean(WEBDRIVER_BROWSER_IE_KILLONDISPOSE)) {
                // Kill IE by Windows means instead of webdriver.quit()
                Runtime.getRuntime().exec("taskkill /f /im iexplore.exe").waitFor();
                Runtime.getRuntime().exec("taskkill /f /im IEDriverServer.exe").waitFor();
            } else {
                webDriver.quit();
            }
        } catch (WebDriverException | IOException | InterruptedException e) {
            log.warn("Failed to quit web driver", e);
        } finally {
            try {
                //TODO take out into a separate method
                // Wait for processes disappear, this might take a few seconds
                if (SystemUtils.IS_OS_WINDOWS) {
                    String brwsrNm = PageFactory.getBrowserName().toLowerCase().trim();
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
                log.warn("Failed to wait for browser processes finish", e);
            }
        }

        setWebDriver(null);
    }

    /**
     * @param aWebDriver the webDriver to set
     */
    public static void setWebDriver(WebDriver aWebDriver) {
        webDriver = aWebDriver;
    }

    /**
     * @param aProxy the proxy to set
     */
    public static void setProxy(BrowserMobProxy aProxy) {
        proxy = aProxy;
    }
}
