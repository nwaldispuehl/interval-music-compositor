package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.model.list.BlendMode;
import ch.retorte.intervalmusiccompositor.model.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.model.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.model.util.VisibleForTesting;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters.*;
import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.model.list.EnumerationMode.CONTINUOUS;
import static ch.retorte.intervalmusiccompositor.model.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.model.list.ListSortMode.SHUFFLE;
import static ch.retorte.intervalmusiccompositor.model.util.Preconditions.checkNotNull;


/**
 * @author nw
 */
public class Playlist implements Iterable<PlaylistItem> {

  //---- Fields

  private final MessageFormatBundle bundle = new CoreBundleProvider().getBundle();
  private List<PlaylistItem> playlistItems = newArrayList();

  private Long startCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.start"));
  private Long endCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.end"));

  private final Random random = new Random();

  private BlendMode blendMode = DEFAULT_BLEND_MODE;
  private Double blendTime = DEFAULT_BLEND_DURATION;
  private EnumerationMode enumerationMode = DEFAULT_ENUMERATION_MODE;
  private ListSortMode listSortMode = DEFAULT_LIST_SORT_MODE;

  private final Map<IAudioFile, Long> currentProgress = new ConcurrentHashMap<>();
  private final MessageProducer messageProducer;


  //---- Constructor

