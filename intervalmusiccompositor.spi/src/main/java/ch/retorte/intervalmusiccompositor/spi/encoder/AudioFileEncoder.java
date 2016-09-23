package ch.retorte.intervalmusiccompositor.spi.encoder;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author nw
 */
public interface AudioFileEncoder {

  /**
   * Encodes the input file into some target format and saves it in the output file. Assumes the input stream of default encoding of the software.
   * 
   * @param audioInputStream
   *          the audio input stream containing the raw audio data to be encoded.
   * @param streamLengthInBytes
   *          the length of the audio input stream in bytes
   * @param outputFile
   *          the location where the output file should be placed into.
   * @throws UnsupportedAudioFileException
   *           if we are not able to read the input file.
   * @throws IOException
   *           if we are not able to either read the input or write the output file.
   */
  void encode(AudioInputStream audioInputStream, long streamLengthInBytes, File outputFile) throws UnsupportedAudioFileException, IOException;

  /**
   * Determines if this encoder is able to encode music data.
   * 
   * @return true if this encoder is able to perform its duties on this system.
   */
  boolean isAbleToEncode();

  /**
   * Produces the file extension used for output files this encoder can produce.
   * 
   * @return the file extension for output files of this encoder.
   */
  String getFileExtension();

  /**
   * Provides a unique identification string for this encoder.
   */
  String getIdentificator();
}
