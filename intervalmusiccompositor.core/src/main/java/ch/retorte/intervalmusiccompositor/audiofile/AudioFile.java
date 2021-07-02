package ch.retorte.intervalmusiccompositor.audiofile;

import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.audiofile.AudioFileStatus;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.util.ChangeListener;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMCalculator;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;
import org.tritonus.sampled.file.WaveAudioFileReader;
import org.tritonus.sampled.file.WaveAudioFileWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static ch.retorte.intervalmusiccompositor.model.audiofile.AudioFileStatus.*;

/**
 * Holds the audio resource of the software with all its relevant properties as for example volume, or beats per minute.
 */
public class AudioFile extends File implements IAudioFile {

    private final UUID uuid = UUID.randomUUID();

    private final MessageFormatBundle bundle = new CoreBundleProvider().getBundle();

    private final Long startCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.start"));
    private final Long endCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.end"));

    private static final long serialVersionUID = 6154514892883792308L;

    private Long duration = 0L;

    private Float volume;
    private int bpm = -1;
    private final String displayName;
    private String errorMessage;
    private File cache = null;
    private AudioFileStatus status = AudioFileStatus.EMPTY;

    // Indicates if the currently available bpm value is considered reliable
    // This is the case if:
    // a) it was read from the files meta data, or
    // b) it was entered manually
    private Boolean isBpmReliable = false;

    // Indicates if the currently available bpm value is equivalent to the one
    // stored in the meta data of the file
    private Boolean isBpmStored = false;

    private final SoundHelper soundHelper;
    private final List<AudioFileDecoder> audioFileDecoders;
    private final BPMReaderWriter bpmReaderWriter;
    private final BPMCalculator bpmCalculator;
    private final AudioStandardizer audioStandardizer;
    private final MessageProducer messageProducer;

    private final Collection<ChangeListener<IAudioFile>> changeListeners = new LinkedList<>();

