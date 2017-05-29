package ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api;

import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.exceptions.PageInitializationException;


/**
 * Держит текущий контекст по пейджам в ходе теста
 */
public class PageContext {

    private static String currentPageTitle;
    private static Page currentPage;

    public static String getCurrentPageTitle() {
        return currentPageTitle;
    }

    private static void setCurrentPageTitle(String currentPageTitle) {
        PageContext.currentPageTitle = currentPageTitle;
    }
    
    public static Page getCurrentPage() throws PageInitializationException {
        return currentPage;
    }
    
    public static void setCurrentPage(Page currentPage) {
        PageContext.currentPage = currentPage;
        PageContext.setCurrentPageTitle(currentPage.getPageTitle());
    }
}
