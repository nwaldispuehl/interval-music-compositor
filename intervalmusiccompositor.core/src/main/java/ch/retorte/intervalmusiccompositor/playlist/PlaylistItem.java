package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;

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
    return getStrictMusicLengthMs() + getStrictBreakLengthMs();
  }

  public long getMusicLengthMs() {
    return getMusicFragment().getExtractDurationInMilliseconds();
  }

  public long getStrictMusicLengthMs() {
    if (playlist.isCrossFadingMode()) {
      return getMusicLengthMs() - playlist.getBlendTimeMs();
    }
    else {
      return getMusicLengthMs();
    }
  }

  public long getBreakLengthMs() {
    if (hasBreakFragment()) {
      return getBreakFragment().getExtractDurationInMilliseconds();
    }
    else {
      return 0L;
    }
  }

  public long getStrictBreakLengthMs() {
    if (hasBreakFragment() && playlist.isCrossFadingMode()) {
      return getBreakLengthMs() - playlist.getBlendTimeMs();
    }
    else {
      return getBreakLengthMs();
    }
  }
}
