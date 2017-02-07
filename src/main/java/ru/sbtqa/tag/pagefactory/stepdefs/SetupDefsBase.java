package ru.sbtqa.tag.pagefactory.stepdefs;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebElement;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.qautils.reflect.ClassUtilsExt;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

public class SetupDefsBase {

    private static final Logger log = LoggerFactory.getLogger(SetupDefsBase.class);

    @Before()
    public void setUp() {
        //try to connect logger property file if exists
        String path = "src/test/resources/config/log4j.properties";
        if (new File(path).exists()) {
            PropertyConfigurator.configure(path);
            log.info("Log4j proprties were picked up on the path " + path);
        } else {
            log.warn("There is no log4j.properties on the path " + path);
        }

        try {
            //TODO fix if task.to.kill does not exist in application.properties
            String[] tasks = Props.get("tasks.to.kill").split(",");
            if (tasks.length > 0) {
                for (String task : tasks) {
                    Runtime.getRuntime().exec("taskkill /IM " + task.trim() + " /F");
                }
            }
        } catch (Exception e) {
            log.debug("Failed to kill one of task to kill", e);
        }

        Reflections reflections;
        PageFactory.getWebDriver();
        PageFactory.getInstance();
        reflections = new Reflections(PageFactory.getPagesPackage());

        Collection<String> allClassesString = reflections.getStore().get("SubTypesScanner").values();
        Set<Class<?>> allClasses = new HashSet();
        for (String clazz : allClassesString) {
            try {
                allClasses.add(Class.forName(clazz));
            } catch (ClassNotFoundException e) {
                log.warn("Cannot add all classes to set from package storage", e);
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
    }

    @After
    public void tearDown() {
        PageFactory.dispose();
    }
}
