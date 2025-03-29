package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.SEPARATE;
import static ch.retorte.intervalmusiccompositor.model.list.EnumerationMode.CONTINUOUS;
import static ch.retorte.intervalmusiccompositor.model.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.model.list.ListSortMode.SHUFFLE;
import static ch.retorte.intervalmusiccompositor.model.list.ListSortMode.SORT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link Playlist}.
 */
public class PlaylistTest {

    //---- Statics

    private static final String MUSIC = "music";
    private static final String BREAK = "break";

    //---- Fields

    private final MessageProducer msgPrd = mock(MessageProducer.class);
    private List<IAudioFile> musicList;
    private List<Integer> musicPattern;
    private List<IAudioFile> breakList;
    private List<Integer> breakPattern;
    private List<SoundEffectOccurrence> soundEffectOccurrences;

    private final IAudioFile musicTrack20s = createAudioFileMockWithLength(sec(20), MUSIC);
    private final IAudioFile musicTrack40s = createAudioFileMockWithLength(sec(40), MUSIC);
    private final IAudioFile musicTrack60s = createAudioFileMockWithLength(sec(60), MUSIC);
    private final IAudioFile breakTrack = createAudioFileMockWithLength(sec(60), BREAK);
    private final IAudioFile breakTrack20s = createAudioFileMockWithLength(sec(20), BREAK);

    private final SoundEffect soundEffect1 = mock(SoundEffect.class);

    //---- Methods

    @BeforeEach
    public void setup() {
        musicList = newArrayList();
        musicPattern = newArrayList();
        breakList = newArrayList();
        breakPattern = newArrayList();
        soundEffectOccurrences = newArrayList();

        when(soundEffect1.getDurationMs()).thenReturn(2000.0);
    }

    @Test
    public void shouldProduceEmptyListOnEmptyPattern() {
        // given
        Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
        musicList.add(musicTrack20s);

        // when
        List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 3, soundEffectOccurrences);

