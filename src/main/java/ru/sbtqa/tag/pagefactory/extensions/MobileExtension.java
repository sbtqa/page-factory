package ru.sbtqa.tag.pagefactory.extensions;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.qautils.strategies.DirectionStrategy;

public class MobileExtension {
    
    private static final Logger LOG = LoggerFactory.getLogger(MobileExtension.class);
    private static final int DEFAULT_SWIPE_TIME = 3000;

    /**
     * Swipe element to direction
     *
     * @param element element to swipe
     * @param direction swipe direction
     */
    public void swipe(WebElement element, DirectionStrategy direction) {
	swipe(element, direction, DEFAULT_SWIPE_TIME);
    }

    /**
     * Swipe element to direction
     *
     * @param element taget element to swipe
     * @param direction swipe direction
     * @param time how fast element should be swiped
     */
    public void swipe(WebElement element, DirectionStrategy direction, int time) {
	Dimension size = element.getSize();
	Point location = element.getLocation();
	swipe(location, size, direction, time);
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
	Dimension size = PageFactory.getMobileDriver().manage().window().getSize();
	swipe(new Point(0, 0), size, direction, time);
    }

    /**
     * Swipe to direction
     *
     * @param location top left-hand corner of the element
     * @param size width and height of the element
     * @param direction swipe direction
     * @param time how fast screen should be swiped
     */
    private void swipe(Point location, Dimension size, DirectionStrategy direction, int time) {
	int startx, endx, starty, endy;
	switch (direction) {
	    case DOWN:
		startx = endx = size.width / 2;
		starty = (int) (size.height * 0.80);
		endy = (int) (size.height * 0.20);
		break;
	    case UP:
		startx = endx = size.width / 2;
		starty = (int) (size.height * 0.20);
		endy = (int) (size.height * 0.80);
		break;
	    case RIGHT:
		startx = (int) (size.width * 0.70);
		endx = (int) (size.width * 0.30);
		starty = endy = size.height / 2;
		break;
	    case LEFT:
		startx = (int) (size.width * 0.30);
		endx = (int) (size.width * 0.70);
		starty = endy = size.height / 2;
		break;
	    default:
		throw new FactoryRuntimeException("Failed to swipe to direction " + direction);
	}
	
	int x = location.getX();
	int y = location.getY();
	LOG.debug("Swipe parameters: location {}, dimension {}, direction {}, time {}", location, size, direction, time);
	PageFactory.getMobileDriver().swipe(x + startx, y + starty, x + endx, y + endy, time);
    }
}
