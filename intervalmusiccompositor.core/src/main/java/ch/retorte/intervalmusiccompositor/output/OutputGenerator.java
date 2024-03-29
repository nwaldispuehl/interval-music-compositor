package ch.retorte.intervalmusiccompositor.output;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressUpdatable;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * Generates the encoded compilation data.
 */
public class OutputGenerator {

  private final SoundHelper soundHelper;
  private final List<AudioFileEncoder> encoders;
  private final MessageProducer messageProducer;

  public OutputGenerator(SoundHelper soundHelper, List<AudioFileEncoder> encoders, MessageProducer messageProducer) {
    this.soundHelper = soundHelper;
    this.encoders = encoders;
    this.messageProducer = messageProducer;
  }

  public void generateOutput(FileInputStream audioFileInputStream, long streamLengthInBytes, String path, String filePrefix, String encoderIdentifier, ProgressListener progressListener) {
    AudioFileEncoder encoder = getEncoderFor(encoderIdentifier);

    File outputFile = generateOutputFile(encoder, path, filePrefix);
    addDebugMessage("Encoding compilation data with: " + encoder.getClass().getSimpleName() + " to: " + outputFile);

    try (AudioInputStream audioInputStream = soundHelper.getStreamFromInputStream(audioFileInputStream, streamLengthInBytes)) {
      checkForProgressIndicationCapabilities(encoder, progressListener);
      encoder.encode(audioInputStream, streamLengthInBytes, outputFile);
    }
    catch (Exception e) {
      addDebugMessage(e.getMessage());
      addErrorMessage(e.getMessage());
    }
  }

  private void checkForProgressIndicationCapabilities(AudioFileEncoder encoder, ProgressListener progressListener) {
    if (encoder instanceof ProgressUpdatable) {
      ((ProgressUpdatable) encoder).setProgressListener(progressListener);
    }
  }

  public List<AudioFileEncoder> getEncoders() {
    return encoders;
  }

  private AudioFileEncoder getEncoderFor(String encoderIdentifier) {
    for (AudioFileEncoder encoder : encoders) {
      try {
        if (encoder.isAbleToEncode() && encoder.getIdentificator().equals(encoderIdentifier)) {
          return encoder;
        }
      }
      catch (Exception e) {
        addDebugMessage("Encoder " + encoder + " threw exception on isAbleToEncode():" + e.getMessage());
        // Go to the next encoder if this one does not bring it.
      }
    }
    throw new IllegalStateException("No encoder found.");
  }

  private File generateOutputFile(AudioFileEncoder encoder, String path, String filePrefix) {
    String normalizedPath = path;
    if (!normalizedPath.endsWith(separator)) {
      normalizedPath += separator;
    }

    return new File(normalizedPath + filePrefix + "." + encoder.getFileExtension());
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void addErrorMessage(String message) {
    messageProducer.send(new ErrorMessage(message));
  }
}
