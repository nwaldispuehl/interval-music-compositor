package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;

/**
 * Part of a {@link PlaylistItem}. Holds the actual music information while the item is only a music/break tuple.
 */
public class PlaylistItemFragment {

  //---- Static

  private static final String SILENT_BREAK_TRACK_NAME = "Silent Break";


  //---- Fields

  private final IAudioFile audioFile;
  private final double volume;
  private final long extractStartInMilliseconds;
  private final long extractEndInMilliseconds;


  //---- Constructor

  PlaylistItemFragment(IAudioFile audioFile, double volume, long extractStartInMilliseconds, long extractEndInMilliseconds) {
    this.audioFile = audioFile;
    this.volume = volume;
    this.extractStartInMilliseconds = extractStartInMilliseconds;
    this.extractEndInMilliseconds = extractEndInMilliseconds;

    if (audioFile != null && audioFile.getDuration() < extractEndInMilliseconds) {
      throw new IllegalStateException("Extract exceeds track length.");
    }
  }


  //---- Methods

  public boolean isSilentBreak() {
    return audioFile == null;
  }

  public long getExtractDurationInMilliseconds() {
    return extractEndInMilliseconds - extractStartInMilliseconds;
  }

  public double getExtractDurationInSeconds() {
    return (double) getExtractDurationInMilliseconds() / 1000;
  }

  public IAudioFile getAudioFile() {
    return audioFile;
  }

  public double getVolume() {
    return volume;
  }

  public long getExtractStartInMilliseconds() {
    return extractStartInMilliseconds;
  }

  public double getExtractStartInSeconds() {
    return (double) getExtractStartInMilliseconds() / 1000;
  }

  long getExtractEndInMilliseconds() {
    return extractEndInMilliseconds;
  }

  double getExtractEndInSeconds() {
    return (double) getExtractEndInMilliseconds() / 1000;
  }

  @Override
  public String toString() {
    String fileName = SILENT_BREAK_TRACK_NAME;
    if (!isSilentBreak()) {
      fileName = getAudioFile().getDisplayName();
    }
    return fileName + " [" + extractStartInMilliseconds + " - " + extractEndInMilliseconds + "]";
  }
}
