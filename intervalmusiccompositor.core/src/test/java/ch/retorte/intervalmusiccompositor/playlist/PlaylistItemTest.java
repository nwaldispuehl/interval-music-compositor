package ch.retorte.intervalmusiccompositor.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link PlaylistItem}.
 */
public class PlaylistItemTest {

    //---- Fields

    private final Playlist playlist = mock(Playlist.class);

    //---- Test methods

    @BeforeEach
    public void setup() {

    }

    @Test
    public void shouldGetItemLengthWithoutCrossFading() {
        // given
        when(playlist.isCrossFadingMode()).thenReturn(false);
        PlaylistItem sut = itemWith(fragmentWith(8000), fragmentWith(4000));

        // when
        long itemLengthMs = sut.getStrictItemLengthMs();

        // then
        assertEquals(12000L, itemLengthMs);
    }

    @Test
    public void shouldGetItemLengthWithCrossFading() {
        // given
        when(playlist.getBlendTimeMs()).thenReturn(2000L);
        when(playlist.isCrossFadingMode()).thenReturn(true);
        PlaylistItem sut = itemWith(fragmentWith(8000), fragmentWith(4000));

        // when
        long itemLengthMs = sut.getStrictItemLengthMs();

        // then
        assertEquals(8000L, itemLengthMs);
    }

    //---- Helper methods

    private PlaylistItem itemWith(PlaylistItemFragment musicFragment, PlaylistItemFragment breakFragment) {
        return new PlaylistItem(playlist, musicFragment, breakFragment, newArrayList());
    }

    private PlaylistItemFragment fragmentWith(long durationMs) {
        PlaylistItemFragment fragment = Mockito.mock(PlaylistItemFragment.class);
        when(fragment.getExtractDurationInMilliseconds()).thenReturn(durationMs);
        return fragment;
    }

}
