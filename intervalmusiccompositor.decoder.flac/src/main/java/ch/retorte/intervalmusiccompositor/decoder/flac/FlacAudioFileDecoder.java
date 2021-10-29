package ch.retorte.intervalmusiccompositor.decoder.flac;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider;

import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;

/**
 * Decodes FLAC files.
 */
public class FlacAudioFileDecoder implements AudioFileDecoder {

  //---- Fields

  private final FlacFileProperties flacFile = new FlacFileProperties();


  //---- Methods

  @Override
  public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {

    AudioInputStream flacAis;
    AudioInputStream result;

    FlacAudioFileReader fafr = new FlacAudioFileReader();
    flacAis = fafr.getAudioInputStream(inputFile);

    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, flacAis.getFormat().getSampleRate(), 16, flacAis.getFormat().getChannels(),
        flacAis.getFormat().getChannels() * 2, flacAis.getFormat().getSampleRate(), false);

    FlacFormatConversionProvider flacReader = new FlacFormatConversionProvider();
    result = flacReader.getAudioInputStream(decodedFormat, flacAis);

    return result;
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return flacFile.isOfThisType(file);
  }

  @Override
  public Collection<String> getExtensions() {
    return newArrayList(flacFile.getFileExtensions());
  }


}
