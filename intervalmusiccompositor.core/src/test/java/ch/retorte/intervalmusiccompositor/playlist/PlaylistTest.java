package ch.retorte.intervalmusiccompositor.playlist;

import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.SEPARATE;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.CONTINUOUS;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SHUFFLE;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SORT;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import com.google.common.collect.Lists;

/**
 * Unit test for the {@link Playlist}.
 */
public class PlaylistTest {

  //---- Statics

  private static final String MUSIC = "music";
  private static final String BREAK = "break";

  //---- Fields

  private MessageProducer msgPrd = mock(MessageProducer.class);
  private List<IAudioFile> musicList;
  private List<Integer> musicPattern;
  private List<IAudioFile> breakList;
  private List<Integer> breakPattern;
  private List<SoundEffectOccurrence> soundEffectOccurrences;

  private IAudioFile musicTrack20s = createAudioFileMockWithLength(sec(20), MUSIC);
  private IAudioFile musicTrack40s = createAudioFileMockWithLength(sec(40), MUSIC);
  private IAudioFile musicTrack60s = createAudioFileMockWithLength(sec(60), MUSIC);
  private IAudioFile breakTrack = createAudioFileMockWithLength(sec(60), BREAK);

  private SoundEffect soundEffect1 = mock(SoundEffect.class);

  //---- Methods

  @Before
  public void setup() {
    musicList = newArrayList();
    musicPattern = newArrayList();
    breakList = newArrayList();
    breakPattern = newArrayList();
    soundEffectOccurrences = newArrayList();

    when(soundEffect1.getDurationMillis()).thenReturn(2000L);
  }

