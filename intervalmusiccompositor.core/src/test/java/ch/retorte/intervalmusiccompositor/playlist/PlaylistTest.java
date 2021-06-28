package ch.retorte.intervalmusiccompositor.playlist;

import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.SEPARATE;
import static ch.retorte.intervalmusiccompositor.model.list.EnumerationMode.CONTINUOUS;
import static ch.retorte.intervalmusiccompositor.model.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.model.list.ListSortMode.SHUFFLE;
import static ch.retorte.intervalmusiccompositor.model.list.ListSortMode.SORT;
import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

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
  private IAudioFile breakTrack20s = createAudioFileMockWithLength(sec(20), BREAK);

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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern,  1,3, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(0));
  }

  @Test
  public void shouldProduceEmptyListOnEmptyTracks() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    musicPattern = pattern(1, 2, 3);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1,  3, soundEffectOccurrences);

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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1,  1, soundEffectOccurrences);

    // then
    Assert.assertThat(tracks.size(), CoreMatchers.is(2));
    assertTrackLength(tracks, 5, 10);
    assertTotalLength(playlist, tracks, 15);
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(2));
    assertTrackLength(tracks, 5, 2, 10, 2);
    assertTotalLength(playlist, tracks, 19);
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1,3, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(6));
    assertTrackLength(tracks, 9, 1, 7, 2, 9, 1, 7, 2, 9, 1, 7, 2);
    assertTotalLength(playlist, tracks, 57);
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 2, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(6));
    assertTotalLength(playlist, tracks, 30);
    assertTrackLength(tracks, 2, 1, 4, 1, 6, 1, 2, 1, 4, 1, 6, 1);
    assertThat(tracks.get(0).getMusicFragment().getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(1).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(2).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(3).getMusicFragment().getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(4).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(5).getMusicFragment().getAudioFile(), is(musicTrack60s));
  }

  @Test
  public void shouldIncreaseExtractLengthOnCrossFadeByTransitionDuration() {
    // given
    Playlist playlist = new Playlist(CROSS, 2d, SINGLE_EXTRACT, SORT, msgPrd);
    musicList.add(musicTrack40s);
    musicPattern = pattern(2, 7, 13);
    breakList.add(breakTrack);
    breakPattern = pattern(3);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(3));
    assertTotalLength(playlist, tracks, 33);
    assertTrackLength(tracks, 4, 5, 9, 5, 15, 5);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectTooShortTracks() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    playlist.setCutOff(sec(5), sec(5));

    musicList.add(musicTrack20s);
    musicPattern = pattern(11);

    // when
    playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences);
  }

  @Test
  public void shouldUseExtractFromInsideTheBoundaries() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
    playlist.setCutOff(sec(10), sec(10));

    musicList.add(musicTrack60s);
    musicPattern = pattern(25);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(1));
    assertTrackLength(tracks, 25);
    assertTotalLength(playlist, tracks, 25);
    assertTrue(sec(10) <= tracks.get(0).getMusicFragment().getExtractStartInMilliseconds());
    assertTrue(tracks.get(0).getMusicFragment().getExtractEndInMilliseconds() <= musicTrack60s.getDuration() - sec(10));
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 3, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(3));
    assertTrackLength(tracks, 11, 1, 11, 1, 11, 1);
    assertTotalLength(playlist, tracks, 36);
    assertThat(tracks.get(0).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(1).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(2).getMusicFragment().getAudioFile(), is(musicTrack60s));
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 10, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(10));
    assertTrackLength(tracks, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
    assertTotalLength(playlist, tracks, 100);
    assertThat(tracks.get(0).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(1).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(2).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(3).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(4).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(5).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(6).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(7).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(8).getMusicFragment().getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(9).getMusicFragment().getAudioFile(), is(musicTrack60s));
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 9, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(9));
    assertTrackLength(tracks, 10, 10, 10, 10, 10, 10, 10, 10, 10);
    assertTotalLength(playlist, tracks, 90);
    assertThat(tracks.get(0).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(1).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(2).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(3).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(4).getMusicFragment().getAudioFile(), is(musicTrack60s));
    assertThat(tracks.get(5).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(6).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(7).getMusicFragment().getAudioFile(), is(musicTrack40s));
    assertThat(tracks.get(8).getMusicFragment().getAudioFile(), is(musicTrack20s));
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
    playlist.generatePlaylist(musicList, musicPattern, breakList, breakPattern, 1, 3, soundEffectOccurrences);

    // then
    assertTrue(playlist.hasSoundEffects());
    List<SoundEffectOccurrence> soundEffects = playlist.iterator().next().getSoundEffects();
    assertThat(soundEffects.get(0).getTimeMillis(), is(15000L));
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
    playlist.generatePlaylist(musicList, musicPattern, breakList, breakPattern, 1, 5, soundEffectOccurrences);

    // then
    assertTrue(playlist.hasSoundEffects());
    List<SoundEffectOccurrence> soundEffects = playlist.iterator().next().getSoundEffects();
    assertThat(soundEffects.get(0).getTimeMillis(), is(6000L));
  }

  @Test
  public void shouldNotPlaceSpecialEffectIfItExceedsTotalLength() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SHUFFLE, msgPrd);
    musicList.add(musicTrack60s);
    musicPattern = pattern(10);
    soundEffectOccurrences.add(new SoundEffectOccurrence(soundEffect1, 9000));

    // when
    playlist.generatePlaylist(musicList, musicPattern, breakList, breakPattern, 1, 2, soundEffectOccurrences);

    // then
    assertTrue(playlist.hasSoundEffects());
    List<SoundEffectOccurrence> soundEffects = playlist.iterator().next().getSoundEffects();
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
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 3, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(3));
    assertTrackLength(tracks, 10, 10, 10);
    assertTotalLength(playlist, tracks, 30);
    assertThat(tracks.get(0).getMusicFragment().getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(1).getMusicFragment().getAudioFile(), is(musicTrack20s));
    assertThat(tracks.get(2).getMusicFragment().getAudioFile(), is(musicTrack20s));
  }

  @Test
  public void shouldLoopMusicWithContinuousExtract() {
    // given
    Playlist playlist = new Playlist(SEPARATE, 0d, CONTINUOUS, SORT, msgPrd);
    musicList.add(musicTrack20s);
    breakList.add(breakTrack20s);

    musicPattern = pattern(10);
    breakPattern = pattern(10);

    // when
    List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 4, soundEffectOccurrences);

    // then
    assertThat(tracks.size(), is(4));
  }

  //---- Helper methods

  private long sec(int seconds) {
    return (long) seconds * 1000;
  }

  private void assertTrackLength(List<PlaylistItem> tracks, int... seconds) {
    int counter = 0;
    for (PlaylistItem item : tracks) {
      assertThat(item.getMusicFragment().getExtractDurationInMilliseconds(), is(sec(seconds[counter])));
      counter++;
      if (item.hasBreakFragment()) {
        assertThat(item.getBreakFragment().getExtractDurationInMilliseconds(), is(sec(seconds[counter])));
        counter++;
      }
    }
  }

  private void assertTotalLength(Playlist p, List<PlaylistItem> tracks, int seconds) {
    assertThat(new Playlist(msgPrd).getTotalLength(p, tracks), is(sec(seconds)));
  }

  private List<Integer> pattern(Integer... patternItems) {
    return asList(patternItems);
  }

  private IAudioFile createAudioFileMockWithLength(Long durationInMilliseconds, String name) {
    IAudioFile result = mock(IAudioFile.class);
    when(result.getDuration()).thenReturn(durationInMilliseconds);
    when(result.getDisplayName()).thenReturn(name);
    when(result.toString()).thenReturn(name + " " + durationInMilliseconds/1000 + "s");
    return result;
  }


}
