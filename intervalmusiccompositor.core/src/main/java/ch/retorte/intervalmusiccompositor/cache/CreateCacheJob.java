package ch.retorte.intervalmusiccompositor.cache;

import java.util.ArrayList;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Representation of a job for creating an audio files raw data cache.
 */
public class CreateCacheJob implements Runnable {

  private IAudioFile audioFile;
  private final ArrayList<TaskFinishListener> listeners = new ArrayList<>();
  private final MessageProducer messageProducer;

  public CreateCacheJob(IAudioFile audioFile, MessageProducer messageProducer) {
    this.audioFile = audioFile;
    this.messageProducer = messageProducer;
  }

  void addListener(TaskFinishListener l) {
    listeners.add(l);
  }

  private void notifyListeners() {
    for (TaskFinishListener l : listeners) {
      try {
        l.onTaskFinished();
      }
      catch (Exception e) {
        addErrorMessage(e);
      }
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
    catch (Exception e) {
      addErrorMessage(e);
    }
    finally {
      notifyListeners();
    }

    this.audioFile = null;
  }

  private void addErrorMessage(Throwable throwable) {
    messageProducer.send(new ErrorMessage(throwable));
    messageProducer.send(new DebugMessage(this, throwable.getMessage()));
  }

  @Override
  public String toString() {
    return "Cache job for " + audioFile;
  }
}
