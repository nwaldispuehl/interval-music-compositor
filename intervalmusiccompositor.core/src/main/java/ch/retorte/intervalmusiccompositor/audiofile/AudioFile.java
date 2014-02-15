package ch.retorte.intervalmusiccompositor.audiofile;

import static ch.retorte.intervalmusiccompositor.audiofile.AudioFileStatus.EMPTY;
import static ch.retorte.intervalmusiccompositor.audiofile.AudioFileStatus.ERROR;
import static ch.retorte.intervalmusiccompositor.audiofile.AudioFileStatus.IN_PROGRESS;
import static ch.retorte.intervalmusiccompositor.audiofile.AudioFileStatus.OK;
import static ch.retorte.intervalmusiccompositor.audiofile.AudioFileStatus.QUEUED;
import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.sampled.file.WaveAudioFileReader;
import org.tritonus.sampled.file.WaveAudioFileWriter;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMCalculator;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * @author nw
 */
public class AudioFile extends File implements IAudioFile {

  private MessageFormatBundle bundle = getBundle("core_imc");

  private Long startCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.start"));
  private Long endCutOffInMilliseconds = Long.parseLong(bundle.getString("imc.audio.cutoff.end"));

  private static final long serialVersionUID = 6154514892883792308L;

  private Long duration = 0L;

  private Float volume;
  private int bpm = -1;
  private String displayName;
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

  private SoundHelper soundHelper;
  private List<AudioFileDecoder> audioFileDecoders;
  private BPMReaderWriter bpmReaderWriter;
  private BPMCalculator bpmCalculator;
  private MessageProducer messageProducer;

  public AudioFile(String pathname, SoundHelper soundHelper, List<AudioFileDecoder> audioFileDecoders, BPMReaderWriter bpmReaderWriter,
      BPMCalculator bpmCalculator, MessageProducer messageProducer) {
    super(pathname);
    this.soundHelper = soundHelper;
    this.audioFileDecoders = audioFileDecoders;
    this.bpmReaderWriter = bpmReaderWriter;
    this.bpmCalculator = bpmCalculator;
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
    }
    catch (IOException e) {
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
    }
    catch (IOException e) {
      addDebugMessage("Problems when calculating amplitude: " + e.getMessage());
    }

