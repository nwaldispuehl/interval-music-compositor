package ch.retorte.intervalmusiccompositor.encoder;

import ch.retorte.intervalmusiccompositor.spi.audio.ByteArrayConverter;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressUpdatable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Encodes an audio input stream to the ogg/vorbis format.
 */
public class OggAudioFileEncoder implements AudioFileEncoder, ProgressUpdatable {

  private static final String EXTENSION = "ogg";

  private ByteArrayConverter byteArrayConverter;

  private VorbisEncoder encoder;
  private ProgressListener progressListener;

  public OggAudioFileEncoder(ByteArrayConverter byteArrayConverter) {
    this.byteArrayConverter = byteArrayConverter;
  }

  @Override
  public void encode(AudioInputStream audioInputStream, File outputFile) throws UnsupportedAudioFileException, IOException {
    createEncoderWithSettings(audioInputStream.getFormat());
    Files.write(Paths.get(outputFile.getAbsolutePath()), encodeToOgg(convert(audioInputStream)));
  }

  private void createEncoderWithSettings(AudioFormat audioFormat) {
    encoder =  new VorbisEncoder(audioFormat, progressListener);
  }

  private byte[] encodeToOgg(byte[] pcmByteArray) throws IOException {
    return encoder.encodePcmToOgg(pcmByteArray);
  }

  private byte[] convert(AudioInputStream audioInputStream) throws IOException {
    return byteArrayConverter.convert(audioInputStream);
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
