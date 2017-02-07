package ru.sbtqa.tag.pagefactory.exceptions;

public class PageException extends Exception {

    /**
     * 
     * @param e  TODO
     */
    public PageException(Throwable e) {
        super(e);
    }
    
    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public PageException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public PageException(String message) {
        super(message);
    }

}
