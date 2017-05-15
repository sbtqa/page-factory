package ru.sbtqa.tag.pagefactory.maven_artefacts.module_entry_points.exceptions;

public class ElementDescriptionException extends PageException {

    /**
     * 
     * @param e a {@link java.lang.Throwable} object.
     */
    public ElementDescriptionException(Throwable e) {
        super(e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public ElementDescriptionException(String message, Throwable e) {
        super(message, e);
    }

    /**
     *
     * @param message a {@link java.lang.String} object.
     */
    public ElementDescriptionException(String message) {
        super(message);
    }

}
