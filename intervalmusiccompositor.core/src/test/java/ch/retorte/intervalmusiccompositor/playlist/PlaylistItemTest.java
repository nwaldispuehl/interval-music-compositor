package ch.retorte.intervalmusiccompositor.playlist;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests if the PlaylistItem works as expected.
 *
 * @author nw
 */
public class PlaylistItemTest {

  @Test
  public void shouldConsiderItselfSilentBreakIfAudioFileIsNull() {
    // when
    PlaylistItem playlistItem = new PlaylistItem(null, 1, 2);

    // then
    assertTrue(playlistItem.isSilentBreak());
  }

  @Test
  public void shouldReturnCorrectExtractDuration() {
    // given
    IAudioFile audioFile = mock(IAudioFile.class);
    when(audioFile.getDuration()).thenReturn(10000L);
    PlaylistItem playlistItem = new PlaylistItem(audioFile, 500L, 1500L);

    // when
    long duration = playlistItem.getExtractDurationInMilliseconds();

    // then
    assertThat(duration, is(1000L));

  }

}
