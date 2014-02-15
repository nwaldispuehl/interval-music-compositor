package ch.retorte.intervalmusiccompositor.encoder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.encoder.WaveAudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.util.mp3converter.ExternalMp3Converter;
import ch.retorte.intervalmusiccompositor.util.mp3converter.ExternalMp3ConverterFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author nw
 */
public class Mp3AudioFileEncoder implements AudioFileEncoder {

  private static final String EXTENSION = "mp3";

  private ExternalMp3Converter mp3Converter = new ExternalMp3ConverterFactory().createConverter();

  public void encode(AudioInputStream audioInputStream, File outputfile) throws UnsupportedAudioFileException, IOException {
    File temporaryFile = File.createTempFile("mp3EncoderTemporaryFile", "imc");

    new WaveAudioFileEncoder().encode(audioInputStream, temporaryFile);

    mp3Converter.convertToMp3(temporaryFile, outputfile);

    temporaryFile.delete();
  }

  public boolean isAbleToEncode() {
    return mp3Converter.isLamePresent();
  }

  public String getFileExtension() {
    return EXTENSION;
  }
}
