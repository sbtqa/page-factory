package ru.sbtqa.tag.pagefactory.aspects;

import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import ru.sbtqa.tag.pagefactory.support.data.DataParser;

@Aspect
public class DataAspect {

    @Around("call(* cucumber.api.junit.Cucumber.addChildren(..))")
    public void replaceDataPlaceholders(ProceedingJoinPoint joinPoint) throws Throwable {
        DataParser dataParser = new DataParser();
        List<CucumberFeature> cucumberFeatures = (List<CucumberFeature>) joinPoint.getArgs()[0];

        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            GherkinDocument gherkinDocument = cucumberFeature.getGherkinFeature();
            Feature feature = gherkinDocument.getFeature();

            dataParser.setFeatureDataTag(dataParser.parseTags(feature.getTags()));
            List<ScenarioDefinition> featureChildren = feature.getChildren();

            for (ScenarioDefinition scenarioDefinition : featureChildren) {
                List<Tag> currentScenarioTags = dataParser.getScenarioTags(scenarioDefinition);
                dataParser.setCurrentScenarioTag(dataParser.parseTags(currentScenarioTags));
                List<Step> steps = scenarioDefinition.getSteps();

                for (Step step : steps) {
                    FieldUtils.writeField(step, "text", dataParser.parseString(step.getText()), true);
                }
            }
        }
        joinPoint.proceed();
    }

}
