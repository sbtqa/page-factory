package ru.sbtqa.tag.pagefactory.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.WebElement;
import ru.sbtqa.tag.pagefactory.extensions.WebExtension;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;

@Aspect
public class SendKeysAspect {

    @Around("call(* org.openqa.selenium.WebElement.sendKeys(..)) || call(* ru.yandex.qatools.htmlelements.element.*.sendKeys(..))")
    public void doAroundClick(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();

        if (target instanceof TypifiedElement || target instanceof WebElement) {
            blink(joinPoint);
        } else {
            joinPoint.proceed();
        }

    }

    private void blink(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean isVideoHighlightEnabled = Boolean.valueOf(Props.get("video.highlight.enabled"));
        String elementHighlightStyle = null;

        Object target = joinPoint.getTarget();
        WebElement element = target instanceof WebElement
                ? (WebElement) joinPoint.getTarget()
                : ((TypifiedElement) joinPoint.getTarget()).getWrappedElement();

        if (isVideoHighlightEnabled) {
            elementHighlightStyle = WebExtension.highlightElementOn(element);
        }

        joinPoint.proceed();

        if (isVideoHighlightEnabled) {
            WebExtension.highlightElementOff(element, elementHighlightStyle);
        }
    }

}
