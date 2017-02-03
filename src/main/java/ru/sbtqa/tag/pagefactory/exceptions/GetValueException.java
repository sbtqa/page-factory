package ru.sbtqa.tag.pagefactory.exceptions;

public class GetValueException extends Exception {

    /**
     *
     * @param e a {@link java.lang.Throwable} object.
     */
    public GetValueException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public GetValueException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public GetValueException(String message) {
        super(message);
    }

}
