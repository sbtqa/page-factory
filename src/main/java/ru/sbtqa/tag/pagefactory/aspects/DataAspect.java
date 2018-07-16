package ru.sbtqa.tag.pagefactory.aspects;

import cucumber.runtime.model.CucumberFeature;
import java.util.List;
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

        dataParser.replaceDataPlaceholders(cucumberFeatures);
        joinPoint.proceed();
    }

}
