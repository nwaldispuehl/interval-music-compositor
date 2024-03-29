package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ProgressMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.SubProcessProgressMessage;
import ch.retorte.intervalmusiccompositor.model.util.VisibleForTesting;
import ch.retorte.intervalmusiccompositor.output.OutputGenerator;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistReport;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.ExceptionMessageExtractor.messageOrNameOf;
import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.SEPARATE;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author nw
 */
public class CompilationGenerator implements Runnable {

  private static final int MAXIMUM_BLEND_TIME = 10;
  private static final String FILENAME_PART_DELIMITER = ".";
  private static final String FILENAME_PREFIX_SOUND_MARKER = "s";
  private static final String FILENAME_PREFIX_BREAK_MARKER = "b";
  private static final String FILENAME_PREFIX_ITERATION_MARKER = "x";
  private static final String FILENAME_PREFIX_PART_DELIMITER = "_";
  private static final String FILENAME_PREFIX_ITERATION_DELIMITER = "-";


  private final Platform platform = new PlatformFactory().getPlatform();

  private final MessageFormatBundle bundle = new CoreBundleProvider().getBundle();

  private final ArrayList<TaskFinishListener> listeners = newArrayList();

  private MusicListControl musicListControl;
  private final Compilation compilation;
  private final MessageProducer messageProducer;
  private CompilationParameters compilationParameters;

  private final ArrayList<IAudioFile> musicPlaylistCandidates = newArrayList();
  private final ArrayList<IAudioFile> breakPlaylistCandidates = newArrayList();
  private final ArrayList<IAudioFile> badSoundFiles = newArrayList();

  private String playlistReportFilename;
  private String correctedOutputPath;
  private String musicFilenameWithoutExtension;

  private Playlist playlist;

  private File compilationDataFile;
  private long compilationDataSize;

  private final OutputGenerator outputGenerator;

  private BufferedImage envelope;

  private ApplicationData applicationData;

  public CompilationGenerator(Compilation compilation, OutputGenerator outputGenerator, MessageProducer messageProducer) {
    this.compilation = compilation;
    this.outputGenerator = outputGenerator;
    this.messageProducer = messageProducer;
  }

  @Override
  public void run() {

    updateProgress(0, bundle.getString("compilation.status.reading"));
    adjustBlendTimeIfTooLong();
    clearOldData();
    compileOutputParameters();

    updateProgress(10, bundle.getString("compilation.status.loadingMusic"));
    collectMusicTracks();

    updateProgress(20, bundle.getString("compilation.status.loadingBreak"));
    collectBreakTracks();

    updateProgress(30, bundle.getString("compilation.status.creatingPlaylist"));
    createPlaylist();
    createPlaylistReport();

    updateProgress(40, bundle.getString("compilation.status.creatingCompilation"));
    createCompilation();

    updateProgress(60, bundle.getString("compilation.status.convertingOutput"));
    writeOutputFile();

    updateProgress(90, bundle.getString("compilation.status.creatingEnvelope"));
    createEnvelope();

    cleanUp();
    notifyListeners();

    updateProgress(100, bundle.getString("compilation.status.finished"));
  }

  public void createCompilationWith(CompilationParameters compilationParameters) {
    this.compilationParameters = compilationParameters;

    new Thread(this).start();
  }

  public BufferedImage getEnvelope() {
    return envelope;
  }

  public void addListener(TaskFinishListener l) {
    listeners.add(l);
  }

  public void clearListeners() {
    listeners.clear();
  }

  private void notifyListeners() {
    listeners.forEach(TaskFinishListener::onTaskFinished);
  }

  private void adjustBlendTimeIfTooLong() {
    int shortestSoundPattern = MAXIMUM_BLEND_TIME;
    for (int s : compilationParameters.getMusicPattern()) {
      shortestSoundPattern = Math.min(shortestSoundPattern, s);
    }

    if (compilationParameters.hasBreakPattern()) {
      for (int b : compilationParameters.getBreakPattern()) {
        shortestSoundPattern = Math.min(shortestSoundPattern, b);
      }
    }

    if (compilationParameters.getBlendMode() == SEPARATE && shortestSoundPattern / 2.0 < compilationParameters.getBlendDuration()) {
      compilationParameters.setBlendDuration(Math.floor(shortestSoundPattern / 2.0));

    }
    else if (compilationParameters.getBlendMode() == CROSS && shortestSoundPattern < compilationParameters.getBlendDuration()) {
      compilationParameters.setBlendDuration((double) Math.min(shortestSoundPattern, 10));
    }
  }

