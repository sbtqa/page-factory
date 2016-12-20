package ru.sbtqa.tag.pagefactory.exceptions;

public class WaitException extends Exception {

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
