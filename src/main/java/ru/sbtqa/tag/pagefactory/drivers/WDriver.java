package ru.sbtqa.tag.pagefactory.drivers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariDriver;
import static ru.sbtqa.tag.pagefactory.PageFactory.getTimeOutInSeconds;
import static ru.sbtqa.tag.pagefactory.PageFactory.setProxy;
import static ru.sbtqa.tag.pagefactory.PageFactory.setWebDriver;
import ru.sbtqa.tag.pagefactory.exceptions.UnsupportedBrowserException;
import ru.sbtqa.tag.pagefactory.support.DesiredCapabilitiesParser;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class WDriver {
    
    private static WDriver webDriver;
    private static final int ATTEMPTS_TO_START_WEBDRIVER = Integer.parseInt(Props.get("driver.create.attempts", "3"));
    
    public static org.openqa.selenium.WebDriver getWebDriver() {
        if (null == webDriver) {
            if (Boolean.valueOf(Props.get("video.enable"))) {
                VideoRecorder.getInstance().startRecording();
            }

            for (int i = 1; i <= ATTEMPTS_TO_START_WEBDRIVER; i++) {
                log.info("Attempt #" + i + " to start web driver");
                try {
                    createWebDriver();
                    break;
                } catch (UnreachableBrowserException e) {
                    log.warn("Failed to create web driver. Attempt number {}", i, e);
                    if (null != webDriver) {
                        // Don't dispose when driver is already null, cus it causes new driver creation at Init.getWebDriver()
                        disposeWeb();
                    }
                } catch (UnsupportedBrowserException e) {
                    log.error("Failed to create web driver", e);
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
                log.error("Can not parse remote url. Check webdriver.remote.host property");
            }
        }
        webDriver.manage().timeouts().pageLoadTimeout(getTimeOutInSeconds(), TimeUnit.SECONDS);
        webDriver.manage().window().maximize();
        webDriver.get(INITIAL_URL);
    }
}