package ru.sbtqa.tag.pagefactory.support;

import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Perform basic operation by robot.
 */
public class RobotUtil {

    private RobotUtil() {
    }

    /**
     * Fill a http-login popup.
     */
    public static void handleHttpLoginPopUp(String login, String password) {
        try {
            Robot robot = new Robot();
            StringSelection stringSelection;
            Clipboard clipboard;

            stringSelection = new StringSelection(login);
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            SECONDS.sleep(1);

            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);

            SECONDS.sleep(1);

            stringSelection = new StringSelection(password);
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            SECONDS.sleep(1);

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        } catch (AWTException | InterruptedException e) {
            throw new FactoryRuntimeException("An Exception occurred during http authorization by robot.", e);
        }
    }
}