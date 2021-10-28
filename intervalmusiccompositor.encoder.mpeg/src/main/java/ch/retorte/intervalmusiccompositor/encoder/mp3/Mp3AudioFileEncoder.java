package ch.retorte.intervalmusiccompositor.encoder.mp3;

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
 * Encodes an {@link AudioInputStream} to a mp3 file.
 */
public class Mp3AudioFileEncoder implements AudioFileEncoder, ProgressUpdatable {

    private static final String EXTENSION = "mp3";

    private LameByteArrayEncoder encoder;
    private ProgressListener progressListener;

    public void encode(AudioInputStream audioInputStream, long streamLengthInBytes, File outputFile) throws IOException {
        createEncoderWithSettings(audioInputStream.getFormat());
        Files.write(Paths.get(outputFile.getAbsolutePath()), encodeToMp3(audioInputStream, streamLengthInBytes));
    }

    private void createEncoderWithSettings(AudioFormat audioFormat) {
        encoder = new LameByteArrayEncoder(audioFormat, progressListener);
    }

    private byte[] encodeToMp3(AudioInputStream audioInputStream, long streamLengthInBytes) throws IOException {
        return encoder.encodeToMp3(audioInputStream, streamLengthInBytes);
    }

    public boolean isAbleToEncode() {
        return true;
    }

    public String getFileExtension() {
        return EXTENSION;
    }

    public String getIdentificator() {
        return EXTENSION;
    }

    @Override
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }
}