        // then
        assertEquals(0, tracks.size());
    }

    @Test
    public void shouldProduceEmptyListOnEmptyTracks() {
        // given
        Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
        musicPattern = pattern(1, 2, 3);

        // when
        List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 3, soundEffectOccurrences);

        // then
        assertEquals(0, tracks.size());
    }

    @Test
    public void shouldGenerateTracksOfRightLength() {
        // given
        Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
        musicList.add(musicTrack20s);
        musicPattern = pattern(5, 10);

        // when
        List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences);

        // then
        assertEquals(2, tracks.size());
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
        assertEquals(2, tracks.size());
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
        List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 3, soundEffectOccurrences);

        // then
        assertEquals(6, tracks.size());
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
        assertEquals(6, tracks.size());
        assertTotalLength(playlist, tracks, 30);
        assertTrackLength(tracks, 2, 1, 4, 1, 6, 1, 2, 1, 4, 1, 6, 1);
        assertEquals(musicTrack20s, tracks.get(0).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(1).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(2).getMusicFragment().getAudioFile());
        assertEquals(musicTrack20s, tracks.get(3).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(4).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(5).getMusicFragment().getAudioFile());
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
        assertEquals(3, tracks.size());
        assertTotalLength(playlist, tracks, 33);
        assertTrackLength(tracks, 4, 5, 9, 5, 15, 5);
    }

    @Test
    public void shouldRejectTooShortTracks() {
        // given
        Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
        playlist.setCutOff(sec(5), sec(5));

        musicList.add(musicTrack20s);
        musicPattern = pattern(11);

        // when
        assertThrows(IllegalStateException.class, () -> 
            playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences)
        );
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
        assertEquals(1, tracks.size());
        assertTrackLength(tracks, 25);
        assertTotalLength(playlist, tracks, 25);
        assertTrue(sec(10) <= tracks.getFirst().getMusicFragment().getExtractStartInMilliseconds());
        assertTrue(tracks.getFirst().getMusicFragment().getExtractEndInMilliseconds() <= musicTrack60s.getDuration() - sec(10));
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
        assertEquals(3, tracks.size());
        assertTrackLength(tracks, 11, 1, 11, 1, 11, 1);
        assertTotalLength(playlist, tracks, 36);
        assertEquals(musicTrack60s, tracks.get(0).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(1).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(2).getMusicFragment().getAudioFile());
    }

    @Test
    public void shouldSkipTooShortTracksButNeverthelessCompletelyFillPatternInAdvancedMode() {
        // given
        Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
        playlist.setCutOff(sec(5), sec(5));

        musicList.add(musicTrack60s);
        musicList.add(musicTrack20s);
        musicList.add(musicTrack40s);

        musicPattern = pattern(10, 11);
        breakPattern = pattern(1, 2);

        // when
        List<PlaylistItem> tracks = playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 2, soundEffectOccurrences);

        // then
        assertEquals(4, tracks.size());
        assertTrackLength(tracks, 10, 1, 11, 2, 10, 1, 11, 2);
        assertTotalLength(playlist, tracks, 48);
        assertEquals(musicTrack60s, tracks.get(0).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(1).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(2).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(3).getMusicFragment().getAudioFile());
    }

    @Test
    public void shouldNotAllowNotSufficientBreakTrack() {
        // given
        Playlist playlist = new Playlist(SEPARATE, 0d, SINGLE_EXTRACT, SORT, msgPrd);
        playlist.setCutOff(sec(5), sec(5));

        musicList.add(musicTrack20s);
        breakList.add(breakTrack20s);

        musicPattern = pattern(8, 8, 8);
        breakPattern = pattern(8, 12, 8);

        // when this, then throw exception
        assertThrows(IllegalStateException.class, () ->
            playlist.generatePlaylistFrom(musicList, musicPattern, breakList, breakPattern, 1, 1, soundEffectOccurrences)
        );
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
        assertEquals(10, tracks.size());
        assertTrackLength(tracks, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        assertTotalLength(playlist, tracks, 100);
        assertEquals(musicTrack60s, tracks.get(0).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(1).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(2).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(3).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(4).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(5).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(6).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(7).getMusicFragment().getAudioFile());
        assertEquals(musicTrack20s, tracks.get(8).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(9).getMusicFragment().getAudioFile());
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
        assertEquals(9, tracks.size());
        assertTrackLength(tracks, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        assertTotalLength(playlist, tracks, 90);
        assertEquals(musicTrack60s, tracks.get(0).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(1).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(2).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(3).getMusicFragment().getAudioFile());
        assertEquals(musicTrack60s, tracks.get(4).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(5).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(6).getMusicFragment().getAudioFile());
        assertEquals(musicTrack40s, tracks.get(7).getMusicFragment().getAudioFile());
        assertEquals(musicTrack20s, tracks.get(8).getMusicFragment().getAudioFile());
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
        assertEquals(15000L, soundEffects.getFirst().getStartTimeMs());
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
        assertEquals(6000L, soundEffects.getFirst().getStartTimeMs());
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
        assertEquals(1, soundEffects.size());
        assertEquals(9000L, soundEffects.getFirst().getStartTimeMs());
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
        assertEquals(3, tracks.size());
        assertTrackLength(tracks, 10, 10, 10);
        assertTotalLength(playlist, tracks, 30);
        assertEquals(musicTrack20s, tracks.get(0).getMusicFragment().getAudioFile());
        assertEquals(musicTrack20s, tracks.get(1).getMusicFragment().getAudioFile());
        assertEquals(musicTrack20s, tracks.get(2).getMusicFragment().getAudioFile());
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
        assertEquals(4, tracks.size());
    }

    //---- Helper methods

    private long sec(int seconds) {
        return (long) seconds * 1000;
    }

    private void assertTrackLength(List<PlaylistItem> tracks, int... seconds) {
        int counter = 0;
        for (PlaylistItem item : tracks) {
            assertEquals(sec(seconds[counter]), item.getMusicFragment().getExtractDurationInMilliseconds());
            counter++;
            if (item.hasBreakFragment()) {
                assertEquals(sec(seconds[counter]), item.getBreakFragment().getExtractDurationInMilliseconds());
                counter++;
            }
        }
    }

    private void assertTotalLength(Playlist p, List<PlaylistItem> tracks, int seconds) {
        assertEquals(sec(seconds), new Playlist(msgPrd).getTotalLength(p, tracks));
    }

    private List<Integer> pattern(Integer... patternItems) {
        return asList(patternItems);
    }

    private IAudioFile createAudioFileMockWithLength(Long durationInMilliseconds, String name) {
        IAudioFile result = mock(IAudioFile.class);
        when(result.getDuration()).thenReturn(durationInMilliseconds);
        when(result.getDisplayName()).thenReturn(name);
        when(result.toString()).thenReturn(name + " " + durationInMilliseconds / 1000 + "s");
        return result;
    }


}
