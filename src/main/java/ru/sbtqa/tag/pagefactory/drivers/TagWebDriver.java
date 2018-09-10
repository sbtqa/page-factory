package ru.sbtqa.tag.pagefactory.drivers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.github.bonigarcia.wdm.OperativeSystem;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Dimension;
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
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.exceptions.UnsupportedBrowserException;
import ru.sbtqa.tag.pagefactory.support.DesiredCapabilitiesParser;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.pagefactory.support.SelenoidCapabilitiesProvider;
import ru.sbtqa.tag.qautils.properties.Props;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.openqa.selenium.remote.BrowserType.CHROME;
import static org.openqa.selenium.remote.BrowserType.FIREFOX;
import static org.openqa.selenium.remote.BrowserType.IE;
import static org.openqa.selenium.remote.BrowserType.IEXPLORE;
import static org.openqa.selenium.remote.BrowserType.IE_HTA;
import static org.openqa.selenium.remote.BrowserType.SAFARI;
import static ru.sbtqa.tag.pagefactory.PageFactory.getTimeOutInSeconds;

public class TagWebDriver {

    private static final Logger LOG = LoggerFactory.getLogger(TagWebDriver.class);

    private static final String IE_BROWSER_TYPE = "ie";

    private static WebDriver webDriver;
    private static BrowserMobProxy proxy;
    private static final int WEBDRIVER_CREATE_ATTEMPTS = Integer.parseInt(Props.get("webdriver.create.attempts", "3"));
    private static final String WEBDRIVER_PATH = Props.get("webdriver.drivers.path");
    private static final String WEBDRIVER_URL = Props.get("webdriver.url");
    private static final String WEBDRIVER_STARTING_URL = Props.get("webdriver.starting.url");
    private static final String WEBDRIVER_PROXY = Props.get("webdriver.proxy");
    private static final boolean WEBDRIVER_BROWSER_IE_KILL_ON_DISPOSE = Boolean.parseBoolean(Props.get("webdriver.browser.ie.killOnDispose", "false"));
    private static final String WEBDRIVER_BROWSER_NAME = Props.get("webdriver.browser.name").equalsIgnoreCase(IE_BROWSER_TYPE)
            // Normalize it for ie shorten name (ie)
            ? IEXPLORE : Props.get("webdriver.browser.name").toLowerCase();
    private static final boolean IS_IE = WEBDRIVER_BROWSER_NAME.equalsIgnoreCase(IE)
            || WEBDRIVER_BROWSER_NAME.equalsIgnoreCase(IE_HTA)
            || WEBDRIVER_BROWSER_NAME.equalsIgnoreCase(IEXPLORE);
    private static final boolean WEBDRIVER_SHARED = Boolean.parseBoolean(Props.get("webdriver.shared", "false"));
    private static final String WEBDRIVER_NEXUS_URL = Props.get("webdriver.nexus.url");
    private static final String WEBDRIVER_DESIRABLE_VERSION = Props.get("webdriver.version");
    private static final String WEBDRIVER_BROWSER_VERSION = Props.get("webdriver.browser.version");
    private static final String WEBDRIVER_OS_ARCHITECTURE = Props.get("webdriver.os.arch");
    private static final String WEBDRIVER_BROWSER_PATH = Props.get("webdriver.browser.path");
    private static final String WEBDRIVER_BROWSER_SIZE = Props.get("webdriver.browser.size");
    private static final boolean WEBDRIVER_BROWSER_START_MAXIMIZED =
            Boolean.parseBoolean(Props.get("webdriver.browser.startMaximized", "true"));
    private static final String MAPPING_FILES_PATH = "drivers/mapping/";
    private static final String MAPPING_FILES_EXTENSION = ".json";

    private TagWebDriver() {
    }

    public static WebDriver getDriver() {
        if (Environment.WEB != PageFactory.getEnvironment()) {
            throw new FactoryRuntimeException("Failed to get web driver while environment is not web");
        }

        if (null == webDriver) {
            for (int i = 1; i <= WEBDRIVER_CREATE_ATTEMPTS; i++) {
                LOG.info("Attempt #{} to start web driver", i);
                try {
                    createDriver();
                    break;
                } catch (UnreachableBrowserException e) {
                    LOG.warn("Failed to create web driver. Attempt number {}", i, e);
                    dispose();
                } catch (UnsupportedBrowserException | MalformedURLException e) {
                    LOG.error("Failed to create web driver", e);
                    break;
                }
            }
        }
        return webDriver;
    }

