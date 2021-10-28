package ch.retorte.intervalmusiccompositor.encoder.ogg;

import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressUpdatable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Encodes an audio input stream to the ogg/vorbis format.
 */
public class OggAudioFileEncoder implements AudioFileEncoder, ProgressUpdatable {

    private static final String EXTENSION = "ogg";

    private VorbisEncoder encoder;
    private ProgressListener progressListener;

    @Override
    public void encode(AudioInputStream audioInputStream, long streamLengthInBytes, File outputFile) throws IOException {
        createEncoderWithSettings(audioInputStream.getFormat());
        Files.write(Paths.get(outputFile.getAbsolutePath()), encodeToOgg(audioInputStream, streamLengthInBytes));
    }

    private void createEncoderWithSettings(AudioFormat audioFormat) {
        encoder = new VorbisEncoder(audioFormat, progressListener);
    }

    private byte[] encodeToOgg(AudioInputStream audioInputStream, long streamLengthInBytes) throws IOException {
        return encoder.encodeToOgg(audioInputStream, streamLengthInBytes);
    }

    @Override
    public boolean isAbleToEncode() {
        return true;
    }

    @Override
    public String getFileExtension() {
        return EXTENSION;
    }

    @Override
    public String getIdentificator() {
        return EXTENSION;
    }

    @Override
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }
}
