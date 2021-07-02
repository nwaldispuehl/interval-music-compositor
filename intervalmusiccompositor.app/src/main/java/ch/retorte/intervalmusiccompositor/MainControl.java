package ch.retorte.intervalmusiccompositor;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFileFactory;
import ch.retorte.intervalmusiccompositor.bundle.AppBundleProvider;
import ch.retorte.intervalmusiccompositor.cache.CreateCacheJob;
import ch.retorte.intervalmusiccompositor.cache.CreateCacheJobManager;
import ch.retorte.intervalmusiccompositor.commons.ArrayHelper;
import ch.retorte.intervalmusiccompositor.commons.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.compilation.CompilationGenerator;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.messagebus.MessageBus;
import ch.retorte.intervalmusiccompositor.model.audiofile.AudioFileComparator;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.model.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.model.list.ObservableList;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.InfoMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.LogBuffer;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.*;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioFileMusicPlayer;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.util.AudioFilesLoader;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static ch.retorte.intervalmusiccompositor.model.list.ListSortMode.*;
import static java.util.stream.Collectors.joining;

//import javafx.collections.ObservableList;

/**
 * Main controller of the software; collects data and reacts to ui events. Implements a lot of control interfaces.
 */
class MainControl implements MusicListControl, MusicCompilationControl, ProgramControl, ApplicationData, SoundEffectsProvider {

    //---- Static

    private static final int ONE_DAY_IN_MILLISECONDS = 86400000;

    private static final String CHANGE_LOG_FILE = "/CHANGELOG.txt";

    //---- Fields

    private ListSortMode musicListSortMode = null;
    private int maxListEntries;

    private final ObservableList<IAudioFile> musicList = new ObservableList<>();
    private final ObservableList<IAudioFile> breakList = new ObservableList<>();

    private CreateCacheJobManager createCacheJobManager;

    private final CompilationGenerator compilationGenerator;
    private final AudioFileFactory audioFileFactory;
    private final AudioFileMusicPlayer musicPlayer;
    private final SoundEffectsProvider soundEffectsProvider;
    private final MessageBus messageBus;
    private final List<Locale> knownLocales;
    private Ui ui;

    private final Platform platform = new PlatformFactory().getPlatform();
    private String programName;
    private Version programVersion;
    private String changeLog;
    private String temporaryFileSuffix;
    private int maximumImportWorkerThreads;


    //---- Constructor

    MainControl(CompilationGenerator compilationGenerator, AudioFileFactory audioFileFactory, AudioFileMusicPlayer musicPlayer, SoundEffectsProvider soundEffectsProvider, MessageBus messageBus, List<Locale> knownLocales) {
        this.compilationGenerator = compilationGenerator;
        this.audioFileFactory = audioFileFactory;
        this.musicPlayer = musicPlayer;
        this.soundEffectsProvider = soundEffectsProvider;
        this.messageBus = messageBus;
        this.knownLocales = knownLocales;

        this.compilationGenerator.setMusicListControl(this);
        this.compilationGenerator.setApplicationData(this);

        setConfigurationProperties();
        createCreateCacheJobManager();
    }


    //---- Methods

    private void setConfigurationProperties() {
        MessageFormatBundle bundle = new AppBundleProvider().getBundle();
        programName = bundle.getString("imc.name");
        programVersion = new Version(bundle.getString("imc.version"));

        changeLog = loadChangeLog();

        MessageFormatBundle coreBundle = new CoreBundleProvider().getBundle();
        temporaryFileSuffix = coreBundle.getString("imc.temporaryFile.suffix");
        maxListEntries = Integer.parseInt(coreBundle.getString("imc.musicList.max_entries"));
        maximumImportWorkerThreads = Integer.parseInt(coreBundle.getString("imc.import.maximumWorkerThreads"));
    }

    private String loadChangeLog() {
        try (InputStream changeLogInputStream = getClass().getResourceAsStream(CHANGE_LOG_FILE)) {
            return new BufferedReader(new InputStreamReader(changeLogInputStream)).lines().collect(joining(System.lineSeparator()));
        } catch (Exception e) {
            addDebugMessage(e);
        }
        return null;
    }

    void setUi(Ui ui) {
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
        if (!compilationParameters.hasUsableData()) {
            addDebugMessage("Compilation must not be of 0 length; aborting.");
            return;
        }

        addDebugMessage("Started compilation with: " + compilationParameters.getMusicPattern() + " " + compilationParameters.getBreakPattern() + " " + compilationParameters.getIterations());

        if (!hasUsableTracksWith(compilationParameters)) {
            addDebugMessage("Not enough usable tracks; aborting.");
            return;
        }

        compilationGenerator.clearListeners();
        compilationGenerator.addListener(() -> {
            ui.setEnvelopeImage(compilationGenerator.getEnvelope());
            ui.setActive();
        });

        ui.setInactive();
        try {
            compilationGenerator.createCompilationWith(compilationParameters);
        } catch (Exception e) {
            messageBus.send(new ErrorMessage(e.getMessage()));
            addDebugMessage(e);
            ui.setActive();
        }
    }

    private boolean hasUsableTracksWith(CompilationParameters compilationParameters) {
        return 0 < getUsableTracks(compilationParameters.getMusicPattern());
    }

    @Override
    public List<AudioFileDecoder> getAvailableDecoders() {
        return newArrayList(audioFileFactory.getDecoders());
    }

    @Override
    public List<AudioFileEncoder> getAvailableEncoders() {
        return compilationGenerator.getEncoders().stream().filter(AudioFileEncoder::isAbleToEncode).collect(Collectors.toList());
    }

