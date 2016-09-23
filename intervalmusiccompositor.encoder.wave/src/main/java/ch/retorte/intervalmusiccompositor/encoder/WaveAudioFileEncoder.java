package ch.retorte.intervalmusiccompositor.encoder;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;

/**
 * @author nw
 */
public class WaveAudioFileEncoder implements AudioFileEncoder {

  private final static String EXTENSION = "wav";

  private final AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
  private final AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

  public void encode(AudioInputStream audioInputStream, long streamLengthInBytes, File outputFile) throws UnsupportedAudioFileException, IOException {
    AudioInputStream pcmAis = AudioSystem.getAudioInputStream(targetEncoding, audioInputStream);
    AudioSystem.write(pcmAis, fileType, outputFile);
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

}
