package ch.retorte.intervalmusiccompositor.compilation;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.SEPARATE;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ProgressMessage;
import ch.retorte.intervalmusiccompositor.messagebus.SubProcessProgressMessage;
import ch.retorte.intervalmusiccompositor.output.OutputGenerator;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistReport;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import javafx.scene.image.WritableImage;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author nw
 */
public class CompilationGenerator implements Runnable {

  private static final int MAXIMUM_BLEND_TIME = 20;
  private static final String FILENAME_PART_DELIMITER = ".";
  private static final String FILENAME_PREFIX_SOUND_MARKER = "s";
  private static final String FILENAME_PREFIX_BREAK_MARKER = "b";
  private static final String FILENAME_PREFIX_ITERATION_MARKER = "x";
  private static final String FILENAME_PREFIX_PART_DELIMITER = "_";
  private static final String FILENAME_PREFIX_ITERATION_DELIMITER = "-";

  private MessageFormatBundle bundle = getBundle("core_imc");

  private ArrayList<TaskFinishListener> listeners = newArrayList();

  private MusicListControl musicListControl;
  private Compilation compilation;
  private MessageProducer messageProducer;
  private CompilationParameters compilationParameters;

  private ArrayList<IAudioFile> musicPlaylistCandidates = newArrayList();
  private ArrayList<IAudioFile> breakPlaylistCandidates = newArrayList();
  private ArrayList<IAudioFile> badSoundFiles = newArrayList();

  private String playlist_outfile;
  private String correctedOutputPath;
  private String outfile_prefix;

  private Playlist playlist;

  private File compilationDataFile;
  private long compilationDataSize;

  private OutputGenerator outputGenerator;

  private WritableImage envelope;

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

  public WritableImage getEnvelope() {
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

    if (compilationParameters.getBlendMode().equals(SEPARATE) && shortestSoundPattern / 2 < compilationParameters.getBlendDuration()) {
      compilationParameters.setBlendDuration(Math.floor(shortestSoundPattern / 2.0));

    }
    else if (compilationParameters.getBlendMode().equals(BlendMode.CROSS) && shortestSoundPattern < compilationParameters.getBlendDuration()) {
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
    if (correctedOutputPath == null || correctedOutputPath.equals("")) {
      correctedOutputPath = bundle.getString("imc.workPath");
    }

    String identification_prefix = createMusicAndBreakPatternPrefixWith(compilationParameters.getMusicPattern(), compilationParameters.getBreakPattern(), compilationParameters.getIterations());
    playlist_outfile = identification_prefix + FILENAME_PART_DELIMITER + bundle.getString("imc.outfile.playlist.suffix");
    outfile_prefix = identification_prefix + FILENAME_PART_DELIMITER + bundle.getString("imc.outfile.sound.infix");
  }

  @VisibleForTesting
  String createMusicAndBreakPatternPrefixWith(List<Integer> musicPattern, List<Integer> breakPattern, int iterations) {
    List<String> parts = Lists.newArrayList();
    for (int i = 0; i < musicPattern.size(); i++) {
      parts.add(valueOf(musicPattern.get(i)) + FILENAME_PREFIX_SOUND_MARKER);
      if (hasAtLeastOneNonNullElement(breakPattern)) {
        parts.add(valueOf(breakPattern.get(i % breakPattern.size())) + FILENAME_PREFIX_BREAK_MARKER);
      }
    }
    return Joiner.on(FILENAME_PREFIX_PART_DELIMITER).join(parts) + FILENAME_PREFIX_ITERATION_DELIMITER + FILENAME_PREFIX_ITERATION_MARKER + valueOf(iterations);
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
    playlist.generatePlaylist(musicPlaylistCandidates, compilationParameters.getMusicPattern(), breakPlaylistCandidates, compilationParameters.getBreakPattern(), compilationParameters.getIterations(), compilationParameters.getSoundEffectOccurrences());
  }

  private void createPlaylistReport() {
    File reportFile = new File(correctedOutputPath + "/" + playlist_outfile);
    String playlistReport = new PlaylistReport(applicationData).generateReportFor(playlist, badSoundFiles);

    try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(reportFile), Charsets.UTF_8)) {
      outputStreamWriter.write(playlistReport);
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.introduction");
      message += e.getMessage();

      throw new CompilationException(message);
    }
  }

  private void createCompilation() {
    try {
      byte[] compilationBytes = compilation.generateCompilation(playlist);

      compilationDataFile = File.createTempFile("compilation", bundle.getString("imc.temporaryFile.suffix"));
      compilationDataSize = compilationBytes.length;
      addDebugMessage("Created temporary compilation file " + compilationDataFile.getAbsolutePath() + " for size " + compilationDataSize);

      writeToCompilationDataFile(compilationBytes);
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.sound.introduction");
      message += e.getMessage();
      throw new CompilationException(message);
    }
    catch (OutOfMemoryError e) {
      String message = bundle.getString("ui.error.memory.introduction");
      message += e.getMessage();
      throw new CompilationException(message);
    }
    catch (UnsupportedAudioFileException e) {
      String message = bundle.getString("ui.error.soundeffectformat.introduction");
      message += e.getMessage();
      throw new CompilationException(message);
    }
  }

  private void writeToCompilationDataFile(byte[] compilationBytes) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(compilationDataFile)) {
      fos.write(compilationBytes);
    }
  }

  private void writeOutputFile() {
    try (FileInputStream compilationData = new FileInputStream(compilationDataFile)) {
      outputGenerator.generateOutput(compilationData, compilationDataSize, correctedOutputPath, outfile_prefix, compilationParameters.getEncoderIdentifier(), new InertProgressListener() {

        @Override
        protected void onProgressChange(int percent) {
          updateSubProgress(percent);
        }
      });

    } catch (Exception e) {
      String message = bundle.getString("ui.error.introduction");
      message += e.getMessage();
      throw new CompilationException(message);
    }
  }

  private byte[] readCompilationDataFileIntoByteArray() throws IOException {
    return Files.readAllBytes(compilationDataFile.toPath());
  }


  private void createEnvelope() {
    Integer width = Integer.valueOf(bundle.getString("ui.envelope.width"));
    Integer height = Integer.valueOf(bundle.getString("ui.envelope.height"));

    EnvelopeImage envelopeImage = new EnvelopeImage(width, height);
    try {
      envelopeImage.generateEnvelope(readCompilationDataFileIntoByteArray(), compilationParameters.getMusicPattern(), compilationParameters.getBreakPattern(), compilationParameters.getIterations());
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.introduction");
      message += e.getMessage();
      throw new CompilationException(message);
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
