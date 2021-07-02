package ch.retorte.intervalmusiccompositor.ui.utils;

import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import javafx.util.StringConverter;

import java.util.Collection;

public class AudioFileEncoderConverter extends StringConverter<AudioFileEncoder> {

    private final Collection<AudioFileEncoder> encoders;

    public AudioFileEncoderConverter(Collection<AudioFileEncoder> encoders) {
        this.encoders = encoders;
    }

    @Override
    public String toString(AudioFileEncoder audioFileEncoder) {
        if (audioFileEncoder == null) {
            return null;
        }
        return audioFileEncoder.getIdentificator();
    }

    @Override
    public AudioFileEncoder fromString(String identifier) {
        return findEncoderFor(identifier);
    }

    private AudioFileEncoder findEncoderFor(String identifier) {
        for (AudioFileEncoder e : encoders) {
            if (e.getIdentificator().equalsIgnoreCase(identifier)) {
                return e;
            }
        }
        throw new IllegalStateException("Identifier " + identifier + " does not match any encoders.");
    }
}
