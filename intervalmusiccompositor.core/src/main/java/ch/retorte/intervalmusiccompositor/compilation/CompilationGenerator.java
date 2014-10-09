package ch.retorte.intervalmusiccompositor.compilation;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.SEPARATE;
import static com.google.common.collect.Lists.newArrayList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.ArrayHelper;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ProgressMessage;
import ch.retorte.intervalmusiccompositor.output.OutputGenerator;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistReport;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import com.google.common.base.Charsets;

/**
 * @author nw
 */
public class CompilationGenerator implements Runnable {

  public static final int MAXIMUM_BLEND_TIME = 20;

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

  private byte[] compilationData;

  private OutputGenerator outputGenerator;

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
    compileOutputFileNames();

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

  private void notifyListeners() {
    for (final TaskFinishListener l : listeners) {
      l.onTaskFinished();
    }
  }

  private void adjustBlendTimeIfTooLong() {
    int shortestSoundPattern = MAXIMUM_BLEND_TIME;
    for (int s : compilationParameters.musicPattern) {
      shortestSoundPattern = Math.min(shortestSoundPattern, s);
    }

    if (compilationParameters.blendMode.equals(SEPARATE) && shortestSoundPattern / 2 < compilationParameters.blendTime) {
      compilationParameters.blendTime = Math.floor(shortestSoundPattern / 2.0);

    }
    else if (compilationParameters.blendMode.equals(BlendMode.CROSS) && shortestSoundPattern < compilationParameters.blendTime) {
      compilationParameters.blendTime = Math.min(shortestSoundPattern, 10);
    }
  }

  private void clearOldData() {
    musicPlaylistCandidates.clear();
    breakPlaylistCandidates.clear();
    badSoundFiles.clear();
  }

  private void compileOutputFileNames() {
    correctedOutputPath = compilationParameters.outputPath;
    if (correctedOutputPath == null || correctedOutputPath.equals("")) {
      correctedOutputPath = bundle.getString("imc.workPath");
    }

    String identification_prefix = ArrayHelper.prettyPrintList(compilationParameters.musicPattern) + "_"
        + ArrayHelper.prettyPrintList(compilationParameters.breakPattern) + "_" + compilationParameters.iterations + ".";
    playlist_outfile = identification_prefix + bundle.getString("imc.outfile.playlist.suffix");
    outfile_prefix = identification_prefix + bundle.getString("imc.outfile.sound.infix");
  }

  private void collectMusicTracks() {
    int i = 0;
    for (IAudioFile audioFile : musicListControl.getMusicList()) {
      if (audioFile.isOK() && audioFile.isLongEnoughFor(compilationParameters.musicPattern.get(i % compilationParameters.musicPattern.size()))) {
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
      if (audioFile.isOK() && audioFile.isLongEnoughFor(compilationParameters.breakPattern.get(i % compilationParameters.breakPattern.size()))) {
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
    playlist.generatePlaylist(musicPlaylistCandidates, compilationParameters.musicPattern, breakPlaylistCandidates, compilationParameters.breakPattern,
        compilationParameters.iterations);
  }

  private void createPlaylistReport() {
    File reportFile = new File(correctedOutputPath + "/" + playlist_outfile);
    String playlistReport = new PlaylistReport(applicationData).generateReportFor(playlist, badSoundFiles);

    try {
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(reportFile), Charsets.UTF_8);
      outputStreamWriter.write(playlistReport);
      outputStreamWriter.close();
    }
    catch (IOException e) {
      String message = bundle.getString("ui.error.introduction");
      message += e.getMessage();

      throw new CompilationException(message);
    }
  }

  private void createCompilation() {

    try {
      compilationData = compilation.generateCompilation(playlist);
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
  }

  private void writeOutputFile() {
    outputGenerator.generateOutput(compilationData, correctedOutputPath, outfile_prefix);
  }

  private void createEnvelope() {
    Integer width = Integer.valueOf(bundle.getString("ui.envelope.width"));
    Integer height = Integer.valueOf(bundle.getString("ui.envelope.height"));

    EnvelopeImage envelopeImage = new EnvelopeImage(width, height);
    envelopeImage.generateEnvelope(compilationData, compilationParameters.musicPattern, compilationParameters.breakPattern, compilationParameters.iterations);
    envelope = envelopeImage.getBufferedImage();
  }

  private void cleanUp() {
    /* We want unused objects to be reclaimed. */
    compilationData = null;
    System.gc();
  }

  private void updateProgress(Integer progressInPercent, String currentActivity) {
    messageProducer.send(new ProgressMessage(progressInPercent, currentActivity));
    addDebugMessage("Progress: " + progressInPercent + ", " + currentActivity);
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
}