  @Test
  public void shouldProduceEmptyListOnEmptyPattern() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    musicList.add(musicTrack20s);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 3);

    // then
    assertThat(tracks.size(), is(0));
  }

  @Test
  public void shouldProduceEmptyListOnEmptyTracks() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    musicPattern = pattern(1, 2, 3);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 3);

    // then
    assertThat(tracks.size(), is(0));
  }

  @Test
  public void shouldGenerateTracksOfRightLength() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    musicList.add(musicTrack20s);
    musicPattern = pattern(5, 10);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1);

    // then
    Assert.assertThat(tracks.size(), CoreMatchers.is(2));
    assertTrackLength(tracks, 5, 10);
    assertTotalLength(tracks, 15);
  }

  @Test
  public void shouldGenerateBreaksOfRightLength() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    musicList.add(musicTrack20s);
    breakList.add(breakTrack);

    musicPattern = pattern(5, 10);
    breakPattern = pattern(2);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1);

    // then
    assertThat(tracks.size(), is(4));
    assertTrackLength(tracks, 5, 2, 10, 2);
    assertTotalLength(tracks, 19);
  }

  @Test
  public void shouldIgnoreSurplusBreakPatterns() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    musicList.add(musicTrack20s);
    breakList.add(breakTrack);

    musicPattern = pattern(9, 7);
    breakPattern = pattern(1, 2, 3, 4);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 3);

    // then
    assertThat(tracks.size(), is(12));
    assertTrackLength(tracks, 9, 1, 7, 2, 9, 1, 7, 2, 9, 1, 7, 2);
    assertTotalLength(tracks, 57);
  }

  @Test
  public void shouldPutTracksInOrder() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    playlist.setCutOff(sec(5), sec(5));
    musicList.add(musicTrack20s);
    musicList.add(musicTrack40s);
    musicList.add(musicTrack60s);

    musicPattern = pattern(2, 4, 6);
    breakPattern = pattern(1);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 2);

    // then
    assertThat(tracks.size(), is(12));
    assertTotalLength(tracks, 30);
    assertTrackLength(tracks, 2, 1, 4, 1, 6, 1, 2, 1, 4, 1, 6, 1);
    assertThat(tracks.get(0).getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(2).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(4).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(6).getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(8).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(10).getAudioFile(), is(musicTrack60s));
  }

  @Test
  public void shouldIncreaseExtractLengthOnCrossFadeByTransitionDuration() {
    // given
    Playlist playlist = new Playlist(CROSS, 1d, SINGLE_EXTRACT, SORT, msgPrd);
    musicList.add(musicTrack40s);
    musicPattern = pattern(2, 7, 13);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 2);

    // then
    assertThat(tracks.size(), is(6));
    assertTotalLength(tracks, 50);
    assertTrackLength(tracks, 3, 8, 14, 3, 8, 14);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectTooShortTracks() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    playlist.setCutOff(sec(5), sec(5));

    musicList.add(musicTrack20s);
    musicPattern = pattern(11);

    // when
    playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1);
  }

  @Test
  public void shouldUseExtractFromInsideTheBoundaries() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    playlist.setCutOff(sec(10), sec(10));

    musicList.add(musicTrack60s);
    musicPattern = pattern(25);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1);

    // then
    assertThat(tracks.size(), is(1));
    assertTrackLength(tracks, 25);
    Assert.assertTrue(sec(10) <= tracks.get(0).getExtractStartInMilliseconds());
    Assert.assertTrue(tracks.get(0).getExtractEndInMilliseconds() <= musicTrack60s.getDuration() - sec(10));
  }

  @Test
  public void shouldSkipTooShortTracksButNeverthelessCompletelyFillPattern() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    playlist.setCutOff(sec(5), sec(5));

    musicList.add(musicTrack60s);
    musicList.add(musicTrack40s);
    musicList.add(musicTrack20s);

    musicPattern = pattern(11);
    breakPattern = pattern(1);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 3);

    // then
    assertThat(tracks.size(), is(6));
    assertTrackLength(tracks, 11, 1, 11, 1, 11, 1);
    assertThat(tracks.get(0).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(2).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(4).getAudioFile(), is(musicTrack60s));
  }


  @Test
  public void shouldTakeTheSameTrackInContinuousMode() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SORT, msgPrd);
    playlist.setCutOff(sec(5), sec(5));

    musicList.add(musicTrack60s);
    musicList.add(musicTrack40s);
    musicList.add(musicTrack20s);

    musicPattern = pattern(10);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 10);

    // then
    assertThat(tracks.size(), is(10));
    assertTrackLength(tracks, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
    assertThat(tracks.get(0).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(1).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(2).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(3).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(4).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(5).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(6).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(7).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(8).getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(9).getAudioFile(), is(musicTrack60s));
  }

  @Test
  public void shouldTakeTheSameTrackInContinuousShuffleMode() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SHUFFLE, msgPrd);
    playlist.setCutOff(sec(5), sec(5));

    musicList.add(musicTrack60s);
    musicList.add(musicTrack40s);
    musicList.add(musicTrack20s);

    musicPattern = pattern(10);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 9);

    // then
    assertThat(tracks.size(), is(9));
    assertTrackLength(tracks, 10, 10, 10, 10, 10, 10, 10, 10, 10);
    assertThat(tracks.get(0).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(1).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(2).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(3).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(4).getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(5).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(6).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(7).getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(8).getAudioFile(), is(musicTrack20s));
  }

  @Test
  public void shouldPlaceSpecialEffectsWithSimplePattern() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SHUFFLE, msgPrd);
    musicList.add(musicTrack60s);
    musicPattern = pattern(10);
    breakPattern = pattern(10);
    soundEffectOccurrences.add(new SoundEffectOccurrence(soundEffect1, 15000));

    // when
    playlist.generatePlaylist(musicList, musicPattern, breakList, breakPattern, 3, soundEffectOccurrences);

    // then
    assertTrue(playlist.hasSoundEffects());
    List<SoundEffectOccurrence> soundEffects = playlist.getSoundEffects();
    assertThat(soundEffects.get(0).getTimeMillis(), is(15000L));
    assertThat(soundEffects.get(1).getTimeMillis(), is(35000L));
    assertThat(soundEffects.get(2).getTimeMillis(), is(55000L));
  }

  @Test
  public void shouldPlaceSpecialEffectsWithComplexPattern() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SHUFFLE, msgPrd);
    musicList.add(musicTrack60s);
    musicPattern = pattern(10, 5);
    breakPattern = pattern(3, 2);
    soundEffectOccurrences.add(new SoundEffectOccurrence(soundEffect1, 6000));

    // when
    playlist.generatePlaylist(musicList, musicPattern, breakList, breakPattern, 5, soundEffectOccurrences);

    // then
    assertTrue(playlist.hasSoundEffects());
    List<SoundEffectOccurrence> soundEffects = playlist.getSoundEffects();
    assertThat(soundEffects.get(0).getTimeMillis(), is(6000L));
    assertThat(soundEffects.get(1).getTimeMillis(), is(19000L));
    assertThat(soundEffects.get(2).getTimeMillis(), is(26000L));
    assertThat(soundEffects.get(3).getTimeMillis(), is(39000L));
    assertThat(soundEffects.get(4).getTimeMillis(), is(46000L));
  }

  @Test
  public void shouldNotPlaceSpecialEffectIfItExceedsTotalLength() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SHUFFLE, msgPrd);
    musicList.add(musicTrack60s);
    musicPattern = pattern(10);
    soundEffectOccurrences.add(new SoundEffectOccurrence(soundEffect1, 9000));

    // when
    playlist.generatePlaylist(musicList, musicPattern, breakList, breakPattern, 2, soundEffectOccurrences);

    // then
    assertTrue(playlist.hasSoundEffects());
    List<SoundEffectOccurrence> soundEffects = playlist.getSoundEffects();
    assertThat(soundEffects.size(), is(1));
    assertThat(soundEffects.get(0).getTimeMillis(), is(9000L));
  }


  @Test
  public void shouldNotThrowExceptionInContinuousShuffleMode() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SHUFFLE, msgPrd);
    playlist.setCutOff(sec(5), sec(5));

    musicList.add(musicTrack20s);

    musicPattern = pattern(10);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 3);

    // then
    assertThat(tracks.size(), is(3));
    assertTrackLength(tracks, 10, 10, 10);
    assertThat(tracks.get(0).getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(1).getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(2).getAudioFile(), is(musicTrack20s));
  }


  //---- Helper methods

  private long sec(int seconds) {
    return (long) seconds * 1000;
  }

  private void assertTrackLength(List<PlaylistItem> tracks, int... seconds) {
    assertThat(tracks.size(), is(seconds.length));
    for (int i = 0; i < seconds.length; i++) {
      assertThat(tracks.get(i).getExtractDurationInMilliseconds(), is(sec(seconds[i])));
    }
  }

  private void assertTotalLength(List<PlaylistItem> tracks, int seconds) {
    assertThat(new Playlist(msgPrd).getTotalLength(tracks), is(sec(seconds)));
  }

  private List<Integer> pattern(Integer... patternItems) {
    return Lists.newArrayList(patternItems);
  }

  private IAudioFile createAudioFileMockWithLength(Long durationInMilliseconds, String name) {
    IAudioFile result = mock(IAudioFile.class);
    when(result.getDuration()).thenReturn(durationInMilliseconds);
    when(result.getDisplayName()).thenReturn(name);
    when(result.toString()).thenReturn(name + " " + durationInMilliseconds/1000 + "s");
    return result;
  }


}
