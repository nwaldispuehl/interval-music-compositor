package ch.retorte.intervalmusiccompositor;

import at.ofai.music.beatroot.BeatRoot;
import ch.retorte.intervalmusiccompositor.audiofile.AudioFileFactory;
import ch.retorte.intervalmusiccompositor.bundle.AppBundleProvider;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.compilation.Compilation;
import ch.retorte.intervalmusiccompositor.compilation.CompilationGenerator;
import ch.retorte.intervalmusiccompositor.decoder.aac.AacAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.flac.FlacAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.flac.FlacBPMReaderWriter;
import ch.retorte.intervalmusiccompositor.decoder.mp3.Mp3AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.mp3.Mp3BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.decoder.ogg.OggAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.decoder.ogg.OggBPMReaderWriter;
import ch.retorte.intervalmusiccompositor.decoder.wave.WaveAudioFileDecoder;
import ch.retorte.intervalmusiccompositor.encoder.mp3.Mp3AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.encoder.ogg.OggAudioFileEncoder;
import ch.retorte.intervalmusiccompositor.encoder.wave.WaveAudioFileEncoder;
import ch.retorte.intervalmusiccompositor.messagebus.ConsoleMessageHandler;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessagePrinter;
import ch.retorte.intervalmusiccompositor.messagebus.MessageBus;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.output.OutputGenerator;
import ch.retorte.intervalmusiccompositor.player.ExtractMusicPlayer;
import ch.retorte.intervalmusiccompositor.soundeffects.BuiltInSoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMCalculator;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.spi.update.VersionUpgrader;
import ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI;
import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
import ch.retorte.intervalmusiccompositor.util.ModuleReplacingVersionUpgrader;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;
import ch.retorte.intervalmusiccompositor.util.UpdateChecker;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.LogManager;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The {@link IntervalMusicCompositor} is the main program file of the software.
 */
class IntervalMusicCompositor {

    //---- Static

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;


    //---- Fields

    private final List<Locale> knownLocales = createKnownLocales();

    private final Platform platform = new PlatformFactory().getPlatform();
    private final MessageFormatBundle bundle = new AppBundleProvider().getBundle();

    private final MessageBus messageBus = createMessageBus();
    private final SoundHelper soundHelper = createSoundHelper();

    private final UiUserPreferences userPreferences = createUserPreferences();


    //---- Methods

    private List<Locale> createKnownLocales() {
        return newArrayList(DEFAULT_LOCALE, Locale.GERMAN);
    }

    private MessageBus createMessageBus() {
        MessageBus result = new MessageBus(true);
        result.addHandler(new ConsoleMessageHandler());
        return result;
    }

    private SoundHelper createSoundHelper() {
        return new SoundHelper(messageBus);
    }

    private UiUserPreferences createUserPreferences() {
        return new UiUserPreferences(messageBus);
    }


    /**
     * Starts the software.
     *
     * @param debugMode        if set to true, debug messages are printed to stdout.
     * @param clearPreferences if set to true, clears all user preferences.
     */
    void startApp(boolean debugMode, boolean clearPreferences) {
        configureDebugMode(debugMode);
        conditionallyClearPreferences(clearPreferences);

        setLoggingProperties();
        setLocale();

        MainControl control = createMainControl();
        Ui userInterface = createUserInterface(control);

        control.tidyOldTemporaryFiles();

        userInterface.launch();
    }

    private void conditionallyClearPreferences(boolean clearPreferences) {
        if (clearPreferences) {
            userPreferences.destroyAllPreferences();
        }
    }

    private void configureDebugMode(boolean debugMode) {
        if (debugMode) {
            messageBus.addHandler(new DebugMessagePrinter());
            addDebugMessage("Debug mode");
        }
        addDebugMessage(bundle.getString("imc.name") + ", V " + bundle.getString("imc.version"));
        addDebugMessage("System properties: " + platform.getSystemDiagnosisString());
    }

    private void setLoggingProperties() {
        try {
            /* We do this to prevent JAudioTagger from logging. */
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("org.jaudiotagger.level = OFF".getBytes(UTF_8)));
        } catch (Exception e) {
            addDebugMessage("Set logging properties: " + e.getMessage());
        }
    }

    private void setLocale() {
        Locale currentLocale = Locale.getDefault();

        if (userPreferences.hasLocale()) {
            currentLocale = userPreferences.loadLocale();
        }

        Optional<Locale> knownLocale = getKnownLocaleFor(currentLocale);

        if (knownLocale.isPresent()) {
            Locale.setDefault(knownLocale.get());
        } else {
            Locale.setDefault(DEFAULT_LOCALE);
        }

        addDebugMessage("Selected locale: " + Locale.getDefault());
    }

    private Optional<Locale> getKnownLocaleFor(Locale currentLocale) {
        if (currentLocale == null) {
            return Optional.empty();
        }

        return knownLocales.stream().filter(l -> l.getLanguage().equals(currentLocale.getLanguage())).findFirst();
    }

    private MainControl createMainControl() {
        return new MainControl(createCompilationGenerator(), createAudioFileFactory(), createMusicPlayer(), createSoundEffectProvider(), messageBus, knownLocales);
    }

    private CompilationGenerator createCompilationGenerator() {
        return new CompilationGenerator(new Compilation(soundHelper, messageBus), createOutputGenerator(), messageBus);
    }

    private AudioFileFactory createAudioFileFactory() {
        AudioStandardizer audioStandardizer = new SoundHelper(messageBus);
        return new AudioFileFactory(soundHelper, getAudioFileDecoders(), getBpmReaderWriters(), createBpmCalculator(), audioStandardizer, messageBus);
    }

    private Collection<AudioFileDecoder> getAudioFileDecoders() {
        List<AudioFileDecoder> decoders = newArrayList();

        decoders.add(new AacAudioFileDecoder());
        decoders.add(new WaveAudioFileDecoder());
        decoders.add(new FlacAudioFileDecoder());
        decoders.add(new Mp3AudioFileDecoder());
        decoders.add(new OggAudioFileDecoder());

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
        List<AudioFileEncoder> encoders = newArrayList();

        encoders.add(new Mp3AudioFileEncoder());
        encoders.add(new OggAudioFileEncoder());
        encoders.add(new WaveAudioFileEncoder());

        return encoders;
    }

    private ExtractMusicPlayer createMusicPlayer() {
        return new ExtractMusicPlayer(messageBus);
    }

    private SoundEffectsProvider createSoundEffectProvider() {
        return new BuiltInSoundEffectsProvider();
    }

    private Ui createUserInterface(MainControl control) {
        Ui userInterface = new IntervalMusicCompositorUI(control, control, control, control, createVersionUpgrader(), createUpdateAvailabilityChecker(control), control, userPreferences, messageBus, messageBus);
        control.setUi(userInterface);
        return userInterface;
    }

    private VersionUpgrader createVersionUpgrader() {
        return new ModuleReplacingVersionUpgrader(platform, messageBus);
    }

    private UpdateChecker createUpdateAvailabilityChecker(ApplicationData applicationData) {
        return new UpdateChecker(applicationData, messageBus);
    }

    private void addDebugMessage(String message) {
        messageBus.send(new DebugMessage(this, message));
    }
}