    public IAudioFile addMusicTrack(int i, File file) {
        if (musicList.size() < maxListEntries) {
            return addTrack(musicList, i, file);
        } else {
            messageBus.send(new ErrorMessage("Too much tracks."));
        }
        return null;
    }

    public IAudioFile appendMusicTrack(File file) {
        return addMusicTrack(getMusicList().size(), file);
    }

    public IAudioFile addBreakTrack(int i, File file) {
        if (breakList.size() < 1) {
            return addTrack(breakList, i, file);
        }
        return null;
    }

    public IAudioFile appendBreakTrack(File file) {
        return addBreakTrack(getBreakList().size(), file);
    }

    private synchronized IAudioFile addTrack(List<IAudioFile> audioFileList, int i, File f) {
        addDebugMessage("Added new file: " + f);

        IAudioFile audioFile = audioFileFactory.createAudioFileFrom(f);

        CreateCacheJob job = new CreateCacheJob(audioFile, messageBus);
        createCacheJobManager.addNewJob(job);

        if (i < 0 || audioFileList.size() < i) {
            i = audioFileList.size();
        }
        audioFileList.add(i, audioFile);

        musicListSortMode = MANUAL;

        return audioFile;
    }

    public void removeMusicTracks(int[] list) {
        List<Integer> preparedList = ArrayHelper.prepareListForRemoval(list);
        preparedList.forEach(this::removeMusicTrack);
    }

    public void removeMusicTrack(int i) {
        IAudioFile audioFile = musicList.get(i);
        musicList.remove(audioFile);
        removeTrackCache(audioFile);
    }

    public void removeBreakTracks(int[] list) {
        List<Integer> preparedList = ArrayHelper.prepareListForRemoval(list);
        preparedList.forEach(this::removeBreakTrack);
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
            } catch (IOException e) {
                addDebugMessage(e.getMessage());
            }
        }
    }

    void loadAudioFiles() {
        AudioFilesLoader audioFilesLoader = new AudioFilesLoader(this, audioFileFactory, messageBus);
        audioFilesLoader.addListener(() -> musicListSortMode = SORT);

        new Thread(audioFilesLoader).start();
    }

    public void quit() {
        removeCachedFiles();
        messageBus.send(new InfoMessage("Gracefully shutting down ..."));
    }

    @Override
    public LogBuffer getLogBuffer() {
        return messageBus.getLogBuffer();
    }

    private void removeCachedFiles() {
        addDebugMessage("Removing cached files.");

        for (IAudioFile audioFile : musicList) {
            try {
                audioFile.removeCache();
            } catch (IOException e) {
                addDebugMessage(e.getMessage());
            }
        }

        for (IAudioFile audioFile : breakList) {
            try {
                audioFile.removeCache();
            } catch (IOException e) {
                addDebugMessage(e.getMessage());
            }
        }
    }

    /**
     * Removes all files with the temporary file suffix (usually '.imc_wav') found in the systems temporary directory which are older than one day. We use this waiting period of a day as a poor mans way to prevent multiple instances of the software conflicting with each other.
     */
    void tidyOldTemporaryFiles() {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("imc", ".imc");
            File directory = new File(tmpFile.getParent());
            addDebugMessage("Looking for old temporary files to delete in: " + directory);

            File[] fileList = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(temporaryFileSuffix));

            if (fileList != null) {
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

        } catch (Exception e) {
            addDebugMessage(e.getMessage());
        } finally {
            if (tmpFile != null) {
                if (!tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
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
            if (audioFile.isOK() && audioFile.isLongEnoughFor(getIthPatternOf(pattern, i))) {
                usableTracks++;
            }
            i++;
        }

        return usableTracks;
    }

    private int getIthPatternOf(List<Integer> pattern, int i) {
        if (pattern == null || pattern.isEmpty()) {
            return 0;
        }
        return pattern.get(i % pattern.size());
    }

    @Override
    public int getOkTracks() {
        return (int) musicList.stream().filter(IAudioFile::isOK).count();
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
    public ObservableList<IAudioFile> getMusicList() {
        return musicList;
    }

    @Override
    public ObservableList<IAudioFile> getBreakList() {
        return breakList;
    }

    public void shuffleMusicList() {
        musicListSortMode = SHUFFLE;
        Collections.shuffle(musicList);
    }

    public void sortMusicList() {
        musicList.sort(new AudioFileComparator());

        if (musicListSortMode == SORT) {
            musicListSortMode = SORT_REV;
            Collections.reverse(musicList);
        } else {
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
    public void playSoundEffect(SoundEffect soundEffect) {
        try {
            musicPlayer.play(new AudioStreamUtil().audioInputStreamFrom(soundEffect.getResourceStream()));
        } catch (Exception e) {
            messageBus.send(new ErrorMessage("Not able to play back sound effect: " + e.getMessage()));
            addDebugMessage(e);
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
            addDebugMessage("Wrote BPM value '" + audioFile.getBpm() + "' into file: " + audioFile.getDisplayName());
        } catch (IOException e) {
            messageBus.send(new ErrorMessage("Not able to write BPM information: " + e.getMessage()));
            addDebugMessage(e);
        }
    }

    private void addDebugMessage(String message) {
        messageBus.send(new DebugMessage(this, message));
    }

    private void addDebugMessage(Throwable throwable) {
        messageBus.send(new DebugMessage(this, throwable));
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

    @Override
    public String getChangeLog() {
        return changeLog;
    }

    @Override
    public List<Locale> getKnownLocales() {
        return knownLocales;
    }

    @Override
    public Collection<SoundEffect> getSoundEffects() {
        return soundEffectsProvider.getSoundEffects();
    }
}
