package ch.retorte.intervalmusiccompositor.ui.utils;

import javafx.util.converter.IntegerStringConverter;

/**
 * This is an extension of the {@link IntegerStringConverter} which does never return 'null',
 * but a default {@link Integer} value instead.
 */
public class NullTolerantIntegerStringConverter extends IntegerStringConverter {

    //---- Fields

    private static final Integer DEFAULT_VALUE = 0;


    //---- Methods

    @Override
    public Integer fromString(String value) {
        Integer result = null;

        try {
            result = super.fromString(value);
        } catch (NumberFormatException e) {
            // nop
        }

        if (result == null) {
            return DEFAULT_VALUE;
        }

        return result;
    }
}
