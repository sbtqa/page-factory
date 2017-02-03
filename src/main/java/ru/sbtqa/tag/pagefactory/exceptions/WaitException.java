package ru.sbtqa.tag.pagefactory.exceptions;

import ru.sbtqa.tag.qautils.errors.AutotestError;

public class WaitException extends AutotestError {

    /**
     *
     * @param e a {@link java.lang.Throwable} object.
     */
    public WaitException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public WaitException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public WaitException(String message) {
        super(message);
    }

}
