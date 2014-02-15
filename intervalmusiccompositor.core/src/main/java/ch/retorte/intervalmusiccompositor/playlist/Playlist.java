package ch.retorte.intervalmusiccompositor.playlist;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.SEPARATE;
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

  private BlendMode blendMode = SEPARATE;
  private Double blendTime = 1.0;
  private EnumerationMode enumerationMode = SINGLE_EXTRACT;
  private ListSortMode listSortMode;

  private Map<IAudioFile, Long> currentProgress = Maps.newConcurrentMap();

  Playlist(MessageProducer messageProducer) {
  }

  public Playlist(BlendMode blendMode, Double blendTime, EnumerationMode enumerationMode, ListSortMode listSortMode, MessageProducer messageProducer) {
    this.blendMode = blendMode;
    this.blendTime = blendTime;
    this.enumerationMode = enumerationMode;
    this.listSortMode = listSortMode;
  }

  public Playlist(CompilationParameters p, MessageProducer messageProducer) {
    this(p.blendMode, p.blendTime, p.enumerationMode, p.listSortMode, messageProducer);
  }

  public void generatePlaylist(List<IAudioFile> musicFiles, List<Integer> musicPattern, List<IAudioFile> breakFiles,
                                          List<Integer> breakPattern,
                                          Integer iterations) {
    playlistItems = generatePlaylistFrom(musicFiles, musicPattern, breakFiles, breakPattern, iterations);
  }


  @VisibleForTesting
  List<PlaylistItem> generatePlaylistFrom(List<IAudioFile> musicFiles, List<Integer> musicPattern, List<IAudioFile> breakFiles,
      List<Integer> breakPattern,
 Integer iterations) {
    List<PlaylistItem> result = newArrayList();
    
    checkNotNull(musicFiles);
    checkNotNull(musicPattern);
    checkNotNull(breakFiles);
    checkNotNull(breakPattern);
    
    if (iterations == 0 || musicFiles.isEmpty()) {
      return result;
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
    List<PlaylistItem> result = Lists.newArrayList();
    int desiredPlaylistSize = musicPattern.size() * iterations;
    int musicTrackCounter = 0;
    int musicPatternCounter = 0;
    int skippedTracks = 0;

    while (result.size() < desiredPlaylistSize) {

      IAudioFile currentAudioFile = musicFiles.get((musicTrackCounter) % musicFiles.size());
      int currentSoundPattern = musicPattern.get(musicPatternCounter % musicPattern.size());

      PlaylistItem newMusicTrack = createPlaylistItemFrom(currentAudioFile, currentSoundPattern * 1000);
      if (newMusicTrack != null) {
        result.add(newMusicTrack);
        musicPatternCounter++;
      }
      else {
        skippedTracks++;
      }

      if (enumerationMode == SINGLE_EXTRACT || newMusicTrack == null) {
        musicTrackCounter++;
      }

      if (listSortMode == SHUFFLE && (musicTrackCounter % musicFiles.size() == 0)) {
          Collections.shuffle(musicFiles, random);
      }

      if (musicFiles.size() < skippedTracks) {
        throw new IllegalStateException("Too few usable tracks.");
      }

    }

    return result;
  }

  private List<PlaylistItem> createBreakPlaylist(List<IAudioFile> breakFiles, List<Integer> breakPattern, Integer iterations, int musicPatternSize) {
    List<PlaylistItem> result = Lists.newArrayList();

    int desiredBreaklistSize = musicPatternSize * iterations;
    int breakTrackCounter = 0;
    int skippedTracks = 0;

    if (breakPattern.isEmpty()) {
      return result;
    }

    while (result.size() < desiredBreaklistSize) {

      int currentBreakPattern = breakPattern.get((breakTrackCounter % musicPatternSize) % breakPattern.size());

      if (!breakFiles.isEmpty()) {

        IAudioFile currentBreakFile = breakFiles.get((breakTrackCounter % musicPatternSize) % breakFiles.size());

        PlaylistItem newBreakTrack = createPlaylistItemFrom(currentBreakFile, currentBreakPattern * 1000);
        if (newBreakTrack != null) {
          result.add(new BreakPlaylistItem(newBreakTrack));
        }
        else {
          skippedTracks++;
        }

      }
      else {
        result.add(new BreakPlaylistItem(createPlaylistItem(null, 0L, (long) currentBreakPattern * 1000)));
      }

      breakTrackCounter++;

      if (breakFiles.size() < skippedTracks) {
        throw new IllegalStateException("Too few usable tracks.");
      }

    }

    return result;
  }

  private PlaylistItem createPlaylistItemFrom(IAudioFile audioFile, long extractLengthInMilliseconds) {

    long maximalRangeForDuration;
    long trackStart = startCutOffInMilliseconds;

    if (blendMode == CROSS) {
      extractLengthInMilliseconds += (long) (blendTime * 1000);
    }

    if (enumerationMode == CONTINUOUS) {
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
      if (enumerationMode == CONTINUOUS) {
        currentProgress.remove(audioFile);
      }
      return null;
    }

    if (enumerationMode == CONTINUOUS) {
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
      result += playlistItem.getExtractDuration();

      if (blendMode == CROSS) {
        // Removing 1/2 of the blend time due to overlapping between the tracks.
        result -= (blendTime * 500);
      }
    }

    if (blendMode == CROSS) {
      result += (blendTime * 1000);
    }

    return result;
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

  public boolean isEmpty() {
    return playlistItems.isEmpty();
  }

  @Override
  public Iterator<PlaylistItem> iterator() {
    return playlistItems.iterator();
  }

}
