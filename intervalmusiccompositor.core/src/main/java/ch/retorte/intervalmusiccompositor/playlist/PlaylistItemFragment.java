package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;

/**
 * Part of a {@link PlaylistItem}. Holds the actual music information while the item is only a music/break tuple.
 */
public class PlaylistItemFragment {

  private final IAudioFile audioFile;
  private final long extractStartInMilliseconds;
  private final long extractEndInMilliseconds;

  PlaylistItemFragment(IAudioFile audioFile, long extractStartInMilliseconds, long extractEndInMilliseconds) {
    this.audioFile = audioFile;
    this.extractStartInMilliseconds = extractStartInMilliseconds;
    this.extractEndInMilliseconds = extractEndInMilliseconds;

    if (audioFile != null && audioFile.getDuration() < extractEndInMilliseconds) {
      throw new IllegalStateException("Extract exceeds track length.");
    }
  }

  public boolean isSilentBreak() {
    return audioFile == null;
  }

  public long getExtractDurationInMilliseconds() {
    return extractEndInMilliseconds - extractStartInMilliseconds;
  }

  public double getExtractDurationInSeconds() {
    return getExtractDurationInMilliseconds() / 1000;
  }

  public IAudioFile getAudioFile() {
    return audioFile;
  }

  public long getExtractStartInMilliseconds() {
    return extractStartInMilliseconds;
  }

  public double getExtractStartInSeconds() {
    return getExtractStartInMilliseconds() / 1000;
  }

  long getExtractEndInMilliseconds() {
    return extractEndInMilliseconds;
  }

  double getExtractEndInSeconds() {
    return getExtractEndInMilliseconds() / 1000;
  }

  @Override
  public String toString() {
    String fileName = "Break";
    if (!isSilentBreak()) {
      fileName = getAudioFile().getDisplayName();
    }
    return fileName + " [" + extractStartInMilliseconds + " - " + extractEndInMilliseconds + "]";
  }
}