    volume = ((float) maximalAverageAmplitude / (float) averageAmplitude);
  }

  public String getDisplayName() {
    return displayName;
  }

  public void createCache() throws UnsupportedAudioFileException, IOException {
    status = AudioFileStatus.IN_PROGRESS;

    File temporaryFile = null;

    try {
      temporaryFile = File.createTempFile(getName(), bundle.getString("imc.temporaryFile.suffix"));
    }
    catch (IOException e) {
      addDebugMessage(e.getMessage());
    }

    if (temporaryFile != null) {

      cache = temporaryFile;
      AudioInputStream ais = null;

      try {
        ais = decodeSourceFile();
        new WaveAudioFileWriter().write(ais, AudioFileFormat.Type.WAVE, cache);
      }
      catch (UnsupportedAudioFileException e) {
        status = AudioFileStatus.ERROR;
        errorMessage = "Audio format not supported!";
        cache.delete();
        cache = null;
        throw e;
      }
      catch (IOException e) {
        status = AudioFileStatus.ERROR;
        errorMessage = "Read / write error!";
        cache.delete();
        cache = null;
        throw e;
      }
      finally {
        if (ais != null) {
          ais.close();
        }
      }

    }
    else {
      status = AudioFileStatus.ERROR;
      throw new IOException("Was not able to create temporary cache file.");
    }

    calculateVolumeRatio();
    calculateDuration();
    readBpm();

    Long startCutOff = Long.parseLong(bundle.getString("imc.audio.cutoff.start"));
    Long endCutOff = Long.parseLong(bundle.getString("imc.audio.cutoff.end"));

    // Now check if the track is long enough
    if (duration < startCutOff + endCutOff) {
      status = AudioFileStatus.ERROR;
      errorMessage = "Track too short! (Duration: " + getFormattedTime(duration) + " s)";
    }
    else {
      status = AudioFileStatus.OK;
    }
  }

  private AudioInputStream decodeSourceFile() throws UnsupportedAudioFileException, IOException {

    for (AudioFileDecoder decoder : audioFileDecoders) {
      try {
        return decoder.decode(this);
      }
      catch (Exception e) {
        // If there is trouble, we just try the next decoder.
      }
    }

    throw new UnsupportedAudioFileException("Audio file not recognized by any decoder.");
  }

  private String getFormattedTime(long milliseconds) {
    return new SimpleDateFormat("HH:mm:ss").format(new Date(milliseconds));
  }

  public void removeCache() {
    if (cache != null && cache.exists()) {
      addDebugMessage("Deleting cache file: " + cache);
      cache.delete();
    }
    status = EMPTY;
  }

  public void setQueuedStatus() {
    status = QUEUED;
  }

  public void setInProgressStatus() {
    status = IN_PROGRESS;
  }

  public int getBpm() {
    return bpm;
  }

  public void setBpm(int bpm) {
    addDebugMessage("Set BPM to " + bpm + " of " + getDisplayName());

    this.bpm = bpm;

    isBpmReliable = true;
    isBpmStored = false;
  }

  /**
   * Starts the process of reading meta information (e.g. ID3v2 tags in MP3) in the source file. Upon success, the value should be stored somehow and be
   * retrieved by the {@link IAudioFile#getBpm()} method. Furthermore, the {@link IAudioFile#hasBpm()} should return true in that case.
   */
  private void readBpm() {

    // Read bpm info
    int loadedBpm = -1;
    int calculatedBpm = -1;

    // Get threshold data from property file
    int bpmBottomThreshold = Integer.valueOf(bundle.getString("imc.audio.bpm.bottom_threshold"));
    int bpmTopThreshold = Integer.valueOf(bundle.getString("imc.audio.bpm.top_threshold"));

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
      int bpmExtractLength = Integer.valueOf(bundle.getString("imc.audio.bpm.trackLength"));
      int bpmExtractStart;

      if (getDuration() / 1000 < bpmExtractLength) {
        bpmExtractLength = (int) (getDuration() / 1000);
      }

      bpmExtractStart = (int) (((getDuration() / 1000) - bpmExtractLength) / 2);

      try {
        calculatedBpm = calculateBpm(bpmExtractLength, bpmExtractStart);
        addDebugMessage("Calculated BPM: " + calculatedBpm + " of " + getDisplayName());
      }
      catch (OutOfMemoryError e) {
        addDebugMessage("Problems when calculating BPM information: " + e.getMessage());
      }

      // Only set bpm value if it is in valid range.
      if (bpmBottomThreshold <= calculatedBpm && calculatedBpm <= bpmTopThreshold) {
        bpm = calculatedBpm;
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
   * @return The calculated bpm value
   * @param extractLength
   *          Length of the inspected track extract in seconds
   * @param extractStart
   *          Starting point of the inspected track extract in seconds
   */
  private int calculateBpm(int extractLength, int extractStart) {

    int result = -1;

    try {
      AudioInputStream shortInputStream = soundHelper.getStreamExtract(getAudioInputStream(), extractLength, extractStart);
      result = bpmCalculator.calculateBPM(shortInputStream);
    }
    catch (Exception e) {
      addDebugMessage("Problems with calculating BPM: " + e.getMessage());
    }

    return result;
  }

  @Override
  public AudioInputStream getAudioInputStream() throws IOException {
    try {
      if (cache == null || !cache.exists()) {
        createCache();
      }

      /* We need to select the WaveFileReader explicitly here. If we let the AudioSystem choose, the JAAD variant is chosen which does not work. */
      return new WaveAudioFileReader().getAudioInputStream(cache);
    }
    catch (UnsupportedAudioFileException e) {
      addDebugMessage("Problems with reading cache audio file: " + e.getMessage());
      throw new IOException(e.getMessage());
    }
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
  public void writeBpm(int bpm) throws IOException {
    bpmReaderWriter.writeBPMTo(bpm, this);
    isBpmReliable = true;
    isBpmStored = true;
  }

  @Override
  public boolean isOK() {
    return status.equals(OK);
  }

  @Override
  public boolean isQueued() {
    return status.equals(QUEUED);
  }

  @Override
  public boolean isLoading() {
    return status.equals(IN_PROGRESS);
  }

  @Override
  public boolean hasError() {
    return status.equals(ERROR);
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  public boolean isLongEnoughFor(int extractInSeconds) {
    return extractInSeconds <= ((getDuration() - startCutOffInMilliseconds - endCutOffInMilliseconds) / 1000);
  }

}