  private void clearOldData() {
    musicPlaylistCandidates.clear();
    breakPlaylistCandidates.clear();
    badSoundFiles.clear();
  }

  private void compileOutputParameters() {
    correctedOutputPath = compilationParameters.getOutputPath();
    if (correctedOutputPath == null || correctedOutputPath.isEmpty()) {
      correctedOutputPath = bundle.getString("imc.workPath");
    }
    createIfNonExisting(new File(correctedOutputPath));

    final String playlistFileSuffix = bundle.getString("imc.outfile.playlist.suffix");
    final String musicFileSuffix = bundle.getString("imc.outfile.sound.infix");

    final int remainingPathLength = platform.getMaximumPathLength() - correctedOutputPath.length() - Math.max(playlistFileSuffix.length(), musicFileSuffix.length()) - 3;

    if (remainingPathLength <= 0) {
      throw new CompilationException(bundle.getString("ui.error.filetoolong"), null);
    }

    final String originalFilenamePrefix = createMusicAndBreakPatternPrefixWith(compilationParameters.getMusicPattern(), compilationParameters.getBreakPattern(), compilationParameters.getIterations());
    final String truncatedFilenamePrefix = truncateIfLongerThan(remainingPathLength, originalFilenamePrefix);

    playlistReportFilename = truncatedFilenamePrefix + FILENAME_PART_DELIMITER + playlistFileSuffix;
    musicFilenameWithoutExtension = truncatedFilenamePrefix + FILENAME_PART_DELIMITER + musicFileSuffix;
  }

  private String truncateIfLongerThan(int length, String s) {
    if (s != null && length < s.length() ) {
      final String truncated = s.substring(0, length - 1) + "x";
      addDebugMessage(String.format("Original file name\n'%s' (length %d)\ntoo long; truncated to:\n'%s' (length %d).", s, s.length(), truncated, truncated.length()));
      return truncated;
    }
    else {
      return s;
    }
  }

  private void createIfNonExisting(File f) {
    if (!f.exists()) {
      boolean success = f.mkdirs();
      addDebugMessage("Attempted to create non-existing output path: " + f + ". Success: " + success);
    }
  }

  @VisibleForTesting
  String createMusicAndBreakPatternPrefixWith(List<Integer> musicPattern, List<Integer> breakPattern, int iterations) {
    List<String> parts = new ArrayList<>();
    for (int i = 0; i < musicPattern.size(); i++) {
      parts.add(musicPattern.get(i) + FILENAME_PREFIX_SOUND_MARKER);
      if (hasAtLeastOneNonNullElement(breakPattern)) {
        parts.add(breakPattern.get(i % breakPattern.size()) + FILENAME_PREFIX_BREAK_MARKER);
      }
    }
    return join(FILENAME_PREFIX_PART_DELIMITER, parts) + FILENAME_PREFIX_ITERATION_DELIMITER + FILENAME_PREFIX_ITERATION_MARKER + iterations;
  }

  private boolean hasAtLeastOneNonNullElement(List<Integer> breakPattern) {
    for (int element : breakPattern) {
      if (element != 0) {
        return true;
      }
    }
    return false;
  }

  private void collectMusicTracks() {
    int i = 0;
    for (IAudioFile audioFile : musicListControl.getMusicList()) {
      if (audioFile.isOK() && audioFile.isLongEnoughFor(compilationParameters.getMusicPattern().get(i % compilationParameters.getMusicPattern().size()))) {
        musicPlaylistCandidates.add(audioFile);
      }
      else {
        badSoundFiles.add(audioFile);
      }
      i++;
    }
  }

  private void collectBreakTracks() {
    int i = 0;
    for (IAudioFile audioFile : musicListControl.getBreakList()) {
      if (audioFile.isOK() && audioFile.isLongEnoughFor(compilationParameters.getBreakPattern().get(i % compilationParameters.getBreakPattern().size()))) {
        breakPlaylistCandidates.add(audioFile);
      }
      else {
        badSoundFiles.add(audioFile);
      }
      i++;
    }
  }

