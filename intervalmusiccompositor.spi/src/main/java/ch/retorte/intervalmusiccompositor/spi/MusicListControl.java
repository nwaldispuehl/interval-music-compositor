package ch.retorte.intervalmusiccompositor.spi;

import java.io.File;
import java.util.List;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.model.list.BlendMode;
import ch.retorte.intervalmusiccompositor.model.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.model.list.ObservableList;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;

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

  void markUsableTracksWith(CompilationParameters compilationParameters);

  boolean hasUsableTracksWith(CompilationParameters compilationParameters);

  long getOkTracks();

  void moveTrack(int sourceIndex, int destinationIndex);

  boolean isTrackListReady();

  ListSortMode getSortMode();

  ObservableList<IAudioFile> getMusicList();

  ObservableList<IAudioFile> getBreakList();

  void shuffleMusicList();

  void sortMusicList();

  void playMusicTrack(int index);

  void playBreakTrack(int index);

  void playSoundEffect(SoundEffect soundEffect);

  void stopMusic();

}
