package ch.retorte.intervalmusiccompositor.output;

import static java.io.File.separator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * @author nw
 */
public class OutputGenerator {

  private List<AudioFileEncoder> encoders;
  private SoundHelper soundHelper;
  private MessageProducer messageProducer;

  public OutputGenerator(SoundHelper soundHelper, List<AudioFileEncoder> encoders, MessageProducer messageProducer) {
    this.encoders = encoders;
    this.soundHelper = soundHelper;
    this.messageProducer = messageProducer;
  }

  public void generateOutput(byte[] soundData, String path, String filePrefix) {
    AudioInputStream audioInputStream = soundHelper.getStreamFromByteArray(soundData);
    AudioFileEncoder encoder = getEncoder();


    File outputFile = generateOutputFile(encoder, path, filePrefix);
    addDebugMessage("Encoding compilation data with: " + encoder.getClass().getSimpleName() + " to: " + outputFile);

    try {
      encoder.encode(audioInputStream, outputFile);
    }
    catch (UnsupportedAudioFileException e) {
      addDebugMessage(e.getMessage());
      addErrorMessage(e.getMessage());
    }
    catch (IOException e) {
      addDebugMessage(e.getMessage());
      addErrorMessage(e.getMessage());
    }
  }

  private File generateOutputFile(AudioFileEncoder encoder, String path, String filePrefix) {
    String normalizedPath = path;
    if (!normalizedPath.endsWith(separator)) {
      normalizedPath += separator;
    }

    return new File(normalizedPath + filePrefix + "." + encoder.getFileExtension());
  }

  private AudioFileEncoder getEncoder() {
    for (AudioFileEncoder encoder : encoders) {
      try {
        if (encoder.isAbleToEncode()) {
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

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void addErrorMessage(String message) {
    messageProducer.send(new ErrorMessage(message));
  }

}
