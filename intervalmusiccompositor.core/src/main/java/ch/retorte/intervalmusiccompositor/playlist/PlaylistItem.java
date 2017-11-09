package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class PlaylistItem {

  private Playlist playlist;
  private final PlaylistItemFragment musicFragment;
  private final PlaylistItemFragment breakFragment;
  private final List<SoundEffectOccurrence> soundEffects = newLinkedList();

  PlaylistItem(Playlist playlist, PlaylistItemFragment musicFragment, PlaylistItemFragment breakFragment, List<SoundEffectOccurrence> soundEffects) {
    this.playlist = playlist;
    this.musicFragment = musicFragment;
    this.breakFragment = breakFragment;
    this.soundEffects.addAll(soundEffects);
  }

  public PlaylistItemFragment getMusicFragment() {
    return musicFragment;
  }

  public boolean hasBreakFragment() {
    return breakFragment != null;
  }

  public PlaylistItemFragment getBreakFragment() {
    return breakFragment;
  }

  public PlaylistItem getPrevious() {
    return playlist.getPreviousOf(this);
  }

  public boolean hasPrevious() {
    return getPrevious() != null;
  }

  public PlaylistItem getNext() {
    return playlist.getNextOf(this);
  }

  public boolean hasNext() {
    return getNext() != null;
  }

  public boolean hasSoundEffects() {
    return !soundEffects.isEmpty();
  }

  public List<SoundEffectOccurrence> getSoundEffects() {
    return soundEffects;
  }

  /**
   * Returns the length of the single segment, that is, the length without overlapping parts.
   */
  public long getStrictItemLengthMs() {
      long musicLengthMs = getMusicFragment().getExtractDurationInMilliseconds();
      long breakLengthMs = hasBreakFragment() ? getBreakFragment().getExtractDurationInMilliseconds() : 0;

      if (playlist.isCrossFadingMode()) {
        return musicLengthMs - playlist.getBlendTimeMs() + breakLengthMs - (hasBreakFragment() ? playlist.getBlendTimeMs() : 0);
      }
      else {
        return musicLengthMs + breakLengthMs;
      }
  }
}
