package ch.retorte.intervalmusiccompositor;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.MANUAL;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SHUFFLE;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SORT;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SORT_REV;
import static java.lang.Integer.valueOf;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFileComparator;
import ch.retorte.intervalmusiccompositor.audiofile.AudioFileFactory;
import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.cache.CreateCacheJob;
import ch.retorte.intervalmusiccompositor.cache.CreateCacheJobManager;
import ch.retorte.intervalmusiccompositor.commons.ArrayHelper;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.compilation.CompilationException;
import ch.retorte.intervalmusiccompositor.compilation.CompilationGenerator;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.messagebus.InfoMessage;
import ch.retorte.intervalmusiccompositor.messagebus.MessageBus;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.MusicCompilationControl;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.ProgramControl;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.audio.MusicPlayer;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.util.AudioFilesLoader;

import com.google.common.collect.Lists;

/**
 * Main controller of the software; collects data and reacts to ui events. Implements a lot of control interfaces.
 * 
 * @author nw
 */
public class MainControl implements MusicListControl, MusicCompilationControl, ProgramControl, ApplicationData {

  private static final int ONE_DAY_IN_MILLISECONDS = 86400000;

  private ListSortMode musicListSortMode = null;
  private int maxListEntries;

  private List<IAudioFile> musicList = Lists.newArrayList();
  private List<IAudioFile> breakList = Lists.newArrayList();

  private CreateCacheJobManager createCacheJobManager;

  private CompilationGenerator compilationGenerator;
  private AudioFileFactory audioFileFactory;
  private MusicPlayer musicPlayer;
  private MessageBus messageBus;
  private Ui ui;

  private Platform platform = new PlatformFactory().getPlatform();
  private String programName;
  private Version programVersion;
  private String temporaryFileSuffix;
  private int maximumImportWorkerThreads;

  public MainControl(CompilationGenerator compilationGenerator, AudioFileFactory audioFileFactory, MusicPlayer musicPlayer, MessageBus messageBus) {
    this.compilationGenerator = compilationGenerator;
    this.audioFileFactory = audioFileFactory;
    this.musicPlayer = musicPlayer;
    this.messageBus = messageBus;

    this.compilationGenerator.setMusicListControl(this);
    this.compilationGenerator.setApplicationData(this);

    setConfigurationProperties();
    createCreateCacheJobManager();
  }

  private void setConfigurationProperties() {
    MessageFormatBundle bundle = getBundle("imc");
    programName = bundle.getString("imc.name");
    programVersion = new Version(bundle.getString("imc.version"));

    MessageFormatBundle coreBundle = getBundle("core_imc");
    temporaryFileSuffix = coreBundle.getString("imc.temporaryFile.suffix");
    maxListEntries = valueOf(coreBundle.getString("imc.musicList.max_entries"));
    maximumImportWorkerThreads = valueOf(coreBundle.getString("imc.import.maximumWorkerThreads"));
  }

  public void setUi(Ui ui) {
    this.ui = ui;
  }

  private void createCreateCacheJobManager() {
    createCacheJobManager = new CreateCacheJobManager(messageBus, getThreadLimit());
  }

  /**
   * If we process too much tracks in parallel, it produces IO and may cause memory issues if the tracks are very big.
   */
  private int getThreadLimit() {
    return Math.min(maximumImportWorkerThreads, platform.getCores());
  }

  public void startCompilation(CompilationParameters compilationParameters) {
    addDebugMessage("Started compilation with: " + compilationParameters.musicPattern + " " + compilationParameters.breakPattern + " "
        + compilationParameters.iterations);

    compilationGenerator.addListener(new TaskFinishListener() {
      public void onTaskFinished() {
        ui.setEnvelopeImage(compilationGenerator.getEnvelope());
        ui.setActive();
      }
    });

    ui.setInactive();
    try {
      compilationGenerator.createCompilationWith(compilationParameters);
    }
    catch (CompilationException e) {
      messageBus.send(new ErrorMessage(e.getMessage()));
      ui.setActive();
    }
  }

  @Override
  public List<AudioFileEncoder> getAvailableEncoders() {
    List<AudioFileEncoder> availableEncoders = Lists.newArrayList();
    for (AudioFileEncoder e : compilationGenerator.getEncoders()) {
      if (e.isAbleToEncode()) {
        availableEncoders.add(e);
      }
    }
    return availableEncoders;
  }

