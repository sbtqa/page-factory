package ru.sbtqa.tag.pagefactory.support;

import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.drivers.TagWebDriver;
import ru.sbtqa.tag.qautils.properties.Props;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class SelenoidCapabilitiesProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SelenoidCapabilitiesProvider.class);

    private static final String SELENOID_BROWSER_VERSION = Props.get("selenoid.browserVersion");
    private static final String SELENOID_ENABLE_VNC = Props.get("selenoid.enableVNC");
    private static final String SELENOID_SCREEN_RESOLUTION = Props.get("selenoid.screenResolution");
    private static final String SELENOID_ENABLE_VIDEO = Props.get("selenoid.enableVideo");
    private static final String SELENOID_VIDEO_NAME = Props.get("selenoid.video.name");
    private static final String SELENOID_VIDEO_SCREEN_SIZE = Props.get("selenoid.video.screenSize");
    private static final String SELENOID_VIDEO_FRAME_RATE = Props.get("selenoid.video.frameRate");
    private static final String SELENOID_NAME_OF_TESTS = Props.get("selenoid.nameOfTests");
    private static final String SELENOID_TIME_ZONE = Props.get("selenoid.timeZone");
    private static final String SELENOID_HOST_ENTRIES = Props.get("selenoid.hostEntries");
    private static final String SELENOID_APPLICATION_CONTAINERS = Props.get("selenoid.applicationContainers");
    private static final String SELENOID_CONTAINER_LABLES = Props.get("selenoid.containerLables");

    public static void apply(DesiredCapabilities capabilities) {

        if (!SELENOID_BROWSER_VERSION.isEmpty()) {
            capabilities.setVersion(SELENOID_BROWSER_VERSION);
        } else {
            LOG.info("Capability \"browserVersion\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_ENABLE_VNC.isEmpty()) {
            capabilities.setCapability("enableVNC", Boolean.parseBoolean(SELENOID_ENABLE_VNC));
        } else {
            LOG.info("Capability \"enableVNC\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_SCREEN_RESOLUTION.isEmpty()) {
            capabilities.setCapability("screenResolution", SELENOID_SCREEN_RESOLUTION);
        } else {
            LOG.info("Capability \"screenResolution\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_ENABLE_VIDEO.isEmpty()) {
            capabilities.setCapability("enableVideo", Boolean.parseBoolean(SELENOID_ENABLE_VIDEO));
        } else {
            LOG.info("Capability \"enableVideo\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_VIDEO_NAME.isEmpty()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy'_'hh:mm:ss");
            capabilities.setCapability("videoName",
                    simpleDateFormat.format(new Date()) + '_' + UUID.randomUUID().toString() + "_" + SELENOID_VIDEO_NAME);
        } else {
            LOG.info("Capability \"videoName\" for Selenoid isn't set. Using session id for name video.");
        }

        if (!SELENOID_VIDEO_SCREEN_SIZE.isEmpty()) {
            capabilities.setCapability("videoScreenSize", SELENOID_VIDEO_SCREEN_SIZE);
        } else {
            LOG.info("Capability \"videoScreenSize\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_VIDEO_FRAME_RATE.isEmpty()) {
            capabilities.setCapability("videoFrameRate", SELENOID_VIDEO_FRAME_RATE);
        } else {
            LOG.info("Capability \"videoFrameRate\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_NAME_OF_TESTS.isEmpty()) {
            capabilities.setCapability("name", SELENOID_NAME_OF_TESTS);
        } else {
            LOG.info("Capability \"name\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_TIME_ZONE.isEmpty()) {
            capabilities.setCapability("timeZone", SELENOID_TIME_ZONE);
        } else {
            LOG.info("Capability \"timeZone\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_HOST_ENTRIES.isEmpty()) {
            capabilities.setCapability("hostsEntries", SELENOID_HOST_ENTRIES);
        } else {
            LOG.info("Capability \"hostsEntries\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_APPLICATION_CONTAINERS.isEmpty()) {
            capabilities.setCapability("applicationContainers", SELENOID_APPLICATION_CONTAINERS);
        } else {
            LOG.info("Capability \"applicationContainers\" for Selenoid isn't set. Using default capability.");
        }

        if (!SELENOID_CONTAINER_LABLES.isEmpty()) {
            capabilities.setCapability("labels", SELENOID_CONTAINER_LABLES);
        } else {
            LOG.info("Capability \"labels\" for Selenoid isn't set. Using default capability.");
        }

        if (TagWebDriver.getBrowserName().equalsIgnoreCase(BrowserType.OPERA)) {
            capabilities.setCapability("operaOptions", new HashMap<String, String>() {
                {
                    put("binary", "/usr/bin/opera");
                }
            });
        }

        if (TagWebDriver.getBrowserName().equalsIgnoreCase(BrowserType.IEXPLORE)) {
            capabilities.setCapability("ie.usePerProcessProxy", true);
            capabilities.setCapability("ie.browserCommandLineSwitches", "-private");
            capabilities.setCapability("ie.ensureCleanSession", true);
            capabilities.setCapability("requireWindowFocus", false);
        }
    }
}
