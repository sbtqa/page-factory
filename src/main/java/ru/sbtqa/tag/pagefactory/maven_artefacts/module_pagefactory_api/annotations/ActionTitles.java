package ru.sbtqa.tag.pagefactory.maven_artefacts.module_pagefactory_api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionTitles {

    /**
     *
     * @return TODO
     */
    ActionTitle[] value();
}
