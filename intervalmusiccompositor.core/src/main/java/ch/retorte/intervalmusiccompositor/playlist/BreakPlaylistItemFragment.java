package ch.retorte.intervalmusiccompositor.playlist;

/**
 * @author nw
 */
public class BreakPlaylistItemFragment extends PlaylistItemFragment {

  public BreakPlaylistItemFragment(PlaylistItemFragment playlistItemFragment) {
    super(playlistItemFragment.getAudioFile(), playlistItemFragment.getExtractStartInMilliseconds(), playlistItemFragment.getExtractEndInMilliseconds());
  }

}
