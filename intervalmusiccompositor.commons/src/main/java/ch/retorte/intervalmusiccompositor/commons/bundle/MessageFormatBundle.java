package ch.retorte.intervalmusiccompositor.commons.bundle;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides the bundle with message format support.
 */
public class MessageFormatBundle {

    private final ResourceBundle bundle;

    public MessageFormatBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public String getString(String key, Object... arguments) {
        String value = bundle.getString(key);
        return MessageFormat.format(value, arguments);
    }

    public Locale getLocale() {
        return bundle.getLocale();
    }

    public ResourceBundle getBundle() {
        return bundle;
    }
}
