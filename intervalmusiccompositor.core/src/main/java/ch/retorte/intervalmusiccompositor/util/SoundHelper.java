package ch.retorte.intervalmusiccompositor.util;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import ch.retorte.intervalmusiccompositor.spi.audio.ByteArrayConverter;
import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import org.tritonus.sampled.convert.SampleRateConversionProvider;

import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Some static methods for easing the handling of sound data.
 * 
 * @author nw
 */
public class SoundHelper implements AudioStandardizer, ByteArrayConverter {

  private MessageProducer messageProducer;

  public SoundHelper(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  @Override
  public byte[] convert(AudioInputStream audioInputStream) throws IOException {
    return getByteArray(audioInputStream);
  }

  /**
   * Determines the largest average frame value of a audio stream. Uses a 16
   * bytes window as smallest unit. May be expanded through the windows
   * argument.
   * 
   * @param inputBuffer
   *          The to be inspected audio {@link AudioInputStream} buffer
   * @param sampleWindow
   *          How many single samples are to be averaged
   * @return The largest average of the stream
   * @throws IOException
   */
  public int getAvgAmplitude(AudioInputStream inputBuffer, int sampleWindow) throws IOException {

    int sample = 0;

    int sampleWindowCounter = 0;
    long sampleWindowSum = 0;

    int maxSample = 0;
    int bytesPerFrame = inputBuffer.getFormat().getFrameSize();

    // Add factor 4 to speed up
    int numberOfBytes = 2 * bytesPerFrame * 8;
    byte[] audioBytes = new byte[numberOfBytes];

    int numberOfBytesRead = 0;

    while ((numberOfBytesRead = inputBuffer.read(audioBytes)) != -1) {

      if (numberOfBytesRead == numberOfBytes) {

        // Here we do a rollout to speed up the whole thing
        sample = Math.abs((audioBytes[0] & 0xFF) | (audioBytes[1] << 8));
        sample += Math.abs((audioBytes[2] & 0xFF) | (audioBytes[3] << 8));
        sample += Math.abs((audioBytes[4] & 0xFF) | (audioBytes[5] << 8));
        sample += Math.abs((audioBytes[6] & 0xFF) | (audioBytes[7] << 8));
        sample += Math.abs((audioBytes[8] & 0xFF) | (audioBytes[9] << 8));
        sample += Math.abs((audioBytes[10] & 0xFF) | (audioBytes[11] << 8));
        sample += Math.abs((audioBytes[12] & 0xFF) | (audioBytes[13] << 8));
        sample += Math.abs((audioBytes[14] & 0xFF) | (audioBytes[15] << 8));
        sample = (sample / 8);
      }
      else {
        // If there is not enough data anymore we don't bother
        // and discard it
        sample = 0;
      }

      if (sampleWindowCounter < sampleWindow) {
        sampleWindowSum += sample;
        sampleWindowCounter++;
      }
      else {

        if (maxSample <= (sampleWindowSum / sampleWindow)) {
          maxSample = (int) (sampleWindowSum / sampleWindow);
        }

        sampleWindowCounter = 0;
        sampleWindowSum = 0;
      }
    }

    return maxSample;
  }

  /**
   * Returns the largest amplitude value of the provided
   * {@link AudioInputStream}
   * 
   * @param inputBuffer
   *          The to be inspected stream
   * @return The maximum amplitude value of the stream
   * @throws IOException
   */
  public int getMaxAmplitude(AudioInputStream inputBuffer) throws IOException {

    int maxSample = 0;
    int bytesPerFrame = inputBuffer.getFormat().getFrameSize();

    // Add factor 4 to speed up
    int numberOfBytes = 2 * bytesPerFrame * 8;
    byte[] audioBytes = new byte[numberOfBytes];

    int numberOfBytesRead = 0;

    while ((numberOfBytesRead = inputBuffer.read(audioBytes)) != -1) {

      if (numberOfBytesRead == numberOfBytes) {

        // Here we do a roll out to speed up the whole thing
        maxSample = Math.max(maxSample, Math.abs((audioBytes[0] & 0xFF) | (audioBytes[1] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[2] & 0xFF) | (audioBytes[3] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[4] & 0xFF) | (audioBytes[5] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[6] & 0xFF) | (audioBytes[7] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[8] & 0xFF) | (audioBytes[9] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[10] & 0xFF) | (audioBytes[11] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[12] & 0xFF) | (audioBytes[13] << 8)));
        maxSample = Math.max(maxSample, Math.abs((audioBytes[14] & 0xFF) | (audioBytes[15] << 8)));
      }
    }

    return maxSample;
  }

  public AudioInputStream getLeveledStream(AudioInputStream audioInputStream, float desiredRelativeAmplitude) {
    AmplitudeAudioInputStream aais = new AmplitudeAudioInputStream(audioInputStream);
    aais.setAmplitudeLinear(desiredRelativeAmplitude);
    return aais;
  }

  public AudioInputStream getStreamFromByteArray(byte[] byteArray) {
    return new AudioInputStream(new ByteArrayInputStream(byteArray), TARGET_AUDIO_FORMAT, (byteArray.length / TARGET_AUDIO_FORMAT.getFrameSize()));
  }

  public byte[] getStreamPart(AudioInputStream inputStream, Long startTimeMS, Long durationMS) throws IOException {

    // First skip to the desired position
    inputStream.skip(Math.round((startTimeMS / 1000) * inputStream.getFormat().getSampleRate() * inputStream.getFormat().getFrameSize()));

    byte[] result = new byte[Math.round((durationMS / 1000) * inputStream.getFormat().getSampleRate() * inputStream.getFormat().getFrameSize())];

    // Choose a buffer of 100 KB
    byte[] audioBytes = new byte[102400];

    int numberOfBytesRead = 0;
    int totalNumberOfBytesRead = 0;

    while ((numberOfBytesRead = inputStream.read(audioBytes)) != -1) {

      if (result.length <= totalNumberOfBytesRead + numberOfBytesRead) {

        // Write down the last bytes
        System.arraycopy(audioBytes, 0, result, totalNumberOfBytesRead, result.length - totalNumberOfBytesRead);

        // Interrupt
        break;
      }

      System.arraycopy(audioBytes, 0, result, totalNumberOfBytesRead, numberOfBytesRead);

      totalNumberOfBytesRead += numberOfBytesRead;
    }

    return result;
  }

  public byte[] getByteArray(AudioInputStream inputStream) throws IOException {

    byte[] result = new byte[(int) getStreamSizeInBytes(inputStream)];

    // Choose a buffer of 100 KB
    byte[] buffer = new byte[102400];

    int len;
    int writtenBytes = 0;
    while ((len = inputStream.read(buffer)) > 0) {

      System.arraycopy(buffer, 0, result, writtenBytes, len);

      writtenBytes += len;
    }

    return result;
  }

  public double getStreamLengthInSeconds(AudioInputStream inputStream) {
    return (inputStream.getFrameLength() / inputStream.getFormat().getFrameRate());
  }

  public long getStreamSizeInBytes(AudioInputStream inputStream) {

    // Calculate length of wav input stream
    AudioFormat af = inputStream.getFormat();

    long frameLength = inputStream.getFrameLength();
    long byteLength = 0;

    int frameSize = af.getFrameSize();

    byteLength = frameLength * frameSize;

    return byteLength;
  }

  public byte[] generateSilenceOfLength(double lengthInSeconds) {
    int samples = getSamplesFromSeconds(lengthInSeconds);

    byte[] silenceBuffer = new byte[samples];
    for (int i = 0; i < silenceBuffer.length; i = i + 2) {
      silenceBuffer[i] = (byte) 0x80;
      silenceBuffer[i + 1] = (byte) 0x00;
    }

    return silenceBuffer;
  }

  public int getSamplesFromSeconds(double seconds) {
    return (int) (seconds * SAMPLE_RATE * TARGET_AUDIO_FORMAT.getFrameSize());
  }

  public double getSecondsFromSamples(int samples) {
    return samples / SAMPLE_RATE / TARGET_AUDIO_FORMAT.getFrameSize();
  }

  public AudioInputStream getStreamExtract(AudioInputStream ais, int start, int length) {

    long startMs = start * 1000;
    long durationMs = length * 1000;

    AudioInputStream result = null;

    try {
      long streamLengthMs = (long) (getStreamLengthInSeconds(ais) * 1000);

      if (streamLengthMs < startMs + durationMs) {

        if (streamLengthMs < durationMs) {
          startMs = 0;
          durationMs = (int) streamLengthMs;
        }
        else {
          startMs = (int) ((streamLengthMs - durationMs) / 2);
        }
      }

      byte[] streamExtract = getStreamPart(ais, startMs, durationMs);

      int extractFrameLength = (int) (TARGET_AUDIO_FORMAT.getSampleRate() * TARGET_AUDIO_FORMAT.getFrameSize() * (durationMs / 1000));
      result = new AudioInputStream(new ByteArrayInputStream(streamExtract), TARGET_AUDIO_FORMAT, extractFrameLength);
    }
    catch (IOException e) {
      // nop
    }

    return result;
  }

  public byte[] linearBlend(byte[] sampleByteArray, double blendTime) {

    // Since the fading caused clicking noises in the beginning and the end
    // of the fading process (where the sound intensity exceeded the limit)
    // we enlarge the fade duration by 0.05 seconds.
    int clickPreventDuration = 8820;

    // Determine how many samples there are to alter
    // There are 176400 bytes per second
    int corrLength = sampleByteArray.length - 1;

    int samples = (int) ((TARGET_AUDIO_FORMAT.getSampleRate() * TARGET_AUDIO_FORMAT.getFrameSize() * blendTime) + clickPreventDuration);

    if (((double) sampleByteArray.length / 2) < samples) {

      if (clickPreventDuration <= ((double) sampleByteArray.length / 2)) {
        samples = (int) ((double) sampleByteArray.length / 2) + clickPreventDuration;

      }
      else {
        samples = (int) ((double) sampleByteArray.length / 2);
      }
    }

    int valueFront = 0;
    int valueEnd = 0;

    for (int i = 0; i < samples; i = i + 2) {

      // Determine effective value of each two bytes
      valueFront = (sampleByteArray[i] & 0xFF) | (sampleByteArray[i + 1] << 8);
      valueEnd = (sampleByteArray[corrLength - i - 1] & 0xFF) | (sampleByteArray[corrLength - i] << 8);

      // Adapt value
      valueFront = (int) (valueFront * ((double) i / (double) samples));
      valueEnd = (int) (valueEnd * ((double) i / (double) samples));

      // Write it back into array
      sampleByteArray[i] = (byte) (valueFront & 0xFF);
      sampleByteArray[i + 1] = (byte) (valueFront >> 8);
      sampleByteArray[corrLength - i - 1] = (byte) (valueEnd & 0xFF);
      sampleByteArray[corrLength - i] = (byte) (valueEnd >> 8);
    }

    return sampleByteArray;
  }

  /*
   * The code below is taken from
   * http://www.jsresources.org/examples/AudioConverter.java.html
   */
  @Override
  public AudioInputStream standardize(AudioInputStream stream) {
    addDebugMessage("Original format: " + stream.getFormat());

    if (stream.getFormat().getChannels() != TARGET_AUDIO_FORMAT.getChannels()) {
      stream = convertChannels(TARGET_AUDIO_FORMAT.getChannels(), stream);
    }

    boolean bDoConvertSampleSize = (stream.getFormat().getSampleSizeInBits() != TARGET_AUDIO_FORMAT.getSampleSizeInBits());
    boolean bDoConvertEndianess = (stream.getFormat().isBigEndian() != TARGET_AUDIO_FORMAT.isBigEndian());

    if (bDoConvertSampleSize || bDoConvertEndianess) {
      stream = convertSampleSizeAndEndianess(TARGET_AUDIO_FORMAT.getSampleSizeInBits(), TARGET_AUDIO_FORMAT.isBigEndian(), stream);
    }

    if (!equals(stream.getFormat().getSampleRate(), TARGET_AUDIO_FORMAT.getSampleRate())) {
      stream = convertSampleRate(TARGET_AUDIO_FORMAT.getSampleRate(), stream);
    }

    addDebugMessage("Converted format: " + stream.getFormat());
    return stream;
  }

  private AudioInputStream convertChannels(int nChannels, AudioInputStream sourceStream) {
    AudioFormat sourceFormat = sourceStream.getFormat();

    AudioFormat targetFormat = new AudioFormat(sourceFormat.getEncoding(), sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(), nChannels,
        calculateFrameSize(nChannels, sourceFormat.getSampleSizeInBits()), sourceFormat.getFrameRate(), sourceFormat.isBigEndian());

    return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
  }

  private AudioInputStream convertSampleSizeAndEndianess(int nSampleSizeInBits, boolean bBigEndian, AudioInputStream sourceStream) {
    AudioFormat sourceFormat = sourceStream.getFormat();

    AudioFormat targetFormat = new AudioFormat(sourceFormat.getEncoding(), sourceFormat.getSampleRate(), nSampleSizeInBits, sourceFormat.getChannels(),
        calculateFrameSize(sourceFormat.getChannels(), nSampleSizeInBits), sourceFormat.getFrameRate(), bBigEndian);

    return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
  }

  private AudioInputStream convertSampleRate(float fSampleRate, AudioInputStream sourceStream) {
    AudioFormat sourceFormat = sourceStream.getFormat();

    AudioFormat targetFormat = new AudioFormat(sourceFormat.getEncoding(), fSampleRate, sourceFormat.getSampleSizeInBits(), sourceFormat.getChannels(),
        sourceFormat.getFrameSize(), fSampleRate, sourceFormat.isBigEndian());

    SampleRateConversionProvider sampleRateConversionProvider = new SampleRateConversionProvider();
    if (sampleRateConversionProvider.isConversionSupported(targetFormat, sourceFormat)) {
      return sampleRateConversionProvider.getAudioInputStream(targetFormat, sourceStream);
    }

    return sourceStream;
  }

  private int calculateFrameSize(int nChannels, int nSampleSizeInBits) {
    return ((nSampleSizeInBits + 7) / 8) * nChannels;
  }

  private boolean equals(float f1, float f2) {
    return (Math.abs(f1 - f2) < 1E-9F);
  }

  public int sec(long milliseconds) {
    return (int) (milliseconds / 1000);
  }

}