  public void addMusicTrack(int i, File file) {
    if (musicList.size() < maxListEntries) {
      addTrack(musicList, i, file);
    }
    else {
      messageBus.send(new ErrorMessage("Too much tracks."));
    }
  }

  public void addBreakTrack(int i, File file) {
    if (breakList.size() < 1) {
      addTrack(breakList, i, file);
    }
  }

  private synchronized void addTrack(List<IAudioFile> audioFileList, int i, File f) {
    addDebugMessage("Added new file: " + f);

    IAudioFile audioFile = audioFileFactory.createAudioFileFrom(f);

    CreateCacheJob job = new CreateCacheJob(audioFile, messageBus);
    job.addListener(new TaskFinishListener() {

      @Override
      public void onTaskFinished() {
        ui.updateUsableTracks();
      }
    });

    createCacheJobManager.addNewJob(job);

    if (i < 0 || audioFileList.size() < i) {
      i = audioFileList.size();
    }
    audioFileList.add(i, audioFile);

    musicListSortMode = MANUAL;
  }

  public void removeMusicTracks(int[] list) {
    List<Integer> preparedList = ArrayHelper.prepareListForRemoval(list);
    for (int i : preparedList) {
      removeMusicTrack(i);
    }
  }

  public void removeMusicTrack(int i) {
    IAudioFile audioFile = musicList.get(i);
    musicList.remove(audioFile);
    removeTrackCache(audioFile);
  }

  public void removeBreakTracks(int[] list) {
    List<Integer> preparedList = ArrayHelper.prepareListForRemoval(list);
    for (int i : preparedList) {
      removeBreakTrack(i);
    }
  }

  public void removeBreakTrack(int i) {
    IAudioFile audioFile = breakList.get(i);
    breakList.remove(audioFile);
    removeTrackCache(audioFile);
  }

  private synchronized void removeTrackCache(IAudioFile audioFile) {
    addDebugMessage("Removed audio file: " + audioFile);

    // Check if there is another object with the same cache around
    // TODO: Does this still work?
    int sameInstances = 0;
    for (IAudioFile musicListFile : musicList) {
      if (audioFile.equals(musicListFile)) {
        sameInstances++;
      }
    }
    // Only delete cache if this is the last of its kind
    if (sameInstances < 2) {
      try {
        audioFile.removeCache();
      }
      catch (IOException e) {
        addDebugMessage(e.getMessage());
      }
    }

    ui.refresh();
  }

  public void loadAudioFiles() {
    AudioFilesLoader audioFilesLoader = new AudioFilesLoader(this, audioFileFactory, messageBus);
    audioFilesLoader.addListener(new TaskFinishListener() {

      @Override
      public void onTaskFinished() {
        musicListSortMode = SORT;
        ui.refresh();
      }
    });

    new Thread(audioFilesLoader).start();
  }

  public void quit() {
    removeCachedFiles();

    messageBus.send(new InfoMessage("Gracefully shutting down ..."));

    System.exit(0);
  }

  private void removeCachedFiles() {
    addDebugMessage("Removing cached files.");

    for (IAudioFile audioFile : musicList) {
      try {
        audioFile.removeCache();
      }
      catch (IOException e) {
        addDebugMessage(e.getMessage());
      }
    }
    musicList.clear();

    for (IAudioFile audioFile : breakList) {
      try {
        audioFile.removeCache();
      }
      catch (IOException e) {
        addDebugMessage(e.getMessage());
      }
    }
    breakList.clear();
  }

