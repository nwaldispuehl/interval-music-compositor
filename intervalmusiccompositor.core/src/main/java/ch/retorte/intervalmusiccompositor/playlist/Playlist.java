package ch.retorte.intervalmusiccompositor.playlist;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.compilation.CompilationParameters.*;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.CONTINUOUS;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SHUFFLE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author nw
 */
public class Playlist implements Iterable<PlaylistItem> {

  private MessageFormatBundle bundle = getBundle("core_imc");
  private List<PlaylistItem> playlistItems = newArrayList();

  private Long startCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.start"));
  private Long endCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.end"));

  private Random random = new Random();

  private BlendMode blendMode = DEFAULT_BLEND_MODE;
  private Double blendTime = DEFAULT_BLEND_TIME;
  private EnumerationMode enumerationMode = DEFAULT_ENUMERATION_MODE;
  private ListSortMode listSortMode = DEFAULT_LIST_SORT_MODE;

  private Map<IAudioFile, Long> currentProgress = Maps.newConcurrentMap();
  private MessageProducer messageProducer;

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
    this(p.blendMode, p.blendTime, p.enumerationMode, p.listSortMode, messageProducer);
  }

  public void generatePlaylist(List<IAudioFile> musicFiles,
                               List<Integer> musicPattern,
                               List<IAudioFile> breakFiles,
                               List<Integer> breakPattern,
                               Integer iterations) {
    playlistItems = generatePlaylistFrom(musicFiles, musicPattern, breakFiles, breakPattern, iterations);
  }

  @VisibleForTesting
  List<PlaylistItem> generatePlaylistFrom(List<IAudioFile> musicFiles,
                                          List<Integer> musicPattern,
                                          List<IAudioFile> breakFiles,
                                          List<Integer> breakPattern,
                                          Integer iterations) {

    checkNotNull(musicFiles);
    checkNotNull(musicPattern);
    checkNotNull(breakFiles);
    checkNotNull(breakPattern);
    
    if (iterations == 0 || musicFiles.isEmpty()) {
      return newArrayList();
    }

    List<PlaylistItem> musicTracks = createMusicPlaylist(musicFiles, musicPattern, iterations);
    List<PlaylistItem> breakTracks = createBreakPlaylist(breakFiles, breakPattern, iterations, musicPattern.size());

    if (breakPattern.isEmpty()) {
      return musicTracks;
    }

    return interleaveTracks(musicTracks, breakTracks);
  }

  private List<PlaylistItem> interleaveTracks(List<PlaylistItem> musicTracks, List<PlaylistItem> breakTracks) {
    List<PlaylistItem> result = Lists.newArrayList();

    for (int i = 0; i < musicTracks.size(); i++) {
      result.add(musicTracks.get(i));
      result.add(breakTracks.get(i));
    }

    return result;
  }


  private List<PlaylistItem> createMusicPlaylist(List<IAudioFile> musicFiles, List<Integer> musicPattern, Integer iterations) {
    List<PlaylistItem> musicPlaylist = Lists.newArrayList();
    int desiredPlaylistSize = musicPattern.size() * iterations;
    int musicTrackCounter = 0;
    int lastShuffleHappened = 0;
    int musicPatternCounter = 0;
    int skippedTracks = 0;

    while (musicPlaylist.size() < desiredPlaylistSize) {

      IAudioFile currentAudioFile = musicFiles.get((musicTrackCounter) % musicFiles.size());
      int currentSoundPattern = musicPattern.get(musicPatternCounter % musicPattern.size());

      PlaylistItem newMusicTrack = createPlaylistItemFrom(currentAudioFile, currentSoundPattern * 1000);
      if (newMusicTrack != null) {
        musicPlaylist.add(newMusicTrack);
        musicPatternCounter++;
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
        messageProducer.send(new ErrorMessage("Too few usable tracks."));
        throw new IllegalStateException("Too few usable tracks.");
      }

    }

    return musicPlaylist;
  }

  private void shuffle(List<IAudioFile> audioFiles) {
    Collections.shuffle(audioFiles, random);
  }

  private List<PlaylistItem> createBreakPlaylist(List<IAudioFile> breakFiles, List<Integer> breakPattern, Integer iterations, int musicPatternSize) {
    List<PlaylistItem> breakPlaylist = Lists.newArrayList();

    if (breakPattern.isEmpty()) {
      return breakPlaylist;
    }

    int desiredBreakListSize = musicPatternSize * iterations;
    int breakTrackCounter = 0;
    int skippedTracks = 0;

    while (breakPlaylist.size() < desiredBreakListSize) {

      int currentBreakPattern = breakPattern.get((breakTrackCounter % musicPatternSize) % breakPattern.size());

      if (!breakFiles.isEmpty()) {

        IAudioFile currentBreakFile = breakFiles.get((breakTrackCounter % musicPatternSize) % breakFiles.size());

        PlaylistItem newBreakTrack = createPlaylistItemFrom(currentBreakFile, currentBreakPattern * 1000);
        if (newBreakTrack != null) {
          breakPlaylist.add(new BreakPlaylistItem(newBreakTrack));
        }
        else {
          skippedTracks++;
        }

      }
      else {
        breakPlaylist.add(new BreakPlaylistItem(createPlaylistItem(null, 0L, (long) currentBreakPattern * 1000)));
      }

      breakTrackCounter++;

      if (breakFiles.size() < skippedTracks) {
        messageProducer.send(new ErrorMessage("Too few usable tracks."));
        throw new IllegalStateException("Too few usable tracks.");
      }

    }

    return breakPlaylist;
  }

  private PlaylistItem createPlaylistItemFrom(IAudioFile audioFile, long extractLengthInMilliseconds) {

    long maximalRangeForDuration;
    long trackStart = startCutOffInMilliseconds;

    if (isCrossFadingMode()) {
      extractLengthInMilliseconds += (long) (blendTime * 1000);
    }

    if (isContinuousExtractEnumerator()) {
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
      if (isContinuousExtractEnumerator()) {
        currentProgress.remove(audioFile);
      }
      return null;
    }

    if (isContinuousExtractEnumerator()) {
      currentProgress.put(audioFile, trackStart + extractLengthInMilliseconds);
    }

    return createPlaylistItem(audioFile, trackStart, trackStart + extractLengthInMilliseconds);
  }



  private PlaylistItem createPlaylistItem(IAudioFile audioFile, Long startInMilliseconds, Long endInMilliseconds) {
    return new PlaylistItem(audioFile, startInMilliseconds, endInMilliseconds);
  }

  public long getTotalLength(List<PlaylistItem> playlistItems) {
    long result = 0L;
    for (PlaylistItem playlistItem : playlistItems) {
      result += playlistItem.getExtractDurationInMilliseconds();

      if (isCrossFadingMode()) {
        // Removing 1/2 of the blend time due to overlapping between the tracks.
        result -= (blendTime * 500);
      }
    }

    if (isCrossFadingMode()) {
      result += (blendTime * 1000);
    }

    return result;
  }

  private boolean isCrossFadingMode() {
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

  public long getTotalLength() {
    return getTotalLength(playlistItems);
  }

  public int getTotalLengthInSeconds() {
    return (int) (getTotalLength() / 1000);
  }

  public void setCutOff(long startCutOffInMilliseconds, long endCutOffInMilliseconds) {
    this.startCutOffInMilliseconds = startCutOffInMilliseconds;
    this.endCutOffInMilliseconds = endCutOffInMilliseconds;
  }

  public BlendMode getBlendMode() {
    return blendMode;
  }

  public Double getBlendTime() {
    return blendTime;
  }

  @Override
  public Iterator<PlaylistItem> iterator() {
    return playlistItems.iterator();
  }

}
