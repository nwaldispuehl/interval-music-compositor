package ch.retorte.intervalmusiccompositor.model.audiofile;

import java.util.Comparator;

/**
 * Comparator which puts IAudioFile in an order, namely by display name.
 * 
 * @author nw
 */
public class AudioFileComparator implements Comparator<IAudioFile> {

  @Override
  public int compare(IAudioFile o1, IAudioFile o2) {
    return o1.getDisplayName().compareTo(o2.getDisplayName());
  }
}