  public void tidyOldTemporaryFiles() {
    File tmpFile = null;
    try {
      tmpFile = File.createTempFile("imc", ".imc");
      File directory = new File(tmpFile.getParent());

      File[] fileList = directory.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(temporaryFileSuffix);
        }
      });

      for (File f : fileList) {

        if (f.lastModified() < System.currentTimeMillis() - ONE_DAY_IN_MILLISECONDS) {
          messageBus.send(new InfoMessage("Purging old temporary file: " + f));
          boolean deleted = f.delete();
          if (!deleted) {
            addDebugMessage("Was not able to delete temporary file: " + f);
          }
        }
      }

    }
    catch (Exception e) {
      addDebugMessage(e.getMessage());
    }
    finally {
      if (tmpFile != null) {
        tmpFile.delete();
      }
    }
  }

  /**
   * Returns the number of tracks which are to be used for the compilation with the currently selected compilation properties.
   */
  public int getUsableTracks(List<Integer> pattern) {
    int usableTracks = 0;
    int i = 0;
    for (IAudioFile audioFile : musicList) {
      if (audioFile.isOK() && audioFile.isLongEnoughFor(pattern.get(i % pattern.size()))) {
        usableTracks++;
      }
      i++;
    }

    return usableTracks;
  }

  /**
   * Returns the number of music and break tracks currently showing the in progress status.
   */
  private int getIdleTracks() {
    int idleTracks = 0;

    for (IAudioFile musicFile : musicList) {
      if (musicFile.isLoading()) {
        idleTracks++;
      }
    }

    for (IAudioFile breakFile : breakList) {
      if (breakFile.isLoading()) {
        idleTracks++;
      }
    }
    return idleTracks;
  }

  public boolean isTrackListReady() {
    return getIdleTracks() == 0;
  }

  @Override
  public ListSortMode getSortMode() {
    return musicListSortMode;
  }

  @Override
  public List<IAudioFile> getMusicList() {
    return Lists.newArrayList(musicList);
  }

  @Override
  public List<IAudioFile> getBreakList() {
    return Lists.newArrayList(breakList);
  }

  public void shuffleMusicList() {
    musicListSortMode = SHUFFLE;
    Collections.shuffle(musicList);
  }

  public void sortMusicList() {
    Collections.sort(musicList, new AudioFileComparator());

    if (musicListSortMode == SORT) {
      musicListSortMode = SORT_REV;
      Collections.reverse(musicList);
    }
    else {
      musicListSortMode = SORT;
    }
  }

  @Override
  public void playMusicTrack(int index) {
    if (0 <= index && index < musicList.size()) {
      musicPlayer.play(musicList.get(index));
    }
  }

  @Override
  public void playBreakTrack(int index) {
    if (0 <= index && index < breakList.size()) {
      musicPlayer.play(breakList.get(index));
    }
  }

  @Override
  public void stopMusic() {
    musicPlayer.stop();
  }

  public void writeMusicBpm(int[] list) {
    for (int i : list) {
      writeMusicBpm(i);
    }
  }

  public void writeBreakBpm(int[] list) {
    for (int i : list) {
      writeBreakBpm(i);
    }
  }

  public void writeMusicBpm(int index) {
    if (0 <= index && index < musicList.size()) {
      writeBpm(musicList.get(index));
    }
  }

  @Override
  public int getMusicBPM(int index) {
    if (0 <= index && index < musicList.size()) {
      return musicList.get(index).getBpm();
    }
    return -1;
  }

  @Override
  public void setMusicBpm(int index, int bpm) {
    if (0 <= index && index < musicList.size()) {
      musicList.get(index).setBpm(bpm);
    }
  }

  public void writeBreakBpm(int index) {
    if (0 <= index && index < breakList.size()) {
      writeBpm(breakList.get(index));
    }
  }

  @Override
  public int getBreakBpm(int index) {
    if (0 <= index && index < breakList.size()) {
      return breakList.get(index).getBpm();
    }
    return -1;
  }

  @Override
  public void setBreakBpm(int index, int bpm) {
    if (0 <= index && index < breakList.size()) {
      breakList.get(index).setBpm(bpm);
    }
  }

  private void writeBpm(IAudioFile audioFile) {
    try {
      audioFile.writeBpm(audioFile.getBpm());
    }
    catch (IOException e) {
      messageBus.send(new ErrorMessage("Not able to write BPM information: " + e.getMessage()));
      addDebugMessage(e.getMessage());
    }
    ui.refresh();
  }

  private void addDebugMessage(String message) {
    messageBus.send(new DebugMessage(this, message));
  }

  @Override
  public void moveTrack(int sourceIndex, int destinationIndex) {
    if (0 <= sourceIndex && sourceIndex < musicList.size()) {
      if (destinationIndex < 0 || musicList.size() < destinationIndex) {
        destinationIndex = musicList.size();
      }
      IAudioFile movedFile = musicList.remove(sourceIndex);
      musicList.add(destinationIndex, movedFile);
      musicListSortMode = MANUAL;
      ui.refresh();
    }
  }

  @Override
  public void moveMusicToBreak(int sourceIndex, int destinationIndex) {
    breakList.add(destinationIndex, musicList.remove(sourceIndex));
  }

  @Override
  public void moveBreakToMusic(int sourceIndex, int destinationIndex) {
    musicList.add(destinationIndex, breakList.remove(sourceIndex));
    musicListSortMode = MANUAL;
  }

  @Override
  public String getProgramName() {
    return programName;
  }

  @Override
  public Version getProgramVersion() {
    return programVersion;
  }

}
