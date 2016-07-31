package ch.retorte.intervalmusiccompositor.decoder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider;

import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author nw
 */
public class FlacAudioFileDecoder implements AudioFileDecoder {

  private FlacFileProperties flacFile = new FlacFileProperties();

  private final AudioStandardizer audioStandardizer;

  public FlacAudioFileDecoder(AudioStandardizer audioStandardizer) {
    this.audioStandardizer = audioStandardizer;
  }

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

    return audioStandardizer.standardize(result);
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