    private static void createDriver() throws UnsupportedBrowserException, MalformedURLException {
        if(WEBDRIVER_BROWSER_NAME.isEmpty()) {
            throw new FactoryRuntimeException("Please add 'webdriver.browser.name  = browser name, for example Chrome' to application.properties");
        }

        DesiredCapabilities capabilities = new DesiredCapabilitiesParser().parse();

        //Local proxy available on local webdriver instances only
        configureProxy(capabilities);
        capabilities.setBrowserName(WEBDRIVER_BROWSER_NAME);

        if (WEBDRIVER_BROWSER_NAME.equalsIgnoreCase(FIREFOX)) {
            if (WEBDRIVER_URL.isEmpty()) {
                setWebDriver(new FirefoxDriver(capabilities));
            }
        } else if (WEBDRIVER_BROWSER_NAME.equalsIgnoreCase(SAFARI)) {
            if (WEBDRIVER_URL.isEmpty()) {
                setWebDriver(new SafariDriver(capabilities));
            }
        } else if (WEBDRIVER_BROWSER_NAME.equalsIgnoreCase(CHROME)) {
            if (WEBDRIVER_URL.isEmpty()) {
                configureDriver(ChromeDriverManager.getInstance(), CHROME);
                setWebDriver(new ChromeDriver(capabilities));
            }
        } else if (IS_IE) {
            if (WEBDRIVER_URL.isEmpty()) {
                configureDriver(InternetExplorerDriverManager.getInstance(), IE_BROWSER_TYPE);
                setWebDriver(new InternetExplorerDriver(capabilities));
            }
        } else {
            throw new UnsupportedBrowserException("'" + WEBDRIVER_BROWSER_NAME + "' is not supported yet");
        }
        if (!WEBDRIVER_URL.isEmpty()) {
            URL remoteUrl = new URL(WEBDRIVER_URL);
            SelenoidCapabilitiesProvider.apply(capabilities);
            setWebDriver(new RemoteWebDriver(remoteUrl, capabilities));
        }
        webDriver.manage().timeouts().pageLoadTimeout(getTimeOutInSeconds(), TimeUnit.SECONDS);

        if (WEBDRIVER_BROWSER_START_MAXIMIZED) {
            webDriver.manage().window().maximize();
        }

        if (!WEBDRIVER_BROWSER_SIZE.isEmpty()) {
            String[] size = WEBDRIVER_BROWSER_SIZE.split("x");
            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);
            webDriver.manage().window().setSize(new Dimension(width, height));
        }

