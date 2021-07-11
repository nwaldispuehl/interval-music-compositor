package ch.retorte.intervalmusiccompositor.model.audiofile;

import ch.retorte.intervalmusiccompositor.model.util.ChangeListener;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Interface for the audio file class used in the software.
 * 
 * @author nw
 */
public interface IAudioFile extends Comparable<File> {

  /**
   * Determines whether the bpm value has been read successfully so far.
   * 
   * @return True if there is a valid bpm value stored in the object. False otherwise.
   */
  Boolean hasBpm();

  /**
   * Gets the currently stored bpm value. The field is initialized with -1.
   * 
   * @return The currently stored bpm value.
   */
  int getBpm();

  /**
   * Indicates whether the bpm value is trusted or not. Usually, values retrieved from the metadata of a file are reliable, as well as manually entered values.
   * 
   * @return True if one can trust the bpm value.
   */
  Boolean isBpmReliable();

  /**
   * Indicates if the bpm value reflects the one stored in the meta data of the file
   * 
   * @return True if the current value is eqivalent to the one in the file
   */
  Boolean isBpmStored();

  /**
   * Sets the currently displayed bpm value without persisting it.
   */
  void setBpm(int bpm);

  /**
   * Writes bpm information back into the meta data fields of the source file (e.g. ID3v2 tags in MP3).
   * 
   * @param bpm
   *          The bpm value that should be written to the file.
   * @throws IOException if writing bpm fails due to file related issues.
   */
  void writeBpm(int bpm) throws IOException;

  /**
   * Indicates whether the implementation supports the reading and writing of bpm tags within the meta data or not
   * 
   * @return True if the reading and writing of bpm data is supported
   */
  Boolean isBpmSupported();

  /**
   * Returns an {@link AudioInputStream} of the audio file. This input stream is of the audio format PCM_SIGNED, 44.1 kHz, 2 channels and frame size 4
   * 
   * @return {@link AudioInputStream} of the audio file.
   * @throws IOException if {@link AudioInputStream} can not be obtained.
   */
  AudioInputStream getAudioInputStream() throws IOException;

  /**
   * Creates a cache file of the audio file containing the raw pcm wave data for faster access and manipulation possibilities. This cache is created in the
   * temporary directory of the file system.
   * 
   * @throws UnsupportedAudioFileException if audio file is not supported.
   * @throws IOException if some other mishap happens.
   */
  void createCache() throws UnsupportedAudioFileException, IOException;

  /**
   * Completely removes the cache file of this audio file.
   * 
   * @throws IOException if cache removal fails.
   */
  void removeCache() throws IOException;

  boolean isLoading();

  boolean isOK();

  AudioFileStatus getStatus();

  void addChangeListener(ChangeListener<IAudioFile> changeListener);

  /**
   * The display name is a comprehensive descriptor of the audio file. This may be the filename or a composition of title and artist of the contained track.
   * 
   * @return The display name of the audio file
   */
  String getDisplayName();


  File getSource();

  /**
   * Gets the previously calculated duration of the audio file. It is initialized with -1.
   * 
   * @return The duration of the track in milliseconds
   */
  Long getDuration();

  /**
   * Returns the ratio between this tracks amplitude and some preset maximum* amplitude. This ratio is 1 if the volume of this track matches the preset volume.
   * The ratio is smaller if the volume of this track is bigger. This ratio thus can be used directly to control the output volume of each track, achieving a
   * normalization of all tracks.
   * 
   * *) For arbitrary definitions of 'maximum'.
   * 
   * @return The ratio of the 'volume' of this track in respect to the preset maximum volume.
   */
  Float getVolumeRatio();

  /**
   * Stores error messages encountered while processing the file for later display
   * 
   * @return An error message string describing the last occurred error.
   */
  String getErrorMessage();

  /**
   * Calculates if the track matches a certain extract length..
   * 
   * @return True if the track is long enough, that is, can hold an extract of the desired length.
   */
  boolean isLongEnoughFor(int extractInSeconds);

  /**
   * Sets the long enough marker to either true ('long enough'), or false ('too short').
   *
   * @param longEnough true if the track is long enough.
   */
  void setLongEnough(boolean longEnough);

  /**
   * Whether this file was marked long enough.
   *
   * @return true if this file is considered long enough for the compilation.
   */
  boolean isLongEnough();

}
