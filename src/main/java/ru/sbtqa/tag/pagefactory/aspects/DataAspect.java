package ru.sbtqa.tag.pagefactory.aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import ru.sbtqa.tag.datajack.exceptions.DataException;
import ru.sbtqa.tag.pagefactory.support.DataProvider;

@Aspect
public class DataAspect {

    @Pointcut("call(* de.rm.trial.mynew.stepdefs.*(..))")
    public void parseData() {

    }

    String parseString(String raw) throws DataException {
        Pattern dataP = Pattern.compile("(?:(#[^\\$]+)?(\\$\\{[^\\}]+\\}))+");
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
                DataProvider.updateCollection(DataProvider.getInstance().fromCollection(collection.replace("#", "")));
                sb = sb.replace(m.start(1) + sbSkip, m.end(1) + sbSkip, "");
                sbSkip += "".length() - collection.length();
            }

            String dataPath = value.replace("${", "").replace("}", "");
            String parsedValue = DataProvider.getInstance().get(dataPath).getValue();
            sb = sb.replace(m.start(2) + sbSkip, m.end(2) + sbSkip, parsedValue);
            sbSkip += parsedValue.length() - value.length();

        }
        return sb.toString();
    }

    @Around("execution(* *..*(..)) && !within(ru.sbtqa.tag.pagefactory.aspects.*)")
    public Object around(ProceedingJoinPoint joinPoint) throws DataException, Throwable {
        try {
            Object[] args = joinPoint.getArgs();
            List<Object> newO = new ArrayList<>();

            for (Object arg1 : args) {
                if (arg1 instanceof String) {
                    String arg = (String) arg1;
                    newO.add(parseString(arg));
                } else {
                    newO.add(arg1);
                }
            }
            return joinPoint.proceed(newO.toArray());
        } catch (Throwable t) {
            return joinPoint.proceed();
        }
    }
}