        webDriver.get(WEBDRIVER_STARTING_URL);
    }

    private static void configureDriver(BrowserManager webDriverManager, String browserType) {
        if (!WEBDRIVER_PATH.isEmpty()) {
            System.setProperty("webdriver." + browserType + ".driver", new File(WEBDRIVER_PATH).getAbsolutePath());
        } else {
            LOG.warn("The value of property 'webdriver.drivers.path' is not specified."
                    + " Trying to automatically download and setup driver.");

            configureWebDriverManagerParams(webDriverManager, browserType);
            webDriverManager.setup();
        }
    }

    private static void configureProxy(DesiredCapabilities capabilities) {
        if (!WEBDRIVER_PROXY.isEmpty()) {
            setProxy(new BrowserMobProxyServer());
            proxy.start(0);
            Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        }
    }

    private static void configureWebDriverManagerParams(BrowserManager webDriverManager, String browserType) {
        configureWebDriverManagerVersion(webDriverManager, browserType);
        configureWebDriverManagerArch(webDriverManager);
        configureWebDriverManagerNexusLink(webDriverManager);
    }

    private static void configureWebDriverManagerNexusLink(BrowserManager webDriverManager) {
        if (!WEBDRIVER_NEXUS_URL.isEmpty()) {
            webDriverManager.useNexus(WEBDRIVER_NEXUS_URL);
        }
    }

    private static void configureWebDriverManagerArch(BrowserManager webDriverManager) {
        if (!WEBDRIVER_OS_ARCHITECTURE.isEmpty()) {
            if (Architecture.valueOf("X" + WEBDRIVER_OS_ARCHITECTURE) == Architecture.X32) {
                LOG.info("Forcing driver arch to X{}", WEBDRIVER_OS_ARCHITECTURE);
                webDriverManager.arch32();
            }
            if (Architecture.valueOf("X" + WEBDRIVER_OS_ARCHITECTURE) == Architecture.X64) {
                LOG.info("Forcing driver arch to X{}", WEBDRIVER_OS_ARCHITECTURE);
                webDriverManager.arch64();
            }
        }
    }

    private static void configureWebDriverManagerVersion(BrowserManager webDriverManager, String browserType) {
        String driverVersion = null;
        if (WEBDRIVER_DESIRABLE_VERSION.isEmpty()) {
            LOG.info("Trying to determine driver version based on browser version.");
            if (WEBDRIVER_BROWSER_VERSION.isEmpty()) {
                if (browserType.equalsIgnoreCase(IE_BROWSER_TYPE)) {
                    LOG.warn("You use IE browser. Switching to LATEST driver version. " +
                            "You can specify driver version by using 'webdriver.version' param.");
                } else {
                    driverVersion = parseDriverVersionFromMapping(detectBrowserVersion(), browserType.toLowerCase());
                }
            } else {
                driverVersion = parseDriverVersionFromMapping(WEBDRIVER_BROWSER_VERSION, browserType.toLowerCase());
            }
        } else {
            LOG.info("Forcing driver version to {}", WEBDRIVER_DESIRABLE_VERSION);
            driverVersion = WEBDRIVER_DESIRABLE_VERSION;
        }
        if (driverVersion == null && !browserType.equalsIgnoreCase(IE_BROWSER_TYPE)) {
            LOG.warn("Can't determine driver version. Rolling back to LATEST by default.");
        }
        webDriverManager.version(driverVersion);
    }

    private static String parseDriverVersionFromMapping(String browserVersion, String browserType) {
        if (browserVersion == null) {
            return null;
        }
        LOG.info("Trying to find driver corresponding to {} browser version.", browserVersion);

        JsonObject mappingObject = getResourceJsonFileAsJsonObject(MAPPING_FILES_PATH + browserType + MAPPING_FILES_EXTENSION);
        JsonElement browserVersionElement;
        if (mappingObject != null && (browserVersionElement = mappingObject.get(browserVersion)) != null) {
            return browserVersionElement.getAsString();
        } else {
            LOG.warn("Can't get corresponding driver for {} browser version. " +
                    "Using LATEST driver version.", browserVersion);
            return null;
        }
    }

    private static JsonObject getResourceJsonFileAsJsonObject(String filePath) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filePath);
        if (inputStream != null) {
            InputStreamReader isr = new InputStreamReader(inputStream);
            JsonReader reader = new JsonReader(isr);
            JsonParser parser = new JsonParser();
            return parser.parse(reader).getAsJsonObject();
        }
        return null;
    }

    private static String detectBrowserVersion() {
        LOG.info("The value of property 'webdriver.browser.version' is not specified. " +
                "Trying to detect your browser version automatically.");

        final String recommendMessage = "Please specify your browser version by " +
                "setting 'webdriver.browser.version' param.";
        final String errorMessage = "Error while detecting browser version.";

        OperativeSystem os = getDefaultOS();
        List<String> commandsToGetVersion;

        if (os != null) {
            commandsToGetVersion = getChromeCommands(os);
        } else {
            LOG.error("{} Can't get current OS. {}", errorMessage, recommendMessage);
            return null;
        }

        ProcessBuilder builder = new ProcessBuilder(commandsToGetVersion);
        builder.redirectErrorStream(true);
        Process p;
        try {
            p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = r.readLine()) != null) {
                Pattern versionPattern = Pattern.compile("(\\d+)\\S*");
                Matcher versionMatcher = versionPattern.matcher(line);
                if (versionMatcher.find()) {
                    return versionMatcher.group(1);
                }
            }
        } catch (IOException e) {
            LOG.error("Error while reading browser version from terminal.", e);
            return null;
        }
        LOG.error("Can't find browser binary in default location.");
        return null;
    }

    private static List<String> getChromeCommands(OperativeSystem os) {
        final String recommendMessage = "Please specify your browser version by " +
                "setting 'webdriver.browser.version' param.";
        List<String> commands = new ArrayList<>();
        String path = WEBDRIVER_BROWSER_PATH;

        switch (os) {
            case MAC: {
                LOG.warn("This OS is not supported for 'browser-driver' mapping yet. {}", recommendMessage);
                break;
            }
            case WIN: {
                if (path.isEmpty()){
                    path = "C:\\\\Program Files (x86)\\\\Google\\\\Chrome\\\\Application\\\\";
                }
                commands = Arrays.asList("cmd.exe", "/c", "wmic datafile where name=\"" + path + "chrome.exe\" get Version /value");
                break;
            }
            case LINUX: {
                if (path.isEmpty()){
                    path = "/usr/bin/";
                }
                commands = Arrays.asList("/bin/bash", "-c", path + "google-chrome --version");
                break;
            }
        }
        return commands;
    }

    private static OperativeSystem getDefaultOS() {
        OperativeSystem os = null;
        if (IS_OS_WINDOWS) {
            os = OperativeSystem.WIN;
        } else if (IS_OS_LINUX) {
            os = OperativeSystem.LINUX;
        } else if (IS_OS_MAC) {
            os = OperativeSystem.MAC;
        }
        return os;
    }

    public static void dispose() {
        if (webDriver == null) {
            return;
        }

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

        if (IS_IE && WEBDRIVER_BROWSER_IE_KILL_ON_DISPOSE) {
            killIE();
        }

        try {
            webDriver.quit();
        } finally {
            setWebDriver(null);
        }
    }

    private static void killIE() {
        try {
            LOG.info("Trying to terminate iexplorer process");
            Runtime.getRuntime().exec("taskkill /f /im iexplore.exe").waitFor();
            LOG.info("All iexplorer processes were terminated");
        } catch (IOException | InterruptedException e) {
            LOG.warn("Failed to wait for browser processes finish", e);
        }
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

    /**
     * @return the WEBDRIVER_BROWSER_NAME
     */
    public static String getBrowserName() {
        return WEBDRIVER_BROWSER_NAME;
    }

    /**
     * @return the WEBDRIVER_SHARED
     */
    public static boolean isWebDriverShared() {
        return WEBDRIVER_SHARED;
    }

    /**
     * @return was driver initialized or not
     */
    public static boolean isDriverInitialized() {
        return webDriver != null;
    }
}