  @VisibleForTesting
  Playlist(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  @VisibleForTesting
  Playlist(BlendMode blendMode, Double blendTime, EnumerationMode enumerationMode, ListSortMode listSortMode, MessageProducer messageProducer) {
    this.blendMode = blendMode;
    this.blendTime = blendTime;
    this.enumerationMode = enumerationMode;
    this.listSortMode = listSortMode;
    this.messageProducer = messageProducer;
  }

  public Playlist(CompilationParameters p, MessageProducer messageProducer) {
    this(p.getBlendMode(), p.getBlendDuration(), p.getEnumerationMode(), p.getListSortMode(), messageProducer);
  }


  //---- Methods

  public void generatePlaylist(List<IAudioFile> musicFiles,
                               List<Integer> musicPattern,
                               List<IAudioFile> breakFiles,
                               List<Integer> breakPattern,
                               double breakVolume,
                               Integer iterations,
                               List<SoundEffectOccurrence> soundEffectOccurrences) {
    playlistItems = generatePlaylistFrom(musicFiles, musicPattern, breakFiles, breakPattern, breakVolume, iterations, soundEffectOccurrences);
  }

  @VisibleForTesting
  List<PlaylistItem> generatePlaylistFrom(List<IAudioFile> musicFiles,
                                                  List<Integer> musicPattern,
                                                  List<IAudioFile> breakFiles,
                                                  List<Integer> breakPattern,
                                                  double breakVolume,
                                                  Integer iterations,
                                                  List<SoundEffectOccurrence> soundEffectOccurrences) {

    checkNotNull(musicFiles);
    checkNotNull(musicPattern);
    checkNotNull(breakFiles);
    checkNotNull(breakPattern);
    
    if (iterations == 0 || musicFiles.isEmpty()) {
      return newArrayList();
    }

    List<PlaylistItemFragment> musicTracks = createMusicPlaylist(musicFiles, musicPattern, iterations);
    List<PlaylistItemFragment> breakTracks = createBreakPlaylist(breakFiles, breakPattern, iterations, musicPattern.size(), breakVolume);

    return createItemsWith(musicTracks, breakTracks, soundEffectOccurrences);
  }

  private List<PlaylistItem> createItemsWith(List<PlaylistItemFragment> musicTracks, List<PlaylistItemFragment> breakTracks, List<SoundEffectOccurrence> soundEffectOccurrences) {
    List<PlaylistItem> result = new ArrayList<>();

    for (int i = 0; i < musicTracks.size(); i++) {
      PlaylistItemFragment musicTrack = musicTracks.get(i);
      PlaylistItemFragment breakTrack = null;
      if (!breakTracks.isEmpty()) {
        breakTrack = breakTracks.get(i);
      }

      result.add(new PlaylistItem(this, musicTrack, breakTrack, soundEffectOccurrences));
    }

    return result;
  }

  private List<PlaylistItemFragment> createMusicPlaylist(List<IAudioFile> musicFiles, List<Integer> musicPattern, Integer iterations) {
    List<PlaylistItemFragment> musicPlaylist = new ArrayList<>();
    int desiredPlaylistSize = musicPattern.size() * iterations;
    int musicTrackCounter = 0;
    int lastShuffleHappened = 0;
    int musicPatternCounter = 0;
    int skippedTracks = 0;

    while (musicPlaylist.size() < desiredPlaylistSize) {

      IAudioFile currentAudioFile = musicFiles.get((musicTrackCounter) % musicFiles.size());
      int currentSoundPattern = musicPattern.get(musicPatternCounter % musicPattern.size());

      PlaylistItemFragment newMusicTrack = createPlaylistItemFrom(currentAudioFile, 1, currentSoundPattern * 1000L, false);
      if (newMusicTrack != null) {
        musicPlaylist.add(newMusicTrack);
        musicPatternCounter++;
        skippedTracks = 0;
      }
      else {
        skippedTracks++;
      }

      if (isSingleExtractEnumeration() || newMusicTrack == null) {
        musicTrackCounter++;
      }

      if (isShuffleMode() && musicTrackCounter != lastShuffleHappened && (musicTrackCounter % musicFiles.size() == 0)) {
        shuffle(musicFiles);
        lastShuffleHappened = musicTrackCounter;
        skippedTracks = 0;
      }

      if (musicFiles.size() < skippedTracks) {
        throw new IllegalStateException("Too few usable tracks.");
      }
    }

    return musicPlaylist;
  }

  private void shuffle(List<IAudioFile> audioFiles) {
    Collections.shuffle(audioFiles, random);
  }

  private List<PlaylistItemFragment> createBreakPlaylist(List<IAudioFile> breakFiles, List<Integer> breakPattern, Integer iterations, int musicPatternSize, double volume) {
    List<PlaylistItemFragment> breakPlaylist = new ArrayList<>();

    if (breakPattern.isEmpty() || hasSingleZero(breakPattern)) {
      return breakPlaylist;
    }

    int desiredBreakListSize = musicPatternSize * iterations;
    int breakTrackCounter = 0;

    while (breakPlaylist.size() < desiredBreakListSize) {
      int currentBreakPattern = breakPattern.get((breakTrackCounter % musicPatternSize) % breakPattern.size());

      if (!breakFiles.isEmpty()) {
        IAudioFile currentBreakFile = breakFiles.get((breakTrackCounter % musicPatternSize) % breakFiles.size());

        PlaylistItemFragment newBreakTrack = createPlaylistItemFrom(currentBreakFile, volume, currentBreakPattern * 1000L, true);
        if (newBreakTrack != null) {
          breakPlaylist.add(new BreakPlaylistItemFragment(newBreakTrack));
        }
        else {
          throw new IllegalStateException("Break track too short for desired break pattern: " + currentBreakPattern + "s.");
        }

      }
      else {
        if (isCrossFadingMode()) {
          currentBreakPattern += blendTime;
        }
        breakPlaylist.add(new BreakPlaylistItemFragment(createPlaylistItem(null, volume, 0L, currentBreakPattern * 1000L)));
      }

      breakTrackCounter++;
    }

    return breakPlaylist;
  }

  private boolean hasSingleZero(List<Integer> breakPattern) {
    return breakPattern.size() == 1 && breakPattern.getFirst() == 0;
  }

  private PlaylistItemFragment createPlaylistItemFrom(IAudioFile audioFile, double volume, long extractLengthInMilliseconds, boolean isBreak) {

    long maximalRangeForDuration;
    long trackStart = startCutOffInMilliseconds;

    if (isCrossFadingMode()) {
      extractLengthInMilliseconds += (long) (blendTime * 1000);
    }

    if (isContinuousExtractEnumerator() && !isBreak) {
      if (!currentProgress.containsKey(audioFile)) {
        currentProgress.put(audioFile, startCutOffInMilliseconds);
      }
      trackStart = currentProgress.get(audioFile);

      maximalRangeForDuration = (audioFile.getDuration() - trackStart - endCutOffInMilliseconds - extractLengthInMilliseconds);
    }
    else {
      maximalRangeForDuration = (audioFile.getDuration() - startCutOffInMilliseconds - endCutOffInMilliseconds - extractLengthInMilliseconds);

      if (0 < maximalRangeForDuration) {
        trackStart += random.nextInt((int) maximalRangeForDuration);
      }
    }

    if (maximalRangeForDuration < 0) {
      if (isContinuousExtractEnumerator()  && !isBreak) {
        currentProgress.remove(audioFile);
      }
      return null;
    }

    if (isContinuousExtractEnumerator()  && !isBreak) {
      currentProgress.put(audioFile, trackStart + extractLengthInMilliseconds);
    }

    return createPlaylistItem(audioFile, volume, trackStart, trackStart + extractLengthInMilliseconds);
  }

  boolean hasSoundEffects() {
    return playlistItems.stream().anyMatch(PlaylistItem::hasSoundEffects);
  }

  private PlaylistItemFragment createPlaylistItem(IAudioFile audioFile, double volume, Long startInMilliseconds, Long endInMilliseconds) {
    return new PlaylistItemFragment(audioFile, volume, startInMilliseconds, endInMilliseconds);
  }

  long getTotalLength(Playlist playlist, List<PlaylistItem> playlistItems) {
    long resultMs = 0L;

    long blendTimeMs = playlist.getBlendTimeMs();
    boolean isCrossFadingMode = playlist.isCrossFadingMode();

    for (PlaylistItem playlistItem : playlistItems) {
      resultMs += playlistItem.getStrictItemLengthMs();
    }

    if (isCrossFadingMode) {
      resultMs += blendTimeMs;
    }

    return resultMs;
  }

  public boolean isCrossFadingMode() {
    return blendMode == CROSS;
  }

  private boolean isSingleExtractEnumeration() {
    return enumerationMode == SINGLE_EXTRACT;
  }

  private boolean isContinuousExtractEnumerator() {
    return enumerationMode == CONTINUOUS;
  }

  private boolean isShuffleMode() {
    return listSortMode == SHUFFLE;
  }

  private long getTotalLength() {
    return getTotalLength(this, playlistItems);
  }

  public int getTotalLengthInSeconds() {
    return (int) (getTotalLength() / 1000);
  }

  void setCutOff(long startCutOffInMilliseconds, long endCutOffInMilliseconds) {
    this.startCutOffInMilliseconds = startCutOffInMilliseconds;
    this.endCutOffInMilliseconds = endCutOffInMilliseconds;
  }

  BlendMode getBlendMode() {
    return blendMode;
  }

  /**
   * Blend time in seconds.
   */
  public Double getBlendTimeS() {
    return blendTime;
  }

  public Double getHalfBlendTimeS() {
    return blendTime / 2;
  }

  public long getBlendTimeMs() {
    return (long) (blendTime * 1000);
  }

  public long getHalfBlendTimeMs() {
    return (long) (blendTime * 500);
  }

  @Override
  public Iterator<PlaylistItem> iterator() {
    return playlistItems.iterator();
  }

  PlaylistItem getPreviousOf(PlaylistItem playlistItem) {
    int previousIndex = playlistItems.indexOf(playlistItem) - 1;
    if (0 <= previousIndex) {
      return playlistItems.get(previousIndex);
    }
    else {
      return null;
    }
  }

  PlaylistItem getNextOf(PlaylistItem playlistItem) {
    int nextIndex = playlistItems.indexOf(playlistItem) + 1;
    if (nextIndex < playlistItems.size()) {
      return playlistItems.get(nextIndex);
    }
    else {
      return null;
    }
  }

  public Optional<PlaylistItem> getFirst() {
    return playlistItems.isEmpty() ? Optional.empty() : Optional.of(playlistItems.getFirst());
  }

  public Optional<PlaylistItem> getLast() {
    return playlistItems.isEmpty() ? Optional.empty() : Optional.of(playlistItems.getLast());
  }
}
