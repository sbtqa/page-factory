package ru.sbtqa.tag.pagefactory;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.annotations.PageEntry;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.qautils.properties.Props;

public class PageShell {

    private static final Logger log = LoggerFactory.getLogger(PageShell.class);

    private String currentPageTitle;
    private Page currentPage;

    private final String pagesPackage;

    /**
     *
     * @param pagesPackage a {@link java.lang.String} object.
     */
    public PageShell(String pagesPackage) {
        this.pagesPackage = pagesPackage;
    }

    /**
     * Construct page object by title
     *
     * @param title page title
     * @return page instance
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     */
    public Page getPage(String title) throws PageInitializationException {
        if (null == currentPage || !currentPageTitle.equals(title)) {
            if (null != currentPage) {
                currentPage = getPage(currentPage.getClass().getPackage().getName(), title);
            }
            if (null == currentPage) {
                currentPage = getPage(pagesPackage, title);
            }
            if (null == currentPage) {
                throw new AutotestError("Page object with title '" + title + "' is not registered");
            }
        }
        return currentPage;
    }

    /**
     * Initialize page by class
     *
     * @param page TODO
     * @return TODO
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     */
    public Page getPage(Class<? extends Page> page) throws PageInitializationException {
        return bootstrapPage(page);
    }

    /**
     * <p>
     * Get Page by PageEntry title </p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param title a {@link java.lang.String} object.
     * @return a Page object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     */
    public Page getPage(String packageName, String title) throws PageInitializationException {
        return bootstrapPage(getPageClass(packageName, title));
    }

    /**
     * <p>
     * Getter for the field <code>currentPage</code>.</p>
     *
     * @return a Page object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     */
    public Page getCurrentPage() throws PageInitializationException {
        if (null == currentPage) {
            throw new PageInitializationException(new Exception("Current page not initialized!"));
        } else {
            return currentPage;
        }
    }

    /**
     * Redirect to Page by Page Entry url value
     *
     * @param title a {@link java.lang.String} object.
     * @return a Page object.
     * @throws PageInitializationException TODO
     */
    public Page changeUrlByTitle(String title) throws PageInitializationException {
        if (null != currentPage) {
            currentPage = changeUrlByTitle(currentPage.getClass().getPackage().getName(), title);
        }
        if (null == currentPage) {
            currentPage = changeUrlByTitle(pagesPackage, title);
        }
        if (null == currentPage) {
            throw new AutotestError("Page Object with title " + title + " is not registered");
        }
        return currentPage;
    }

    /**
     * Redirect to Page by Page Entry url value
     *
     * @param packageName a {@link java.lang.String} object.
     * @param title a {@link java.lang.String} object.
     * @return a Page object.
     * @throws ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException TODO
     */
    public Page changeUrlByTitle(String packageName, String title) throws PageInitializationException {

        Class<?> pageClass = getPageClass(packageName, title);
        if (pageClass == null) {
            return null;
        }

        Annotation annotation = pageClass.getAnnotation(PageEntry.class);
        if (annotation != null && !((PageEntry) annotation).url().isEmpty()) {
            if (PageFactory.getWebDriver().getCurrentUrl() == null) {
                throw new AutotestError("Current URL is null");
            } else {
                try {
                    URL currentUrl = new URL(PageFactory.getWebDriver().getCurrentUrl());
                    String finalUrl = currentUrl.getProtocol() + "://" + currentUrl.getAuthority() + getUrlPrefix() + ((PageEntry) annotation).url();
                    PageFactory.getWebDriver().navigate().to(finalUrl);
                } catch (MalformedURLException ex) {
                    log.error("Failed to get current url", ex);
                }
            }

            return bootstrapPage(pageClass);
        }

        throw new AutotestError("Page " + title + " doesn't have fast URL in PageEntry");
    }

    /**
     *
     * @return
     */
    private String getUrlPrefix() {
        String prefix = Props.get("webdriver.url.prefix");
        if (!"".equals(prefix)) {
            return ((prefix.startsWith("/")) ? "" : "/")
                    + prefix
                    + ((prefix.endsWith("/")) ? "" : "/");
        }
        return "/";
    }

    /**
     *
     * @param packageName TODO
     * @param title TODO
     * @return
     */
    private Class<?> getPageClass(String packageName, String title) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<Class<?>> allClasses = new HashSet<>();
        try {
            ClassPath.from(loader)
                    .getAllClasses()
                    .stream()
                    .filter((info) -> (info.getName().startsWith(packageName + ".")))
                    .forEach((info) -> {
                        allClasses.add(info.load());
                    });
        } catch (IOException ex) {
            log.warn("Failed to shape class info set", ex);
        }

        for (Class<?> page : allClasses) {
            String pageTitle = null;
            if (null != page.getAnnotation(PageEntry.class)) {
                pageTitle = page.getAnnotation(PageEntry.class).title();
            } else {
                try {
                    pageTitle = (String) FieldUtils.readStaticField(page, "title", true);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    log.debug("Failed to read {} becase it is not page object", pageTitle, ex);
                }
            }
            if (pageTitle != null && pageTitle.equals(title)) {
                return page;
            }
        }

        return null;
    }

    /**
     *
     * @param page TODO
     * @return TODO
     * @throws PageInitializationException TODO
     */
    private Page bootstrapPage(Class<?> page) throws PageInitializationException {
        if (page != null) {
            try {
                @SuppressWarnings("unchecked")
                Constructor<Page> constructor = ((Constructor<Page>) page.getConstructor());
                constructor.setAccessible(true);
                currentPage = constructor.newInstance();
                currentPageTitle = currentPage.getTitle();
                return currentPage;
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new PageInitializationException("Failed to initialize page '" + page + "'", e);
            }
        }
        return null;
    }

    /**
     * @return the currentPageTitle
     */
    public String getCurrentPageTitle() {
        return currentPageTitle;
    }
}
