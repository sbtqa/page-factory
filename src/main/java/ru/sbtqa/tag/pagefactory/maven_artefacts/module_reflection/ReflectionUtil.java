package ru.sbtqa.tag.pagefactory.maven_artefacts.module_reflection;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.cucumber.TagCucumber;
import ru.sbtqa.tag.pagefactory.WebElementsPage;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.annotations.ActionTitle;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.annotations.ActionTitles;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.annotations.RedirectsTo;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.exceptions.ElementDescriptionException;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.exceptions.ElementNotFoundException;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.exceptions.PageException;
import ru.sbtqa.tag.qautils.i18n.I18N;
import ru.sbtqa.tag.qautils.i18n.I18NRuntimeException;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common reflection utils as static methods from Page.Core
 *
 * May be it should be in entry_point module
 *
 */
public class ReflectionUtil {
    
    public static final Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);
    
    
    /**
     * Return a list of methods declared tin the given class and its super
     * classes
     *
     * @param clazz class to check
     * @return list of methods. could be empty list
     */
    public static List<Method> getDeclaredMethods(Class clazz) {
        List<Method> methods = new ArrayList<>();
        
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        
        Class supp = clazz.getSuperclass();
        
        while (supp != java.lang.Object.class) {
            methods.addAll(Arrays.asList(supp.getDeclaredMethods()));
            supp = supp.getSuperclass();
        }
        
        return methods;
    }
    
    /**
     * Check whether given method has {@link ActionTitle} or
     * {@link ActionTitles} annotation with required title
     *
     * @param method method to check
     * @param title required title
     * @return true|false
     */
    public static Boolean isRequiredAction(Method method, final String title) {
        ActionTitle actionTitle = method.getAnnotation(ActionTitle.class);
        ActionTitles actionTitles = method.getAnnotation(ActionTitles.class);
        List<ActionTitle> actionList = new ArrayList<>();
        
        if (actionTitles != null) {
            actionList.addAll(Arrays.asList(actionTitles.value()));
        }
        if (actionTitle != null) {
            actionList.add(actionTitle);
        }
        
        for (ActionTitle action : actionList) {
            String actionValue = action.value();
            try {
                I18N i18n = I18N.getI18n(method.getDeclaringClass(), TagCucumber.getFeature().getI18n().getLocale(), I18N.DEFAULT_BUNDLE_PATH);
                actionValue = i18n.get(action.value());
            } catch (I18NRuntimeException e) {
                LOG.debug("There is no bundle for translation class. Leave it as is", e);
            }
            
            if (actionValue.equals(title)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check whether given field is a child of specified class
     *
     * @param parent class that is supposed to be parent
     * @param field field to check
     * @return true|false
     */
    public static boolean isChildOf(Class<?> parent, Field field) {
        Class<?> fieldType = field.getType();
        while (fieldType != null && fieldType != Object.class) {
            if (fieldType == parent) {
                return true;
            }
            fieldType = fieldType.getSuperclass();
        }
        
        return false;
    }
    
    
    /**
     * Find list of elements of the specified type with required title in
     * the given context. Context is either a page object itself, or a block
     * on the page. !BEWARE! field.get() will actually query browser to
     * evaluate the list, so this method might reduce performance!
     *
     * @param listTitle value of ElementTitle annotation of required element
     * @param type type of elements inside of the list
     * @param context object where search should be performed
     * @param <T> type of elements in returned list
     * @return list of WebElement's or its derivatives
     * @throws PageException if didn't find any list or current page wasn't
     * initialized
     */
    @SuppressWarnings("unchecked")
    public static <T extends WebElement> List<T> findListOfElements(String listTitle, Class<T> type, Object context)
            throws PageException {
        for (Field field : FieldUtilsExt.getDeclaredFieldsWithInheritance(context.getClass())) {
            if (isRequiredElement(field, listTitle) && List.class.isAssignableFrom(field.getType())
                    && field.getGenericType() instanceof ParameterizedType
                    && ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].equals(type)) {
                field.setAccessible(true);
                try {
                    return (List<T>) field.get(context);
                } catch (IllegalAccessException e) {
                    throw new FactoryRuntimeException(
                            String.format("Internal error during attempt to find list '%s'", listTitle), e);
                }
            }
        }
        throw new ElementNotFoundException(String.format("Couldn't find elements list '%s' on page '%s'", listTitle, PageFactory
                .getInstance().getCurrentPageTitle()));
    }
    
    /**
     * Check whether {@link ElementTitle} annotation of the field has a
     * required value
     *
     * @param field field to check
     * @param title value of ElementTitle annotation of required element
     * @return true|false
     */
    public static boolean isRequiredElement(Field field, String title) {
        return getFieldTitle(field).equals(title);
    }
    
    /**
     * Return value of {@link ElementTitle} annotation for the field. If
     * none present, return empty string
     *
     * @param field field to check
     * @return either an element title, or an empty string
     */
    public static String getFieldTitle(Field field) {
        for (Annotation a : field.getAnnotations()) {
            if (a instanceof ElementTitle) {
                return ((ElementTitle) a).value();
            }
        }
        return "";
    }
    
    /**
     * Search for the given given element among the parent object fields,
     * check whether it has a {@link
     * RedirectsTo} annotation, and return a redirection page class, if so.
     * Search goes in recursion if it meets HtmlElement field, as given
     * element could be inside of the block
     *
     * @param element element that is being checked for redirection
     * @param parent parent object
     * @return class of the page, this element redirects to
     */
    public static Class<? extends WebElementsPage> findRedirect(Object parent, Object element) {
        List<Field> fields = FieldUtilsExt.getDeclaredFieldsWithInheritance(parent.getClass());
        
        for (Field field : fields) {
            RedirectsTo redirect = field.getAnnotation(RedirectsTo.class);
            if (redirect != null) {
                try {
                    field.setAccessible(true);
                    Object targetField = field.get(parent);
                    if (targetField != null) {
                        if (targetField == element) {
                            return redirect.page();
                        }
                    }
                } catch (NoSuchElementException | StaleElementReferenceException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    LOG.debug("Failed to get page destination to redirect for element", ex);
                }
            }
            if (isChildOf(HtmlElement.class, field)) {
                field.setAccessible(true);
                Class<? extends WebElementsPage> redirects = null;
                try {
                    redirects = findRedirect(field.get(parent), element);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOG.debug("Failed to get page destination to redirect for html element", ex);
                }
                if (redirects != null) {
                    return redirects;
                }
            }
        }
        return null;
    }
    
    /**
     * Get object from a field of specified parent
     *
     * @param parentObject object that contains(must contain) given field
     * @param field field to get
     * @param <T> supposed type of the field. if field cannot be cast into
     * this type, it will fail
     * @return element of requested type
     * @throws ElementDescriptionException in case if field does not belong
     * to the object, or element could not be cast to specified type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getElementByField(Object parentObject, Field field) throws ElementDescriptionException {
        field.setAccessible(true);
        Object element;
        try {
            element = field.get(parentObject);
            return (T) element;
        } catch (IllegalArgumentException | IllegalAccessException iae) {
            throw new ElementDescriptionException("Specified parent object is not an instance of the class or "
                    + "interface, declaring the underlying field: '" + field + "'", iae);
        } catch (ClassCastException cce) {
            throw new ElementDescriptionException("Requested type is incompatible with field '" + field.getName()
                    + "' of '" + parentObject.getClass().getCanonicalName() + "'", cce);
        }
    }
    
    /**
     * Get title annotation of specified WebElement, and add it as a
     * parameter to allure report results, with corresponding value. If
     * there is no title annotation, log warning and exit
     *
     * @param webElement WebElement to add
     * @param text value for the specified element
     */
    public static void addToReport(WebElement webElement, String text) {
        try {
            String elementTitle = PageFactory.getInstance().getCurrentPage().getElementTitle(webElement);
            addToReport(elementTitle, text);
        } catch (PageException e) {
            LOG.warn("Failed to add element " + webElement + " to report", e);
        }
    }
    
    /**
     * Add parameter to allure report.
     *
     * @param paramName parameter name
     * @param paramValue parameter value to set
     */
    public static void addToReport(String paramName, String paramValue) {
        ParamsHelper.addParam(paramName, paramValue);
        LOG.debug("Add '" + paramName + "->" + paramValue + "' to report for page '"
                + PageFactory.getInstance().getCurrentPageTitle() + "'");
    }
}
 

