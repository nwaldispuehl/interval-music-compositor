package ch.retorte.intervalmusiccompositor;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static com.google.common.collect.Lists.newArrayList;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;

import at.ofai.music.beatroot.BeatRoot;
import ch.retorte.intervalmusiccompositor.audiofile.AudioFileFactory;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.compilation.Compilation;
import ch.retorte.intervalmusiccompositor.compilation.CompilationGenerator;
import ch.retorte.intervalmusiccompositor.decoder.AacAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.FlacAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.FlacBPMReaderWriter;
import ch.retorte.intervalmusiccompositor.decoder.Mp3AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.Mp3BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.decoder.OggAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.OggBPMReaderWriter;
import ch.retorte.intervalmusiccompositor.decoder.WaveAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.encoder.Mp3AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.encoder.WaveAudioFileEncoder;
import ch.retorte.intervalmusiccompositor.messagebus.ConsoleMessageHandler;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessageHandler;
import ch.retorte.intervalmusiccompositor.messagebus.MessageBus;
import ch.retorte.intervalmusiccompositor.output.OutputGenerator;
import ch.retorte.intervalmusiccompositor.player.ExtractMusicPlayer;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.audio.ByteArrayConverter;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMCalculator;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.ui.SwingUserInterface;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;
import ch.retorte.intervalmusiccompositor.util.UpdateChecker;

import com.google.common.collect.Lists;

/**
 * The {@link IntervalMusicCompositor} is the main program file of the software.
 * 
 * @author nw
 */
public class IntervalMusicCompositor {

  private MessageFormatBundle bundle = getBundle("imc");

  private MessageBus messageBus = createMessageBus();
  private SoundHelper soundHelper = createSoundHelper();

  private MessageBus createMessageBus() {
    MessageBus result = new MessageBus();
    result.addHandler(new ConsoleMessageHandler());
    return result;
  }

  private SoundHelper createSoundHelper() {
    return new SoundHelper(messageBus);
  }

  /**
   * Starts the software.
   * 
   * @param debugMode
   *          if set to true, debug messages are printed to stdout.
   */
  public void startApp(boolean debugMode) {

    configureDebugMode(debugMode);

    setLoggingProperties();
    setLocale();

    MainControl control = createMainControl();
    Ui userInterface = createUserInterface(control);

    control.tidyOldTemporaryFiles();
    control.loadAudioFiles();

    userInterface.launch();
  }

  private void configureDebugMode(boolean debugMode) {
    if (debugMode) {
      messageBus.addHandler(new DebugMessageHandler());
      addDebugMessage("Debug mode");
      addDebugMessage(bundle.getString("imc.name") + ", V " + bundle.getString("imc.version"));
    }
  }

  private void setLoggingProperties() {
    try {
      /* We do this to prevent JAudioTagger from logging. */
      LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("org.jaudiotagger.level = OFF".getBytes("UTF-8")));
    }
    catch (Exception e) {
      addDebugMessage("Set logging properties: " + e.getMessage());
    }
  }

  private void setLocale() {
    Locale defaultLocale = Locale.getDefault();

    if (!isKnownLanguage(defaultLocale)) {
      defaultLocale = Locale.ENGLISH;
    }

    Locale.setDefault(defaultLocale);
    addDebugMessage("Selected locale: " + Locale.getDefault());
  }

  private boolean isKnownLanguage(Locale locale) {
    List<String> knownLanguages = Lists.newArrayList();
    knownLanguages.add(Locale.ENGLISH.getLanguage());
    knownLanguages.add(Locale.GERMAN.getLanguage());
    return knownLanguages.contains(locale.getLanguage());
  }

  private MainControl createMainControl() {
    return new MainControl(createCompilationGenerator(), createAudioFileFactory(), createMusicPlayer(), messageBus);
  }

  private CompilationGenerator createCompilationGenerator() {
    return new CompilationGenerator(new Compilation(soundHelper, messageBus), createOutputGenerator(), messageBus);
  }

  private AudioFileFactory createAudioFileFactory() {
    return new AudioFileFactory(soundHelper, getAudioFileDecoders(), getBpmReaderWriters(), createBpmCalculator(), messageBus);
  }

  private Collection<AudioFileDecoder> getAudioFileDecoders() {
    AudioStandardizer audioStandardizer = new SoundHelper(messageBus);
    List<AudioFileDecoder> decoders = newArrayList();

    decoders.add(new AacAudioFileDecoder(audioStandardizer));
    decoders.add(new WaveAudioFileDecoder(audioStandardizer));
    decoders.add(new FlacAudioFileDecoder(audioStandardizer));
    decoders.add(new Mp3AudioFileDecoder(audioStandardizer));
    decoders.add(new OggAudioFileDecoder(audioStandardizer));

    return decoders;
  }

  private Collection<BPMReaderWriter> getBpmReaderWriters() {
    List<BPMReaderWriter> bpmReaderWriters = newArrayList();

    bpmReaderWriters.add(new FlacBPMReaderWriter());
    bpmReaderWriters.add(new Mp3BPMReaderWriter());
    bpmReaderWriters.add(new OggBPMReaderWriter());

    return bpmReaderWriters;
  }

  private BPMCalculator createBpmCalculator() {
    return new BeatRoot();
  }

  private OutputGenerator createOutputGenerator() {
    return new OutputGenerator(soundHelper, getAudioFileEncoders(), messageBus);
  }

  private List<AudioFileEncoder> getAudioFileEncoders() {
    ByteArrayConverter byteArrayConverter = new SoundHelper(messageBus);
    List<AudioFileEncoder> encoders = newArrayList();

    encoders.add(new Mp3AudioFileEncoder(byteArrayConverter));
    encoders.add(new WaveAudioFileEncoder());

    return encoders;
  }

  private ExtractMusicPlayer createMusicPlayer() {
    return new ExtractMusicPlayer(messageBus);
  }

  private Ui createUserInterface(MainControl control) {
    Ui userInterface = new SwingUserInterface(control, control, control, control, createUpdateAvailabilityChecker(control), messageBus, messageBus);
    control.setUi(userInterface);
    return userInterface;
  }

  private UpdateChecker createUpdateAvailabilityChecker(ApplicationData applicationData) {
    return new UpdateChecker(applicationData, messageBus);
  }

  private void addDebugMessage(String message) {
    messageBus.send(new DebugMessage(this, message));
  }
}
