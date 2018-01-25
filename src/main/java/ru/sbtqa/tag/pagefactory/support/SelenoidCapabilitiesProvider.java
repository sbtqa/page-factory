package ru.sbtqa.tag.pagefactory.support;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.qautils.properties.Props;

public class SelenoidCapabilitiesProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SelenoidCapabilitiesProvider.class);

    private static final String REMOTE_WEBDRIVER_BROWSER_VERSION = Props.get("webdriver.remote.browserVersion");
    private static final String REMOTE_WEBDRIVER_ENABLE_VNC = Props.get("webdriver.remote.enableVNC");
    private static final String REMOTE_WEBDRIVER_ENABLE_VIDEO = Props.get("webdriver.remote.enableVideo");
    private static final String REMOTE_WEBDRIVER_SCREEN_RESOLUTION = Props.get("webdriver.remote.screenResolution");
    private static final String REMOTE_WEBDRIVER_VIDEO_SCREEN_SIZE = Props.get("webdriver.remote.video.screenSize");
    private static final String REMOTE_WEBDRIVER_VIDEO_FRAME_RATE = Props.get("webdriver.remote.video.screenSize");
    private static final String REMOTE_WEBDRIVER_NAME_OF_TESTS = Props.get("webdriver.remote.nameOfTests");
    private static final String REMOTE_WEBDRIVER_TIME_ZONE = Props.get("webdriver.remote.timeZone");
    private static final String REMOTE_WEBDDRIVER_HOST_ENTRIES = Props.get("webdriver.remote.hostEntries");

    public static void apply(DesiredCapabilities capabilities) {

        if (!REMOTE_WEBDRIVER_BROWSER_VERSION.isEmpty()) {
            capabilities.setVersion(REMOTE_WEBDRIVER_BROWSER_VERSION);
        }
        else {
            LOG.info("Capability \"browserVersion\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_ENABLE_VNC.isEmpty()) {
            capabilities.setCapability("enableVNC", Boolean.parseBoolean(REMOTE_WEBDRIVER_ENABLE_VNC));
        }
        else {
            LOG.info("Capability \"enableVNC\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_ENABLE_VIDEO.isEmpty()) {
            capabilities.setCapability("enableVideo", Boolean.parseBoolean(REMOTE_WEBDRIVER_ENABLE_VIDEO));
        }
        else {
            LOG.info("Capability \"enableVideo\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_SCREEN_RESOLUTION.isEmpty()) {
            capabilities.setCapability("screenResolution", REMOTE_WEBDRIVER_SCREEN_RESOLUTION);
        }
        else {
            LOG.info("Capability \"screenResolution\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_VIDEO_SCREEN_SIZE.isEmpty()) {
            capabilities.setCapability("videoScreenSize", REMOTE_WEBDRIVER_VIDEO_SCREEN_SIZE);
        }
        else {
            LOG.info("Capability \"videoScreenSize\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_VIDEO_FRAME_RATE.isEmpty()) {
            capabilities.setCapability("videoFrameRate", REMOTE_WEBDRIVER_VIDEO_FRAME_RATE);
        }
        else {
            LOG.info("Capability \"videoFrameRate\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_NAME_OF_TESTS.isEmpty()) {
            capabilities.setCapability("name", REMOTE_WEBDRIVER_NAME_OF_TESTS);
        }
        else {
            LOG.info("Capability \"name\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDRIVER_TIME_ZONE.isEmpty()) {
            capabilities.setCapability("timeZone", REMOTE_WEBDRIVER_TIME_ZONE);
        }
        else {
            LOG.info("Capability \"timeZone\" for Selenoid isn't set. Using default capability.");
        }

        if (!REMOTE_WEBDDRIVER_HOST_ENTRIES.isEmpty()) {
            capabilities.setCapability("hostsEntries", REMOTE_WEBDDRIVER_HOST_ENTRIES);
        }
        else {
            LOG.info("Capability \"hostsEntries\" for Selenoid isn't set. Using default capability.");
        }
    }
}
