package ru.sbtqa.tag.pagefactory.drivers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import java.io.File;
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

public class MblDriver {

    private static final Logger LOG = LoggerFactory.getLogger(MblDriver.class);

    private static AppiumDriver<AndroidElement> mobileDriver;

    public static AppiumDriver<AndroidElement> getDriver() {
	if (Environment.MOBILE != PageFactory.getEnvironment()) {
	    throw new FactoryRuntimeException("Failed to get mobile driver while environment is not mobile");
	}
	
	if (null == mobileDriver) {
	    if (Boolean.valueOf(Props.get("video.enable"))) {
		VideoRecorder.getInstance().startRecording();
	    }

	    createDriver();
	}
	return mobileDriver;
    }

    private static void createDriver() {
	File appDir = new File(System.getProperty("user.dir"), "app");
	File app = new File(appDir, Props.get("appium.app.name"));
	LOG.info("Absolute path to app is {}", app.getAbsolutePath());

	DesiredCapabilities capabilities = new DesiredCapabilities();
	capabilities.setCapability("deviceName", Props.get("appium.device.name"));
	capabilities.setCapability("platformVersion", Props.get("appium.device.platform"));
	capabilities.setCapability("app", app.getAbsolutePath());
	capabilities.setCapability("appPackage", Props.get("appium.app.package"));
	capabilities.setCapability("appActivity", Props.get("appium.app.activity"));
	capabilities.setCapability("autoGrantPermissions", "true");
	LOG.info("Capabilities are {}", capabilities);

	URL url;
	try {
	    url = new URL(PageFactory.getInitialUrl());
	} catch (MalformedURLException e) {
	    throw new FactoryRuntimeException("Failed to connect to appium on url " + PageFactory.getInitialUrl(), e);
	}

	setAspectsDisabled(true);
	LOG.info("Aspect disabled");
	mobileDriver = new AndroidDriver<>(url, capabilities);
	LOG.info("Mobile driver created {}", mobileDriver);
    }

    public static void dispose() {
	if (mobileDriver != null) {
	    mobileDriver.quit();
	}
    }
}
