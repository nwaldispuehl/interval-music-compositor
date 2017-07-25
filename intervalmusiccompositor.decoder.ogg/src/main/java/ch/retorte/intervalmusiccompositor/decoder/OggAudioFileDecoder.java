package ch.retorte.intervalmusiccompositor.decoder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Decodes OGG files.
 */
public class OggAudioFileDecoder implements AudioFileDecoder {

  //---- Fields

  private OggFileProperties oggFile = new OggFileProperties();


  //---- Methods

  @Override
  public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {

    AudioInputStream oggAis;
    AudioInputStream result;

    JorbisAudioFileReader reader = new JorbisAudioFileReader();
    oggAis = reader.getAudioInputStream(inputFile);

    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, oggAis.getFormat().getSampleRate(), 16, oggAis.getFormat().getChannels(),
        oggAis.getFormat().getChannels() * 2, oggAis.getFormat().getSampleRate(), false);

    JorbisFormatConversionProvider oggReader = new JorbisFormatConversionProvider();
    result = oggReader.getAudioInputStream(decodedFormat, oggAis);

    return result;
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return oggFile.isOfThisType(file);
  }

  @Override
  public Collection<String> getExtensions() {
    return newArrayList(oggFile.getFileExtensions());
  }


}
