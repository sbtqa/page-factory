package ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api;

import ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.annotations.PageEntry;

/**
 * Entry point to the page-factory
 */
public class Page {

    /**
     * Get title of current page obect
     *
     * @return the title
     */
    public String getPageTitle() {
        return this.getClass().getAnnotation(PageEntry.class).title();
    }
}
