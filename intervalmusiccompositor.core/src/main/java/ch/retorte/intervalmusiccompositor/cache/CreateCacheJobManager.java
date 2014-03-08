package ch.retorte.intervalmusiccompositor.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFile;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.ThreadHelper;

/**
 * @author nw
 */
public class CreateCacheJobManager implements Runnable {

  private ConcurrentLinkedQueue<CreateCacheJob> pendingJobs;
  private int threadLimit;
  private int threadCount = 0;
  private Boolean run = true;
  private ThreadHelper threadHelper;

  public CreateCacheJobManager(MessageProducer messageProducer, int threadLimit) {
    this.threadHelper = new ThreadHelper(messageProducer);
    this.threadLimit = threadLimit;
    this.pendingJobs = new ConcurrentLinkedQueue<CreateCacheJob>();
  }

  @Override
  public void run() {
    while (run) {
      if (canAcceptJobs()) {
        dispatchOldestJob();
      }

      threadHelper.sleep(200);

      if (!isJobAvailable()) {
        threadHelper.wait(this);
      }
    }
  }

  private boolean canAcceptJobs() {
    return isJobAvailable() && isBelowConcurrentJobsLimit();
  }

  private boolean isJobAvailable() {
    return 0 < pendingJobs.size();
  }

  private boolean isBelowConcurrentJobsLimit() {
    return threadCount < threadLimit;
  }

  private synchronized void dispatchOldestJob() {
    CreateCacheJob job = pendingJobs.poll();

    job.addListener(new TaskFinishListener() {
      @Override
      public void onTaskFinished() {
        notifyTermination();
      }
    });

    dispatch(job);
  }

  private synchronized void dispatch(CreateCacheJob j) {
    new Thread(j).start();
    threadCount++;
  }

  public void add(CreateCacheJob t) {
    threadHelper.notify(this);

    ((AudioFile) t.getAudioFile()).setQueuedStatus();
    pendingJobs.add(t);
  }

  public synchronized void notifyTermination() {
    threadCount--;
  }

  public void stop() {
    threadHelper.notify(this);

    run = false;
  }

}
