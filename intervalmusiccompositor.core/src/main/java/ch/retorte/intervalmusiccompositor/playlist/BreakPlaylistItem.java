package ch.retorte.intervalmusiccompositor.playlist;

/**
 * @author nw
 */
public class BreakPlaylistItem extends PlaylistItem {

  public BreakPlaylistItem(PlaylistItem playlistItem) {
    super(playlistItem.getAudioFile(), playlistItem.getExtractStartInMilliseconds(), playlistItem.getExtractEndInMilliseconds());
  }

}
