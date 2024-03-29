package ch.retorte.intervalmusiccompositor.decoder.wave;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.sampled.convert.PCM2PCMConversionProvider;
import org.tritonus.sampled.file.WaveAudioFileReader;

import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;

/**
 * Decodes WAV files
 */
public class WaveAudioFileDecoder implements AudioFileDecoder {

  //---- Fields

  private final WaveFileProperties waveFile = new WaveFileProperties();


  //---- Methods

  @Override
  public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {

    AudioInputStream ais;
    AudioInputStream result;

    ais = new WaveAudioFileReader().getAudioInputStream(inputFile);

    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, ais.getFormat().getSampleRate(), 16, ais.getFormat().getChannels(), ais
        .getFormat().getChannels() * 2, ais.getFormat().getSampleRate(), false);

    result = new PCM2PCMConversionProvider().getAudioInputStream(decodedFormat, ais);

    return result;
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return waveFile.isOfThisType(file);
  }

  @Override
  public Collection<String> getExtensions() {
    return newArrayList(waveFile.getFileExtensions());
  }

}
