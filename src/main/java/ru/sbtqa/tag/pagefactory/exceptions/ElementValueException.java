package ru.sbtqa.tag.pagefactory.exceptions;

public class ElementValueException extends Exception {

    /**
     *
     * @param e a {@link java.lang.Throwable} object.
     */
    public ElementValueException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public ElementValueException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public ElementValueException(String message) {
        super(message);
    }

}
