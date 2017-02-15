package ru.sbtqa.tag.pagefactory.drivers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.PageFactory;
import static ru.sbtqa.tag.pagefactory.PageFactory.setAspectsDisabled;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class TagMobileDriver {

    private static final Logger LOG = LoggerFactory.getLogger(TagMobileDriver.class);

    private static AppiumDriver<AndroidElement> mobileDriver;
    private static final String APPIUM_URL = Props.get("appium.url");
    private static final String APPIUM_DEVICE_NAME = Props.get("appium.device.name");
    private static final String APPIUM_DEVICE_PLATFORM = Props.get("appium.device.platform");
    private static final String APPIUM_APP_PACKAGE = Props.get("appium.app.package");
    private static final String APPIUM_APP_ACTIVITY = Props.get("appium.app.activity");
    private static final String VIDEO_ENABLE = Props.get("video.enable", "false");
    
    public static AppiumDriver<AndroidElement> getDriver() {
	if (Environment.MOBILE != PageFactory.getEnvironment()) {
	    throw new FactoryRuntimeException("Failed to get mobile driver while environment is not mobile");
	}
	
	if (null == mobileDriver) {
	    if (Boolean.valueOf(VIDEO_ENABLE)) {
		VideoRecorder.getInstance().startRecording();
	    }

	    createDriver();
	}
	return mobileDriver;
    }

    private static void createDriver() {
	DesiredCapabilities capabilities = new DesiredCapabilities();
	capabilities.setCapability("deviceName", APPIUM_DEVICE_NAME);
	capabilities.setCapability("platformVersion", APPIUM_DEVICE_PLATFORM);
	capabilities.setCapability("appPackage", APPIUM_APP_PACKAGE);
	capabilities.setCapability("appActivity", APPIUM_APP_ACTIVITY);
	capabilities.setCapability("autoGrantPermissions", "true");
	capabilities.setCapability("unicodeKeyboard", "true");
	capabilities.setCapability("resetKeyboard", "true");
	LOG.info("Capabilities are {}", capabilities);

	URL url;
	try {
	    url = new URL(APPIUM_URL);
	} catch (MalformedURLException e) {
	    throw new FactoryRuntimeException("Appium url is malformed", e);
	}

	setAspectsDisabled(true);
	LOG.debug("Aspect disabled");
	mobileDriver = new AndroidDriver<>(url, capabilities);
	LOG.info("Mobile driver created {}", mobileDriver);
    }

    public static void dispose() {
	if (mobileDriver != null) {
	    mobileDriver.quit();
	}
    }
}
