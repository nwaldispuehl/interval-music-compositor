package ch.retorte.intervalmusiccompositor.decoder;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

/**
 * @author nw
 */
public class OggAudioFileDecoder implements AudioFileDecoder {

  private OggFileProperties oggFile = new OggFileProperties();
  private AudioStandardizer audioStandardizer;

  public OggAudioFileDecoder(AudioStandardizer audioStandardizer) {
    this.audioStandardizer = audioStandardizer;
  }

  @Override
  public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {

    AudioInputStream oggAis = null;
    AudioInputStream result = null;

    JorbisAudioFileReader reader = new JorbisAudioFileReader();
    oggAis = reader.getAudioInputStream(inputFile);

    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, oggAis.getFormat().getSampleRate(), 16, oggAis.getFormat().getChannels(),
        oggAis.getFormat().getChannels() * 2, oggAis.getFormat().getSampleRate(), false);

    JorbisFormatConversionProvider oggReader = new JorbisFormatConversionProvider();
    result = oggReader.getAudioInputStream(decodedFormat, oggAis);

    return audioStandardizer.standardize(result);
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return oggFile.isOfThisType(file);
  }


}
