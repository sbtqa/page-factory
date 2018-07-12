package ru.sbtqa.tag.pagefactory.aspects;

import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import ru.sbtqa.tag.datajack.TestDataObject;
import ru.sbtqa.tag.datajack.exceptions.DataException;
import ru.sbtqa.tag.pagefactory.support.DataProvider;

@Aspect
public class DataAspect {

    private String featureDataTag;
    private String currentScenarioTag;

    @Around("call(* *.addChildren(..))")
    public void stash(ProceedingJoinPoint joinPoint) throws Throwable {
        List<CucumberFeature> cucumberFeatures = (List<CucumberFeature>) joinPoint.getArgs()[0];
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            GherkinDocument gherkinDocument = cucumberFeature.getGherkinFeature();
            Feature feature = gherkinDocument.getFeature();
            featureDataTag = parseTags(feature.getTags());
            List<ScenarioDefinition> featureChildren = feature.getChildren();
            for (ScenarioDefinition scenarioDefinition : featureChildren) {
                List<Tag> currentScenarioTags = getScenarioTags(scenarioDefinition);
                currentScenarioTag = parseTags(currentScenarioTags);
                List<Step> steps = scenarioDefinition.getSteps();
                for (Step step : steps) {
                    FieldUtils.writeField(step, "text", parseString(step.getText()), true);
                }

            }
        }

        joinPoint.proceed();
    }

    private List<Tag> getScenarioTags(ScenarioDefinition scenarioDefinition) {
        try {
            return (List<Tag>) FieldUtils.readField(scenarioDefinition, "tags", true);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return new ArrayList<>();
        }
    }

    private String parseTags(List<Tag> tags) throws DataException {
        Optional<Tag> dataTag = tags.stream().filter(predicate -> predicate.getName().startsWith("@data")).findFirst();
        return dataTag.isPresent() ? dataTag.get().getName().split("=")[1].trim() : null;
    }

    private String parseString(String raw) throws DataException {
        Pattern dataP = Pattern.compile("(?:(@[^\\$]+)?(\\$\\{[^\\}]+\\}))+");
        Matcher m = dataP.matcher(raw);
        StringBuffer sb = new StringBuffer(raw);
        int sbSkip = 0;
        while (m.find()) {
            String collection = m.group(1);

            String value = m.group(2);

            if (value == null) {
                continue;
            }
            if (collection != null) {
                DataProvider.updateCollection(DataProvider.getInstance().fromCollection(collection.replace("@", "")));
                sb = sb.replace(m.start(1) + sbSkip, m.end(1) + sbSkip, "");
                sbSkip += "".length() - collection.length();
            } else {
                String tag = currentScenarioTag != null ? currentScenarioTag : featureDataTag;
                if (tag != null) {
                    parseTestDataObject(tag);
                }
            }

            String dataPath = value.replace("${", "").replace("}", "");
            String parsedValue = DataProvider.getInstance().get(dataPath).getValue();
            sb = sb.replace(m.start(2) + sbSkip, m.end(2) + sbSkip, parsedValue);
            sbSkip += parsedValue.length() - value.length();

        }
        return sb.toString();
    }

    private void parseTestDataObject(String raw) throws DataException {
        Pattern dataP = Pattern.compile("([^\\$]+)(?:\\$\\{([^\\}]+)\\})?");
        Matcher m = dataP.matcher(raw.trim());

        if (m.matches()) {
            String collection = m.group(1);
            String value = m.group(2);

            TestDataObject tdo = DataProvider.getInstance().fromCollection(collection);

            if (value != null) {
                tdo = tdo.get(value);
            }

            DataProvider.updateCollection(tdo);
        }
    }
}
