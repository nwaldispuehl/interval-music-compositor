package ch.retorte.intervalmusiccompositor.encoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.spi.audio.ByteArrayConverter;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author nw
 */
public class Mp3AudioFileEncoder implements AudioFileEncoder {

  private static final String EXTENSION = "mp3";

  private ByteArrayConverter byteArrayConverter;

  private LameByteArrayEncoder encoder;

  public Mp3AudioFileEncoder(ByteArrayConverter byteArrayConverter) {
    this.byteArrayConverter = byteArrayConverter;
  }

  public void encode(AudioInputStream audioInputStream, File outputFile) throws UnsupportedAudioFileException, IOException {
    createEncoderWithSettings(audioInputStream.getFormat());
    Files.write(Paths.get(outputFile.getAbsolutePath()), encodeToMp3(convert(audioInputStream)));
  }

  private void createEncoderWithSettings(AudioFormat audioFormat) {
    encoder =  new LameByteArrayEncoder(audioFormat);
  }

  private byte[] convert(AudioInputStream audioInputStream) throws IOException {
    return byteArrayConverter.convert(audioInputStream);
  }

  private byte[] encodeToMp3(byte[] pcmByteArray) {
    return encoder.encodePcmToMp3(pcmByteArray);
  }

  public boolean isAbleToEncode() {
    return true;
  }

  public String getFileExtension() {
    return EXTENSION;
  }
}
