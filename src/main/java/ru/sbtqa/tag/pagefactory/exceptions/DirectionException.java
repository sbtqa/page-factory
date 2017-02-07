package ru.sbtqa.tag.pagefactory.exceptions;

public class DirectionException extends Exception {

    /**
     *
     * @param e a {@link java.lang.Throwable} object.
     */
    public DirectionException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public DirectionException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public DirectionException(String message) {
        super(message);
    }

}
