package ch.retorte.intervalmusiccompositor.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFile;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.ThreadHelper;

/**
 * @author nw
 */
public class CacheQueueManager implements Runnable {

  private ConcurrentLinkedQueue<CacheQueueItem> threadList;
  private MessageProducer messageProducer;
  private int threadLimit;
  private int threadCount = 0;
  private Boolean run = true;

  public CacheQueueManager(MessageProducer messageProducer, int threadLimit) {
    this.messageProducer = messageProducer;
    this.threadLimit = threadLimit;
    threadList = new ConcurrentLinkedQueue<CacheQueueItem>();
  }

  @Override
  public void run() {
    while (run) {

      // Check if there are new CacheGenerators around and if we are
      // below the limit of allowed concurrent CacheGenerators
      if (0 < threadList.size() && threadCount < threadLimit) {

        // Retrieve oldest CacheQueueItem
        CacheQueueItem t = threadList.poll();

        // It should call back if it is finished
        t.addListener(new TaskFinishListener() {
          @Override
          public void onTaskFinished() {
            notifyTermination();
          }
        });

        // Send it on duty
        synchronized (this) {
          new Thread(t).start();
          threadCount++;
        }
      }

      new ThreadHelper(messageProducer).sleep(200);
    }
  }

  public void add(CacheQueueItem t) {
    ((AudioFile) t.getAudioFile()).setQueuedStatus();
    threadList.add(t);
  }

  public synchronized void notifyTermination() {
    threadCount--;
  }

  public void stop() {
    run = false;
  }

}
