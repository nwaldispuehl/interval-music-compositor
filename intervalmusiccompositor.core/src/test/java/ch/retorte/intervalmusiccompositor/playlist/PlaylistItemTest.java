package ch.retorte.intervalmusiccompositor.playlist;

import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link PlaylistItem}.
 */
public class PlaylistItemTest {

  //---- Fields

  private Playlist playlist = mock(Playlist.class);

  //---- Test methods

  @Before
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
    assertThat(itemLengthMs, is(12000L));
  }

  @Test
  public void shouldGetItemLengthWithCrossFading() {
    // given
    when(playlist.getBlendTimeMs()).thenReturn(2000L);
    when(playlist.isCrossFadingMode()).thenReturn(true);
    PlaylistItem sut = itemWith( fragmentWith(8000), fragmentWith(4000));

    // when
    long itemLengthMs = sut.getStrictItemLengthMs();

    // then
    assertThat(itemLengthMs, is(8000L));
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
