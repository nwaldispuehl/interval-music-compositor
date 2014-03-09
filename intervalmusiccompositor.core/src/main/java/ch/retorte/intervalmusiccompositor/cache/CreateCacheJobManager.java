package ch.retorte.intervalmusiccompositor.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFile;
import ch.retorte.intervalmusiccompositor.spi.TaskFinishListener;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Manages pending jobs for cache creation of imported music tracks.
 * 
 * @author nw
 */
public class CreateCacheJobManager {

  private ConcurrentLinkedQueue<CreateCacheJob> pendingJobs;
  private int threadLimit;
  private int threadCount = 0;

  public CreateCacheJobManager(MessageProducer messageProducer, int threadLimit) {
    this.threadLimit = threadLimit;
    this.pendingJobs = new ConcurrentLinkedQueue<CreateCacheJob>();
  }

  private synchronized void dispatchOldestJob() {
    CreateCacheJob job = pendingJobs.poll();

    job.addListener(new TaskFinishListener() {
      @Override
      public void onTaskFinished() {
        notifyJobTermination();
      }
    });

    dispatch(job);
  }

  private void dispatch(CreateCacheJob j) {
    new Thread(j).start();
    threadCount++;
  }

  public void addNewJob(CreateCacheJob j) {
    add(j);
    runJobs();
  }

  private void add(CreateCacheJob j) {
    ((AudioFile) j.getAudioFile()).setQueuedStatus();
    pendingJobs.add(j);
  }

  public void notifyJobTermination() {
    synchronized (this) {
      threadCount--;
    }

    runJobs();
  }

  private synchronized void runJobs() {
    while (canAcceptJobs()) {
      dispatchOldestJob();
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

}
