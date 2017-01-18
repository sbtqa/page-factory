package ru.sbtqa.tag.pagefactory;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.qautils.strategies.DirectionStrategy;

public class MobileExtensions {

    private static final Logger LOG = LoggerFactory.getLogger(MobileExtensions.class);
    private static final int DEFAULT_SWIPE_TIME = 3000;

    /**
     * Swipe element to desired direction
     *
     * @param element element to swipe
     * @param direction swipe direction
     */
    public void swipe(WebElement element, DirectionStrategy direction) {
        swipe(element, direction, DEFAULT_SWIPE_TIME);
    }

    /**
     * Swipe element to desired direction
     *
     * @param element taget element to swipe
     * @param direction swipe direction
     * @param time how fast element should be swiped
     */
    public void swipe(WebElement element, DirectionStrategy direction, int time) {
        int offset;
        int h = element.getSize().getHeight();
        int w = element.getSize().getWidth();
        Point elloc = element.getLocation();

        int startX, endX, startY, endY;
        startX = endX = elloc.getX() + (w / 2) - 1;

        startY = elloc.getY() + (h / 2);
        endY = elloc.getY() + (h / 2);

        switch (direction) {
            case DOWN:
                offset = Math.round(h / 2) - 10;
                startY = startY + offset;
                break;
            case UP:
                offset = Math.round(h / 2);
                endY = endY + offset;
                break;
            case LEFT:
                offset = Math.round(w / 2);
                startX = startX + offset;
                endX = endX - offset / 2;
                break;
            case RIGHT:
                offset = Math.round(w / 2);
                endX = endX + offset;
                break;
//            default:
//                LOG.error("Swipe direction chosen incorrect");
        }

        PageFactory.getMobileDriver().swipe(startX, startY, endX, endY, time);
    }

    /**
     * Swipe screen to direction
     *
     * @param direction swipe direction
     */
    public void swipe(DirectionStrategy direction) {
        swipe(direction, DEFAULT_SWIPE_TIME);
    }

    /**
     * Swipe screen to direction
     *
     * @param direction swipe direction
     * @param time how fast screen should be swiped
     */
    public void swipe(DirectionStrategy direction, int time) {
//        swipingHorizontal
        //Get the size of screen.
        Dimension size = PageFactory.getMobileDriver().manage().window().getSize();

        //Find swipe start and end point from screen's with and height.
        //Find startx point which is at right side of screen.
        int startx = (int) (size.width * 0.70);
        //Find endx point which is at left side of screen.
        int endx = (int) (size.width * 0.30);
        //Find vertical point where you wants to swipe. It is in middle of screen height.
        int starty = size.height / 2;
        System.out.println("startx = " + startx + " ,endx = " + endx + " , starty = " + starty);

        //Swipe from Right to Left.
        PageFactory.getMobileDriver().swipe(startx, starty, endx, starty, 3000);
//        Thread.sleep(2000);

        //Swipe from Left to Right.
        PageFactory.getMobileDriver().swipe(endx, starty, startx, starty, 3000);
//        Thread.sleep(2000);
        
        
        
        
        
        
        

//        PageFactory.getMobileDriver().swipe(startX, startY, endX, endY, time);
    }

}