  private void createPlaylist() {
    playlist = new Playlist(compilationParameters, messageProducer);
    playlist.generatePlaylist(musicPlaylistCandidates, compilationParameters.getMusicPattern(), breakPlaylistCandidates, compilationParameters.getBreakPattern(), compilationParameters.getBreakVolume(), compilationParameters.getIterations(), compilationParameters.getSoundEffectOccurrences());
  }

  private void createPlaylistReport() {
    File reportFile = new File(correctedOutputPath + "/" + playlistReportFilename);
    String playlistReport = new PlaylistReport(applicationData).generateReportFor(playlist, badSoundFiles);

    try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(reportFile), UTF_8)) {
      outputStreamWriter.write(playlistReport);
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.general.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
  }

  private void createCompilation() {
    try {
      compilationDataFile = File.createTempFile("compilation", bundle.getString("imc.temporaryFile.suffix"));
      addDebugMessage("Created temporary compilation file " + compilationDataFile.getAbsolutePath());

      FileOutputStream fileOutputStream = new FileOutputStream(compilationDataFile);
      compilation.generateCompilation(playlist, fileOutputStream);
      fileOutputStream.close();

      compilationDataSize = compilationDataFile.length();
      addDebugMessage("Wrote " + compilationDataSize + " bytes into temporary compilation file at " + compilationDataFile.getAbsolutePath());
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.sound.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
    catch (OutOfMemoryError e) {
      String message = bundle.getString("ui.error.memory.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
    catch (UnsupportedAudioFileException e) {
      String message = bundle.getString("ui.error.soundeffectformat.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
    catch (Exception e) {
      String message = bundle.getString("ui.error.general.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
  }

  private void writeOutputFile() {
    try (FileInputStream compilationData = new FileInputStream(compilationDataFile)) {
      outputGenerator.generateOutput(compilationData, compilationDataSize, correctedOutputPath, musicFilenameWithoutExtension, compilationParameters.getEncoderIdentifier(), new InertProgressListener() {

        @Override
        protected void onProgressChange(int percent) {
          updateSubProgress(percent);
        }
      });

    } catch (Exception e) {
      String message = bundle.getString("ui.error.general.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
  }

  private void createEnvelope() {
    Integer width = Integer.valueOf(bundle.getString("ui.envelope.width"));
    Integer height = Integer.valueOf(bundle.getString("ui.envelope.height"));

    EnvelopeImage envelopeImage = new EnvelopeImage(width, height);
    try (FileInputStream compilationData = new FileInputStream(compilationDataFile)) {
      envelopeImage.generateEnvelope(compilationData, compilationDataSize, compilationParameters.getMusicPattern(), compilationParameters.getBreakPattern(), compilationParameters.getIterations());
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.general.introduction");
      message += messageOrNameOf(e);
      throw new CompilationException(message, e);
    }
    envelope = envelopeImage.getBufferedImage();
  }

  private void cleanUp() {
    /* We want unused objects to be reclaimed. */
    if (compilationDataFile.delete()) {
      addDebugMessage("Cleaned up temporary compilation file " + compilationDataFile.getAbsolutePath());
    }
    else {
      addDebugMessage("Was not able to delete temporary compilation file " + compilationDataFile.getAbsolutePath());
    }
    System.gc();
  }

  private void updateProgress(Integer progressInPercent, String currentActivity) {
    messageProducer.send(new ProgressMessage(progressInPercent, currentActivity));
    addDebugMessage("Progress: " + progressInPercent + ", " + currentActivity);
  }

  private void updateSubProgress(Integer progressInPercent) {
    messageProducer.send(new SubProcessProgressMessage(progressInPercent));
    addDebugMessage("Progress of sub process: " + progressInPercent);
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  public void setMusicListControl(MusicListControl musicListControl) {
    this.musicListControl = musicListControl;
  }

  public void setApplicationData(ApplicationData applicationData) {
    this.applicationData = applicationData;
  }

  public List<AudioFileEncoder> getEncoders() {
    return outputGenerator.getEncoders();
  }

}
