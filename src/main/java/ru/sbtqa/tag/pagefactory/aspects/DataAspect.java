package ru.sbtqa.tag.pagefactory.aspects;

import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import ru.sbtqa.tag.datajack.TestDataObject;
import ru.sbtqa.tag.datajack.exceptions.DataException;
import ru.sbtqa.tag.pagefactory.support.DataProvider;

@Aspect
public class DataAspect {

    String parseString(String raw) throws DataException {
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
                DataProvider.updateCollection(DataProvider.getInstance().fromCollection(DataProvider.getConfigCollection()));
            }
            TestDataObject deepObj = DataProvider.getInstance().get("my.long.super.path.1.2.3.4.5");// ->name
            TestDataObject deepObj2 = DataProvider.getInstance().get("my.long2.super.path.1.2.3.4.6");// -> surname
            
            
            deepObj.get("name").getValue();
            deepObj2.get("surname").getValue();
            
            
            
            String dataPath = value.replace("${", "").replace("}", "");
            String parsedValue = DataProvider.getInstance().get(dataPath).getValue();
            sb = sb.replace(m.start(2) + sbSkip, m.end(2) + sbSkip, parsedValue);
            sbSkip += parsedValue.length() - value.length();

        }
        return sb.toString();
    }

    @Pointcut("call(* *.addChildren(..))")
    public void addChildren() {
    }

    @Around("addChildren()")
    public void stash(ProceedingJoinPoint joinPoint) throws Throwable {
        List<CucumberFeature> cucumberFeatures = (List<CucumberFeature>) joinPoint.getArgs()[0];
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            GherkinDocument gherkinDocument = cucumberFeature.getGherkinFeature();
            Feature feature = gherkinDocument.getFeature();
            List<ScenarioDefinition> featureChildren = feature.getChildren();
            for (ScenarioDefinition scenarioDefinition : featureChildren) {
                List<Step> steps = scenarioDefinition.getSteps();
                for (Step step : steps) {
                    FieldUtils.writeField(step, "text", parseString(step.getText()), true);
                }

            }
        }

        joinPoint.proceed();
    }

}
