package ch.retorte.intervalmusiccompositor.spi;

import java.io.File;
import java.util.List;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;
import javafx.collections.ObservableList;

/**
 * Offers instruments to control the music list.
 * 
 * @author nw
 */
public interface MusicListControl {

  void moveMusicToBreak(int sourceIndex, int destinationIndex);

  void moveBreakToMusic(int sourceIndex, int destinationIndex);

  IAudioFile addMusicTrack(int index, File f);

  IAudioFile appendMusicTrack(File f);

  void removeMusicTrack(int index);

  void removeMusicTracks(int[] index);

  void writeMusicBpm(int[] list);

  void writeMusicBpm(int i);

  int getMusicBPM(int index);

  void setMusicBpm(int index, int bpm);

  IAudioFile addBreakTrack(int index, File f);

  IAudioFile appendBreakTrack(File f);

  void removeBreakTrack(int index);

  void removeBreakTracks(int[] index);

  void writeBreakBpm(int[] list);

  void writeBreakBpm(int i);

  int getBreakBpm(int index);

  void setBreakBpm(int index, int bpm);

  int getUsableTracks(List<Integer> pattern);

  void moveTrack(int sourceIndex, int destinationIndex);

  boolean isTrackListReady();

  ListSortMode getSortMode();

  ObservableList<IAudioFile> getMusicList();

  ObservableList<IAudioFile> getBreakList();

  void shuffleMusicList();

  void sortMusicList();

  void playMusicTrack(int index);

  void playBreakTrack(int index);

  void stopMusic();

}
