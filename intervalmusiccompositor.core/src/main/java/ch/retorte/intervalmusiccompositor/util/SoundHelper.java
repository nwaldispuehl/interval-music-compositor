package ch.retorte.intervalmusiccompositor.util;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import ch.retorte.intervalmusiccompositor.spi.audio.ByteArrayConverter;
import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import org.tritonus.sampled.convert.SampleRateConversionProvider;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Some static methods for easing the handling of sound data.
 */
public class SoundHelper implements AudioStandardizer, ByteArrayConverter {

  private final MessageProducer messageProducer;

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
   * Determines the largest average frame value of an audio stream. Uses a 16
   * bytes window as smallest unit. May be expanded through the sampleWindow
   * argument.
   * 
   * @param inputBuffer
   *          The to be inspected audio {@link AudioInputStream} buffer
   * @param sampleWindow
   *          How many single samples are to be averaged
   * @return The largest average of the stream
   * @throws IOException if there was trouble
   */
  public int getAvgAmplitude(AudioInputStream inputBuffer, int sampleWindow) throws IOException {

    int sample;

    int sampleWindowCounter = 0;
    long sampleWindowSum = 0;

    int maxSample = 0;
    int bytesPerFrame = inputBuffer.getFormat().getFrameSize();

    // Add factor 4 to speed up
    int numberOfBytes = 2 * bytesPerFrame * 8;
    byte[] audioBytes = new byte[numberOfBytes];

    int numberOfBytesRead;

    while ((numberOfBytesRead = inputBuffer.read(audioBytes)) != -1) {

      if (numberOfBytesRead == numberOfBytes) {

        // Here we do a roll-out to speed up the whole thing
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
   * Adjusts the 'volume' (i.e. the amplitude) of the provided audio stream according to the measured volume ratio and an arbitrary volume factor.
   *
   * @param audioInputStream the stream holding the to be processed audio data.
   * @param desiredRelativeAmplitude the relative amplitude to match.
   * @param volume an arbitrary control factor, with e.g. 0.5 for half the volume.
   * @return the amplified audio stream.
   */
  public AudioInputStream getLeveledStream(AudioInputStream audioInputStream, float desiredRelativeAmplitude, double volume) {
    AmplitudeAudioInputStream amplitudeAudioInputStream = new AmplitudeAudioInputStream(audioInputStream);
    amplitudeAudioInputStream.setAmplitudeLinear((float) (desiredRelativeAmplitude * volume));
    return amplitudeAudioInputStream;
  }

  public AudioInputStream getStreamFromInputStream(InputStream inputStream, long streamSize) {
    return new AudioInputStream(inputStream, TARGET_AUDIO_FORMAT, (streamSize / TARGET_AUDIO_FORMAT.getFrameSize()));
  }

  public byte[] getStreamPart(AudioInputStream inputStream, Long startTimeMS, Long durationMS) throws IOException {

    // First skip to the desired position
    long bytesToSkip = Math.round((startTimeMS / 1000.0) * inputStream.getFormat().getSampleRate() * inputStream.getFormat().getFrameSize());
    long skippedBytes = inputStream.skip(bytesToSkip);
    if (bytesToSkip != skippedBytes) {
      addDebugMessage("Tried to skip " + bytesToSkip + " bytes but only skipped " + skippedBytes + " bytes.");
    }

    byte[] result = new byte[Math.round(((float) durationMS / 1000) * inputStream.getFormat().getSampleRate() * inputStream.getFormat().getFrameSize())];

    // Choose a buffer of 100 KB
    byte[] audioBytes = new byte[102400];

    int numberOfBytesRead;
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

  public byte[] getExtract(byte[] data, long startMs, long durationMs) {
    long startBytes = getSamplesFromMilliseconds(startMs);
    long durationBytes = getSamplesFromMilliseconds(durationMs);
    byte[] result = new byte[(int) durationBytes];

    System.arraycopy(data, (int) startBytes, result, 0, result.length);

    return result;
  }

  private byte[] getByteArray(AudioInputStream inputStream) throws IOException {

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

  private long getStreamSizeInBytes(AudioInputStream inputStream) {

    // Calculate length of wav input stream
    AudioFormat af = inputStream.getFormat();

    long frameLength = inputStream.getFrameLength();
    long byteLength;

    int frameSize = af.getFrameSize();

    byteLength = frameLength * frameSize;

    return byteLength;
  }

  public byte[] generateSilenceOfLength(double lengthInMs) {
    int samples = getSamplesFromMilliseconds(lengthInMs);

    byte[] silenceBuffer = new byte[samples];
    for (int i = 0; i < silenceBuffer.length; i = i + 2) {
      silenceBuffer[i] = (byte) 0x80;
      silenceBuffer[i + 1] = (byte) 0x00;
    }

    return silenceBuffer;
  }

  public int getSamplesFromMilliseconds(double milliseconds) {
    return (int) (milliseconds * SAMPLE_RATE * TARGET_AUDIO_FORMAT.getFrameSize() / 1000.0);
  }

  public AudioInputStream getStreamExtract(AudioInputStream ais, int start, int length) {

    long startMs = start * 1000L;
    long durationMs = length * 1000L;

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

      int extractFrameLength = (int) (TARGET_AUDIO_FORMAT.getSampleRate() * TARGET_AUDIO_FORMAT.getFrameSize() * (durationMs / 1000.0));
      result = new AudioInputStream(new ByteArrayInputStream(streamExtract), TARGET_AUDIO_FORMAT, extractFrameLength);
    }
    catch (IOException e) {
      // nop
    }

    return result;
  }

  /**
   * Fades the provided sample array in at the start and out at the end. The respective fading part length is determined
   * by the blendTime argument. Additionally, a blend factor can be specified which denotes how the blending should be scaled.
   */
  public byte[] doubleSidedLinearBlend(byte[] sampleByteArray, double blendTimeMs, double startFactor, double endFactor) {

    int sampleLength = sampleByteArray.length;
    int blendSamples = getSamplesFromMilliseconds(blendTimeMs);
    double factorPerSample = (endFactor - startFactor) / blendSamples;

    if (((double) sampleByteArray.length / 2) < blendSamples) {
      blendSamples = (int) ((double) sampleByteArray.length / 2);
    }

    int valueFront;
    int valueEnd;

    for (int i = 0; i < blendSamples; i = i + 2) {
      // Determine effective value of each two bytes
      valueFront = (sampleByteArray[i] & 0xFF) | (sampleByteArray[i + 1] << 8);
      valueEnd = (sampleByteArray[sampleLength - 1 - i - 1] & 0xFF) | (sampleByteArray[sampleLength - 1 - i] << 8);

      // Adapt value
      valueFront = (int) Math.round((startFactor + (i * factorPerSample)) * valueFront);
      valueEnd = (int) Math.round((startFactor + (i * factorPerSample)) * valueEnd);

      // Write it back into array
      sampleByteArray[i] = (byte) (valueFront & 0xFF);
      sampleByteArray[i + 1] = (byte) (valueFront >> 8);
      sampleByteArray[sampleLength - 1 - i - 1] = (byte) (valueEnd & 0xFF);
      sampleByteArray[sampleLength - 1 - i] = (byte) (valueEnd >> 8);
    }

    return sampleByteArray;
  }



  public byte[] fadeOut(byte[] sample) {
    return fade(sample, 1, 0);
  }

  public byte[] fadeIn(byte[] sample) {
    return fade(sample, 0, 1);
  }

  /**
   * Creates a faded (blended) copy of the provided sample, with linear fading from startFactor at the beginning to endFactor at the end.
   * The two control arguments startFactor and endFactor are supposed to be in [0, 1].
   */
  public byte[] fade(byte[] sample, double startFactor, double endFactor) {
    byte[] result = new byte[sample.length];

    double factorPerSample = (endFactor - startFactor) / sample.length;

    int audioValue;
    for (int i = 0; i < sample.length; i = i + 2) {
      audioValue = (sample[i] & 0xFF) | (sample[i + 1] << 8);

      // Perform fading on audio value
      audioValue = (int) Math.round((startFactor + (i * factorPerSample)) * audioValue);

      result[i] = (byte) (audioValue & 0xFF);
      result[i + 1] = (byte) (audioValue >> 8);
    }
    return result;
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
    boolean bDoConvertEndianness = (stream.getFormat().isBigEndian() != TARGET_AUDIO_FORMAT.isBigEndian());

    if (bDoConvertSampleSize || bDoConvertEndianness) {
      stream = convertSampleSizeAndEndianness(TARGET_AUDIO_FORMAT.getSampleSizeInBits(), TARGET_AUDIO_FORMAT.isBigEndian(), stream);
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

  private AudioInputStream convertSampleSizeAndEndianness(int nSampleSizeInBits, boolean bBigEndian, AudioInputStream sourceStream) {
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

}
