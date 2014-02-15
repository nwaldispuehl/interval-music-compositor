package ch.retorte.intervalmusiccompositor.spi.bpm;

import java.io.File;

/**
 * Interface may be implemented by anyone who is able to read BPM (beats per minute) information from a file of certain type.
 * 
 * @author nw
 */
public interface BPMReaderWriter {

  /**
   * Reads the BPM value from the provided file.
   * 
   * @param file
   *          the file which contains the BMP information.
   * @return the BPM value, or null if none was found.
   */
  Integer readBPMFrom(File file);

  /**
   * Writes the provided BPM value into the file.
   * 
   * @param bpm
   *          a BPM value.
   * @param file
   *          the file where this information is being written to.
   */
  void writeBPMTo(Integer bpm, File file);

  /**
   * Determines if this {@link BPMReaderWriter} is able to write and read the BPM information of the provided file.
   * 
   * @param file
   *          which should be checked for compatibility with this {@link BPMReaderWriter}.
   * @return true if this reader/writer is able to handle the file, false otherwise.
   */
  boolean isAbleToReadWriteBPM(File file);
}
