package ru.sbtqa.tag.pagefactory.maven_artefacts.module_entry_points.exceptions;

public class ElementNotFoundException extends PageException {

    /**
     * 
     * @param e a {@link java.lang.Throwable} object.
     */
    public ElementNotFoundException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public ElementNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public ElementNotFoundException(String message) {
        super(message);
    }

}
