package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests if the PlaylistItemFragment works as expected.
 *
 * @author nw
 */
public class PlaylistItemFragmentTest {

    @Test
    public void shouldConsiderItselfSilentBreakIfAudioFileIsNull() {
        // when
        PlaylistItemFragment playlistItemFragment = new PlaylistItemFragment(null, 1, 1, 2);

        // then
        assertTrue(playlistItemFragment.isSilentBreak());
    }

    @Test
    public void shouldReturnCorrectExtractDuration() {
        // given
        IAudioFile audioFile = mock(IAudioFile.class);
        when(audioFile.getDuration()).thenReturn(10000L);
        PlaylistItemFragment playlistItemFragment = new PlaylistItemFragment(audioFile, 1, 500L, 1500L);

        // when
        long duration = playlistItemFragment.getExtractDurationInMilliseconds();

        // then
        assertEquals(1000L, duration);

    }

}
