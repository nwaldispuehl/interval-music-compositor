package ch.retorte.intervalmusiccompositor.commons;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;

import java.util.ResourceBundle;

/**
 * Bundle which supports Utf8.
 */
public class Utf8Bundle {

    public static MessageFormatBundle getBundle(String baseName) {
        return new MessageFormatBundle(ResourceBundle.getBundle(baseName));
    }

}
