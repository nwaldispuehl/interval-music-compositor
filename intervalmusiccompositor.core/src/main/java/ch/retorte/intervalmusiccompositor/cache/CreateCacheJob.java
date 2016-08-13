package ch.retorte.intervalmusiccompositor.cache;

import java.util.ArrayList;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Representation of a job for creating an audio files raw data cache.
 * 
 * @author nw
 */
public class CreateCacheJob implements Runnable {

  private IAudioFile audioFile;
  private ArrayList<TaskFinishListener> listeners = new ArrayList<>();
  private MessageProducer messageProducer;

  public CreateCacheJob(IAudioFile audioFile, MessageProducer messageProducer) {
    this.audioFile = audioFile;
    this.messageProducer = messageProducer;
  }

  void addListener(TaskFinishListener l) {
    listeners.add(l);
  }

  private void notifyListeners() {
    listeners.forEach(TaskFinishListener::onTaskFinished);
  }

  public IAudioFile getAudioFile() {
    return audioFile;
  }

  @Override
  public void run() {
    try {
      audioFile.createCache();
    }
    catch (Exception e) {
      addDebugMessage(e.getMessage());
    }
    finally {
      notifyListeners();
    }

    this.audioFile = null;
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  @Override
  public String toString() {
    return "Cache job for " + audioFile;
  }
}
