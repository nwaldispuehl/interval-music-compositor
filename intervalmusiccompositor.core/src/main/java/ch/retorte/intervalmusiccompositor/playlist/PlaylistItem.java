package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;

/**
 * @author nw
 */
public class PlaylistItem {

  private final IAudioFile audioFile;
  private final long extractStartInMilliseconds;
  private final long extractEndInMilliseconds;

  public PlaylistItem(IAudioFile audioFile, long extractStartInMilliseconds, long extractEndInMilliseconds) {
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

  public long getExtractDuration() {
    return extractEndInMilliseconds - extractStartInMilliseconds;
  }

  public double getExtractDurationInSeconds() {
    return getExtractDuration() / 1000;
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

  public long getExtractEndInMilliseconds() {
    return extractEndInMilliseconds;
  }

  public double getExtractEndInSeconds() {
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
