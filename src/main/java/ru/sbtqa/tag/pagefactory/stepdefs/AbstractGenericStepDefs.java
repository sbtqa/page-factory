package ru.sbtqa.tag.pagefactory.stepdefs;

public class AbstractGenericStepDefs {

    public String getLanguage() {
        String[] packages = this.getClass().getCanonicalName().split("\\.");
        String currentLanguage = packages[packages.length - 2];
        
        return currentLanguage;
    }
}
