package ru.sbtqa.tag.pagefactory.exceptions;

public class PageInitializationException extends PageException {

    /**
     *
     * @param e a {@link java.lang.Throwable} object.
     */
    public PageInitializationException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public PageInitializationException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public PageInitializationException(String message) {
        super(message);
    }

}
