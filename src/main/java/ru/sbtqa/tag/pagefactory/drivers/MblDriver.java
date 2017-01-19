package ru.sbtqa.tag.pagefactory.drivers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.sbtqa.tag.pagefactory.PageFactory;
import static ru.sbtqa.tag.pagefactory.PageFactory.setAspectsDisabled;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;

public class MblDriver {
    
    private static AppiumDriver<AndroidElement> mobileDriver;
    
    public static AppiumDriver<AndroidElement> getDriver() {
        if (null == mobileDriver) {
            createDriver();
        }
        return mobileDriver;
    }

    private static void createDriver() {
        File classpathRoot = new File(System.getProperty("user.dir"));
        //TODO refactor app get
        File appDir = new File(classpathRoot, "app");
        File app = new File(appDir, "friend.apk");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        //TODO move to application properties
        capabilities.setCapability("deviceName", "Android Emulator");
        capabilities.setCapability("platformVersion", "6.0");
        capabilities.setCapability("app", app.getAbsolutePath());
        capabilities.setCapability("appPackage", "ru.sberbankmobile");
        capabilities.setCapability("appActivity", "SplashActivity");
        capabilities.setCapability("autoGrantPermissions", "true");

        URL url;
        try {
            url = new URL(PageFactory.getInitialUrl());
        } catch (MalformedURLException e) {
            throw new FactoryRuntimeException("Failed to connect to appium on url " + PageFactory.getInitialUrl(), e);
        }

        setAspectsDisabled(true);
        mobileDriver = new AndroidDriver<>(url, capabilities);
    }
    
    public static void dispose() {
        if (mobileDriver != null) {
            mobileDriver.quit();
        }
    }
}
