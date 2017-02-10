package ru.sbtqa.tag.pagefactory.exceptions;

public class SwipeException extends PageException {

    /**
     * 
     * @param e a {@link java.lang.Throwable} object.
     */
    public SwipeException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public SwipeException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public SwipeException(String message) {
        super(message);
    }

}
