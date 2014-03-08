package ch.retorte.intervalmusiccompositor.cache;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * @author nw
 */
public class CreateCacheJob implements Runnable {

  private IAudioFile audioFile;
  private ArrayList<TaskFinishListener> listeners = new ArrayList<TaskFinishListener>();
  private MessageProducer messageProducer;

  public CreateCacheJob(IAudioFile audioFile, MessageProducer messageProducer) {
    this.audioFile = audioFile;
    this.messageProducer = messageProducer;
  }

  public void addListener(TaskFinishListener l) {
    listeners.add(l);
  }

  private void notifyListeners() {

    for (final TaskFinishListener l : listeners) {
      l.onTaskFinished();
    }
  }

  public IAudioFile getAudioFile() {
    return audioFile;
  }

  @Override
  public void run() {
    try {
      audioFile.createCache();
    }
    catch (UnsupportedAudioFileException e) {
      addDebugMessage(e.getMessage());
    }
    catch (IOException e) {
      addDebugMessage(e.getMessage());
    }

    this.audioFile = null;

    notifyListeners();
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
