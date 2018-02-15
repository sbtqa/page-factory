package ru.sbtqa.tag.pagefactory.support;

import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import io.qameta.allure.cucumberjvm.AllureCucumberJvm;
import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.allurehelper.Type;
import ru.sbtqa.tag.cucumber.TagCucumber;
import ru.sbtqa.tag.pagefactory.PageFactory;

public class TagAllureReporter extends AllureCucumberJvm {

    private static final String FAILED = "failed";
    private static final String SCREENSHOT = "Screenshot";
    
    @Override
    public String getStepName(Step step) {
        return step.getName().split(TagCucumber.SECRET_DELIMITER).length > 1
                ? step.getName().split(TagCucumber.SECRET_DELIMITER)[1]
                : step.getName();
    }

    @Override
    public void result(final Result result) {
        super.result(result);
        if (FAILED.equals(result.getStatus()) && PageFactory.isDriverInitialized()) {
            ParamsHelper.addAttachment(ScreenShooter.take(), SCREENSHOT, Type.PNG);
        }
    }
}