    public AudioFile(String pathname, SoundHelper soundHelper, List<AudioFileDecoder> audioFileDecoders, BPMReaderWriter bpmReaderWriter, BPMCalculator bpmCalculator, AudioStandardizer audioStandardizer, MessageProducer messageProducer) {
        super(pathname);
        this.soundHelper = soundHelper;
        this.audioFileDecoders = audioFileDecoders;
        this.bpmReaderWriter = bpmReaderWriter;
        this.bpmCalculator = bpmCalculator;
        this.audioStandardizer = audioStandardizer;
        this.messageProducer = messageProducer;

        displayName = super.getName();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public Long getDuration() {
        return duration;
    }

    /**
     * Starts the process of calculating the duration of the audio file.
     */
    private void calculateDuration() {
        try {
            // Obtain result in seconds and make milliseconds out of it
            duration = (long) (soundHelper.getStreamLengthInSeconds(getAudioInputStream()) * 1000);
            notifyChangeListeners();
            addDebugMessage(getDisplayName() + " duration: " + duration + " ms");
        } catch (IOException e) {
            addDebugMessage("Problems on calculating volume: " + e.getMessage());
        }
    }

    @Override
    public Float getVolumeRatio() {
        return volume;
    }

    /**
     * Calculates the 'volume' ratio of the audio track, that is, the proportion between the tracks average amplitude and a certain preset maximum value. If this
     * track is equally 'loud' as the preset, the value returned is 1. If it is, say, two times as loud as the maximum value, then 0.5 is returned. This volume
     * value may be directly used as a setting for the output volume, achieving that all tracks appear to be equally loud in the output.
     */
    private void calculateVolumeRatio() {
        int averageAmplitude = 0;
        int maximalAverageAmplitude = Integer.parseInt(bundle.getString("imc.audio.volume.max_average_amplitude"));
        int averageAmplitudeWindowSize = Integer.parseInt(bundle.getString("imc.audio.volume.window_size"));

        try {
            averageAmplitude = soundHelper.getAvgAmplitude(getAudioInputStream(), averageAmplitudeWindowSize);
        } catch (IOException e) {
            addDebugMessage("Problems when calculating amplitude: " + e.getMessage());
        }

        volume = ((float) maximalAverageAmplitude / (float) averageAmplitude);
        notifyChangeListeners();
        addDebugMessage(getDisplayName() + " volume ratio: " + volume);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void createCache() throws UnsupportedAudioFileException, IOException {
        setStatus(IN_PROGRESS);

        File temporaryFile = null;

        try {
            temporaryFile = File.createTempFile(getName(), bundle.getString("imc.temporaryFile.suffix"));
        } catch (IOException e) {
            addDebugMessage(e.getMessage());
        }

        if (temporaryFile == null) {
            setStatus(AudioFileStatus.ERROR);
            throw new IOException("Was not able to create temporary cache file.");
        }

        cache = temporaryFile;

        try (AudioInputStream ais = decodeSourceFile()) {
            new WaveAudioFileWriter().write(ais, AudioFileFormat.Type.WAVE, cache);

        } catch (UnsupportedAudioFileException e) {
            setStatus(AudioFileStatus.ERROR);
            errorMessage = "Audio format not supported!";
            removeCache();
            throw e;

        } catch (IOException e) {
            setStatus(AudioFileStatus.ERROR);
            errorMessage = "Read / write error!";
            removeCache();
            throw e;
        }

        calculateVolumeRatio();
        calculateDuration();
        readBpm();

        Long startCutOff = Long.parseLong(bundle.getString("imc.audio.cutoff.start"));
        Long endCutOff = Long.parseLong(bundle.getString("imc.audio.cutoff.end"));

        // Now check if the track is long enough
        if (duration < startCutOff + endCutOff) {
            setStatus(AudioFileStatus.ERROR);
            errorMessage = "Track too short! (Duration: " + getFormattedTime(duration) + " s)";
        } else {
            setStatus(AudioFileStatus.OK);
        }
    }

    private AudioInputStream decodeSourceFile() throws UnsupportedAudioFileException {
        for (AudioFileDecoder decoder : audioFileDecoders) {
            try {
                AudioInputStream sourceFileStream = decoder.decode(this);
                return audioStandardizer.standardize(sourceFileStream);
            } catch (Exception e) {
                addDebugMessage("Audio decoder complained for file '" + getDisplayName() + "': " + e.getMessage());
                // If there is trouble, we just try the next decoder.
            }
        }

        throw new UnsupportedAudioFileException("Audio file not recognized by any decoder: " + getDisplayName());
    }

    private String getFormattedTime(long milliseconds) {
        return new FormatTime().getStrictFormattedTime(milliseconds / 1000.0);
    }

    public void removeCache() {
        if (hasCache()) {
            boolean deleted = cache.delete();
            addDebugMessage("Deleting cache file: " + cache + ". Success: " + deleted);
            cache = null;
        }
        setStatus(EMPTY);
    }

    public void setQueuedStatus() {
        setStatus(QUEUED);
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        addDebugMessage("Set BPM to " + bpm + " of " + getDisplayName());

        this.bpm = bpm;

        isBpmReliable = true;
        isBpmStored = false;

        notifyChangeListeners();
    }

    /**
     * Starts the process of reading meta information (e.g. ID3v2 tags in MP3) in the source file. Upon success, the value should be stored somehow and be
     * retrieved by the {@link IAudioFile#getBpm()} method. Furthermore, the {@link IAudioFile#hasBpm()} should return true in that case.
     */
    private void readBpm() {

        // Read bpm info
        int loadedBpm;
        int calculatedBpm = -1;

        // Get threshold data from property file
        int bpmBottomThreshold = Integer.parseInt(bundle.getString("imc.audio.bpm.bottom_threshold"));
        int bpmTopThreshold = Integer.parseInt(bundle.getString("imc.audio.bpm.top_threshold"));

        // If the reading of bpm is supported, retrieve value from meta data
        if (isBpmSupported()) {
            loadedBpm = readBpmInternal();

            // Only set bpm value if it is in valid range
            if (bpmBottomThreshold <= loadedBpm && loadedBpm <= bpmTopThreshold) {
                bpm = loadedBpm;

                isBpmReliable = true;
                isBpmStored = true;
            }
        }

        // If there was none, calculate it
        if (bpm == -1) {
            int bpmExtractLength = Integer.parseInt(bundle.getString("imc.audio.bpm.trackLength"));
            int bpmExtractStart;

            if (getDuration() / 1000 < bpmExtractLength) {
                bpmExtractLength = (int) (getDuration() / 1000);
            }

            bpmExtractStart = (int) (((getDuration() / 1000) - bpmExtractLength) / 2);

            try {
                calculatedBpm = calculateBpm(bpmExtractStart, bpmExtractLength);
                addDebugMessage(getDisplayName() + " BPM: " + calculatedBpm);
            } catch (OutOfMemoryError e) {
                addDebugMessage("Problems when calculating BPM information: " + e.getMessage());
            }

            // Only set bpm value if it is in valid range.
            if (bpmBottomThreshold <= calculatedBpm && calculatedBpm <= bpmTopThreshold) {
                bpm = calculatedBpm;
                notifyChangeListeners();
            }
        }
    }

    private int readBpmInternal() {
        Integer readBPMValue = bpmReaderWriter.readBPMFrom(this);
        if (readBPMValue == null) {
            return -1;
        }
        addDebugMessage("Reading BPM from file: " + readBPMValue);
        return readBPMValue;
    }

    @Override
    public Boolean hasBpm() {
        return 0 <= bpm;
    }

    /**
     * Calculates the bpm value of the file on an extract starting at extractStart and having a length of extractLength.
     *
     * @param extractLength Length of the inspected track extract in seconds
     * @param extractStart  Starting point of the inspected track extract in seconds
     * @return The calculated bpm value
     */
    private int calculateBpm(int extractStart, int extractLength) {

        int result = -1;

        try {
            AudioInputStream shortInputStream = soundHelper.getStreamExtract(getAudioInputStream(), extractStart, extractLength);
            result = bpmCalculator.calculateBPM(shortInputStream);
        } catch (Exception e) {
            addDebugMessage("Problems with calculating BPM: " + e.getMessage());
        }

        return result;
    }

    @Override
    public AudioInputStream getAudioInputStream() throws IOException {
        try {
            if (!hasCache()) {
                createCache();
            }

            /* We need to select the WaveFileReader explicitly here. If we let the AudioSystem choose, the JAAD variant is chosen which does not work. */
            return new WaveAudioFileReader().getAudioInputStream(cache);
        } catch (UnsupportedAudioFileException e) {
            addDebugMessage("Problems with reading cache audio file: " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    private boolean hasCache() {
        return cache != null && cache.exists();
    }

    @Override
    public Boolean isBpmReliable() {
        return isBpmReliable;
    }

    @Override
    public Boolean isBpmStored() {
        return isBpmStored;
    }

    @Override
    public Boolean isBpmSupported() {
        return bpmReaderWriter != null;
    }

    @Override
    public void writeBpm(int bpm) {
        bpmReaderWriter.writeBPMTo(bpm, this);
        isBpmReliable = true;
        isBpmStored = true;
        notifyChangeListeners();
    }

    @Override
    public boolean isOK() {
        return status.equals(OK);
    }

    @Override
    public boolean isLoading() {
        return status.equals(IN_PROGRESS);
    }

    @Override
    public AudioFileStatus getStatus() {
        return status;
    }

    private void setStatus(AudioFileStatus status) {
        addDebugMessage("Setting status to " + status + " for " + getDisplayName());
        this.status = status;
        notifyChangeListeners();
    }

    @Override
    public File getSource() {
        return this;
    }

    @Override
    public void addChangeListener(ChangeListener<IAudioFile> changeListener) {
        synchronized (changeListeners) {
            changeListeners.add(changeListener);
        }
    }

    private void notifyChangeListeners() {
        synchronized (changeListeners) {
            changeListeners.forEach(changeListener -> changeListener.changed(this));
        }
    }

    private void addDebugMessage(String message) {
        messageProducer.send(new DebugMessage(this, message));
    }

    public boolean isLongEnoughFor(int extractInSeconds) {
        return extractInSeconds <= ((getDuration() - startCutOffInMilliseconds - endCutOffInMilliseconds) / 1000);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AudioFile) {
            if (hasCache()) {
                return cache.equals(((AudioFile) obj).cache);
            } else {
                return uuid.equals(((AudioFile) obj).uuid);
            }

        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return cache.hashCode();
    }
}
