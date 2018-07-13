package ru.sbtqa.tag.pagefactory.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.qautils.errors.AutotestError;

@Aspect
/**
 * Aspect for translation an Exceptions to AssertationError errors <br/>
 * to split infrastructure errors from functional errors
 *
 *
 * @version $Id: $Id
 */
public class ExceptionAspect {

    static long lastFailureTimestamp = 0;

    /**
     * <p>
     * translateException.</p>
     *
     * @param joinPoint a {@link org.aspectj.lang.ProceedingJoinPoint} object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Throwable if any.
     */
    @Around("execution(* *..*(..))")
    public Object translateException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (AssertionError | FactoryRuntimeException e) {
            if (!(e instanceof AutotestError)) {
                throw new AutotestError(getErrorText(e), e);
            } else {
                throw e;
            }
        }
    }

    private String getErrorText(Throwable throwMessage) throws PageInitializationException, IllegalArgumentException, IllegalAccessException {
        String errorText = throwMessage.getCause() != null ? throwMessage.getCause().getMessage() : throwMessage.getMessage();

        Field[] fields = PageFactory.getInstance().getCurrentPage().getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            Object currentObject = null;
            if (PageFactory.getInstance().getCurrentPage() != null) {
                currentObject = field.get(PageFactory.getInstance().getCurrentPage());
            }

            if (null != currentObject && throwMessage.getMessage().contains(field.getName())) {
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation instanceof ElementTitle) {
                        errorText = "There is no element with title == " + ((ElementTitle) annotation).value();
                        break;
                    }
                }
            }
        }

        return errorText;
    }

}
