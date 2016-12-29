package ru.sbtqa.tag.pagefactory.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import ru.sbtqa.tag.datajack.Stash;
import ru.sbtqa.tag.pagefactory.DriverExtensions;
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;

@Aspect
public class ClickAspect {

    /**
     * <p>
     * clickMethod.</p>
     */
    @Pointcut("call(* org.openqa.selenium.WebElement.click()) || call(* ru.yandex.qatools.htmlelements.element.*.click()) ")
    public void clickMethod() {
    }

    /**
     * <p>
     * doAroundClick.</p>
     *
     * @param joinPoint a {@link org.aspectj.lang.ProceedingJoinPoint} object.
     * @throws java.lang.Throwable if any.
     */
    @Around("clickMethod()")
    public void doAroundClick(ProceedingJoinPoint joinPoint) throws Throwable {
        String elementHighlightStyle = null;
        WebElement targetWebElement;
        Class<? extends Page> elementRedirect;

        if (joinPoint.getTarget() instanceof TypifiedElement) {
            targetWebElement = ((TypifiedElement) joinPoint.getTarget()).getWrappedElement();

            TypifiedElement typifiedElement = (TypifiedElement) joinPoint.getTarget();
            elementRedirect = PageFactory.getInstance().getCurrentPage().getElementRedirect(typifiedElement);

        } else if (joinPoint.getTarget() instanceof WebElement) {
            targetWebElement = (WebElement) joinPoint.getTarget();

            elementRedirect = PageFactory.getInstance().getCurrentPage().getElementRedirect(targetWebElement);

        } else {
            return;
        }

        if (Boolean.valueOf(Props.get("video.highlight.enable"))) {
            elementHighlightStyle = DriverExtensions.highlightElementOn(targetWebElement);
        }

        Stash.put("beforeClickHandles", PageFactory.getWebDriver().getWindowHandles());

//            boolean isReinitNeeded = ExperianModel.getElementRePageFactory(targetWebElement, PageFactory.getInstance().currentPage);
        if (!PageFactory.isAspectsDisabled()) {
            Actions actions = new Actions(PageFactory.getWebDriver());
            //TODO: для андроеда тут все выполнялось в цикле. Надо если что поставить иф
            if ("IE".equals(PageFactory.getBrowserName())) {
                Dimension size = PageFactory.getWebDriver().manage().window().getSize();
                Point elementLocation = (targetWebElement).getLocation();
                Dimension elementSize = (targetWebElement).getSize();
                //Если элемент находится за пределами видимой области, включаем скролл
                if (size.getHeight() < (elementLocation.getY() + elementSize.getHeight() + 200)) {
                    ((JavascriptExecutor) PageFactory.getWebDriver()).
                            executeScript("window.scroll(" + elementLocation.getX() + ","
                                    + (elementLocation.getY() - 200) + ");");
                }
            }

            switch (PageFactory.getBrowserName()) {
                case "Chrome":
                case "IE":
                    actions.moveToElement(targetWebElement);
                    actions.click();
                    actions.build().perform();
                    break;
                default:
                    joinPoint.proceed();
            }
        } else {
            joinPoint.proceed();
        }

        if (null != elementRedirect) {
            PageFactory.getInstance().getPage(elementRedirect);
        }
//            if (isReinitNeeded) {
//                PageFactory.getDriverExtensions().waitForPageToLoad();
//                ExperianModel.initElements(PageFactory.getWebDriver(), PageFactory.getInstance().currentPage);
//            }

        if (Boolean.valueOf(Props.get("video.highlight.enable"))) {
            DriverExtensions.highlightElementOff(targetWebElement, elementHighlightStyle);
        }
    }

}
