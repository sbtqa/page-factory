package ru.sbtqa.tag.stepdefs;

import cucumber.api.Scenario;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebElement;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.allurehelper.Type;
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.ScenarioContext;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.drivers.TagWebDriver;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.pagefactory.support.ScreenShooter;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.qautils.reflect.ClassUtilsExt;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.sbtqa.tag.videorecorder.VideoRecorder;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

public class SetupSteps {

    private static final ThreadLocal<Boolean> isSetUp = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    private static final ThreadLocal<Boolean> isTearDown = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(SetupSteps.class);

    public void setUp(Scenario scenario) {

        if (isAlreadyPerformed(isSetUp)) {
            return;
        }

        ScenarioContext.setScenario(scenario);
        //try to connect logger property file if exists
        String path = "src/test/resources/config/log4j.properties";
        if (new File(path).exists()) {
            PropertyConfigurator.configure(path);
            LOG.info("Log4j properties were picked up on the path {}", path);
        } else {
            LOG.warn("There is no log4j.properties on the path {}", path);
        }

        try {
            String tasksToKill = Props.get("tasks.to.kill");
            if (!PageFactory.isSharingProcessing() && !"".equals(tasksToKill)) {
                for (String task : tasksToKill.split(",")) {
                    if (SystemUtils.IS_OS_WINDOWS) {
                        Runtime.getRuntime().exec("taskkill /IM " + task.trim() + " /F");
                    } else {
                        Runtime.getRuntime().exec("killall " + task.trim());
                    }
                }
            }
        } catch (IOException e) {
            LOG.debug("Failed to kill one of task to kill", e);
        }

        String aspectDisabled = Props.get("page.aspect.disabled");
        if (!"".equals(aspectDisabled)) {
            PageFactory.setAspectsDisabled(Boolean.parseBoolean(aspectDisabled));
        }

        Reflections reflections;
        reflections = new Reflections(PageFactory.getPagesPackage());

        Collection<String> allClassesString = reflections.getStore().get("SubTypesScanner").values();
        Set<Class<?>> allClasses = new HashSet<>();
        for (String clazz : allClassesString) {
            try {
                allClasses.add(Class.forName(clazz));
            } catch (ClassNotFoundException e) {
                LOG.warn("Cannot add all classes to set from package storage", e);
            }
        }

        for (Class<?> page : allClasses) {
            List<Class> supers = ClassUtilsExt.getSuperclassesWithInheritance(page);
            if (!supers.contains(Page.class) && !supers.contains(HtmlElement.class)) {
                if (page.getName().contains("$")) {
                    continue; //We allow private additional classes but skip it if its not extends Page
                } else {
                    throw new FactoryRuntimeException("Class " + page.getName() + " is not extended from Page class. Check you webdriver.pages.package property.");
                }
            }
            List<Field> fields = FieldUtilsExt.getDeclaredFieldsWithInheritance(page);
            Map<Field, String> fieldsMap = new HashMap<>();
            for (Field field : fields) {
                Class<?> fieldType = field.getType();
                if (fieldType.equals(WebElement.class)) {

                    ElementTitle titleAnnotation = field.getAnnotation(ElementTitle.class);
                    if (titleAnnotation != null) {
                        fieldsMap.put(field, titleAnnotation.value());
                    } else {
                        fieldsMap.put(field, field.getName());
                    }
                }
            }

            PageFactory.getPageRepository().put((Class<? extends Page>) page, fieldsMap);
        }

        if (PageFactory.isVideoRecorderEnabled()) {
            VideoRecorder.getInstance().startRecording();
        }
    }

    public void tearDown() {

        if (isAlreadyPerformed(isTearDown)) {
            return;
        }

        attachScreenshotToReport();

        if (PageFactory.isVideoRecorderEnabled() && VideoRecorder.getInstance().isVideoStarted()) {
            ParamsHelper.addParam("Video url", VideoRecorder.getInstance().stopRecording());
            VideoRecorder.getInstance().resetVideoRecorder();
        }

        if (PageFactory.getEnvironment() == Environment.WEB && TagWebDriver.isWebDriverShared()) {
            LOG.info("Webdriver sharing is processing...");
            PageFactory.setSharingProcessing(true);
        } else {
            PageFactory.dispose();
        }
    }

    private synchronized boolean isAlreadyPerformed(ThreadLocal<Boolean> t) {
        if (t.get()) {
            return true;
        } else {
            t.set(true);
            if (t.equals(isSetUp)) {
                isTearDown.remove();
            } else if (t.equals(isTearDown)) {
                isSetUp.remove();
            }
            return false;
        }
    }

    private void attachScreenshotToReport() {
        boolean isScenarioFailed = ScenarioContext.getScenario().isFailed();
        if (isScenarioFailed && PageFactory.isDriverInitialized()) {
            ParamsHelper.addAttachmentToRender(ScreenShooter.take(), "Screenshot", Type.PNG);
        }
    }
}
