package ch.retorte.intervalmusiccompositor.spi.decoder;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The implementation of this interface offers the ability to decode a certain type of audio files, that is, converts the provided file into a standard audio
 * input stream which can then be further processed.
 * 
 * @author nw
 */
public interface AudioFileDecoder {

  /* This is what we want is internal representation, that is, target format. The conventional compact disc standard. */
  public static final float SAMPLE_RATE = 44100.0F;
  public static final AudioFormat.Encoding TARGET_ENCODING = AudioFormat.Encoding.PCM_SIGNED;
  public static final AudioFormat TARGET_AUDIO_FORMAT = new AudioFormat(TARGET_ENCODING, SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false);

  /**
   * Reads and decodes the input file to a {@link AudioInputStream}. The convention is that the returned {@link AudioInputStream} has the properties of the
   * target audio format.
   * 
   * @param inputFile
   *          the file which has to be decoded.
   * @return the decoded audio data of the file in form of a {@link AudioInputStream}.
   * @throws UnsupportedAudioFileException
   *           if the file is in a format this decoder does not understand.
   * @throws IOException
   *           if the decoder was not able to read the input file properly.
   */
  AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException;

  /**
   * Determines if this decoder is able to decode the provided file.
   * 
   * @param file a file for which to check if this decoder is able to decode it.
   * @return true if this decoder thinks he is able to decode the provided file.
   */
  boolean isAbleToDecode(File file);
}
