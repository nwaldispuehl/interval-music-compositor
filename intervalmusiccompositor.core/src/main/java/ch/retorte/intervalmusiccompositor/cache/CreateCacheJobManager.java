package ch.retorte.intervalmusiccompositor.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFile;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Manages pending jobs for cache creation of imported music tracks.
 */
public class CreateCacheJobManager {

  private final ConcurrentLinkedQueue<CreateCacheJob> pendingJobs;
  private final MessageProducer messageProducer;
  private final int threadLimit;
  private int threadCount = 0;

  public CreateCacheJobManager(MessageProducer messageProducer, int threadLimit) {
    this.messageProducer = messageProducer;
    this.threadLimit = threadLimit;
    this.pendingJobs = new ConcurrentLinkedQueue<>();
  }

  private synchronized void dispatchOldestJob() {
    if (!pendingJobs.isEmpty()) {
      CreateCacheJob job = pendingJobs.poll();
      job.addListener(this::notifyJobTermination);
      dispatch(job);
    }
  }

  private void dispatch(CreateCacheJob j) {
    new Thread(j).start();
    threadCount++;
    addDebugMessagesWith("Dispatched job: " + j);
  }

  public void addNewJob(CreateCacheJob j) {
    add(j);
    runJobs();
  }

  private void add(CreateCacheJob j) {
    ((AudioFile) j.getAudioFile()).setQueuedStatus();
    pendingJobs.add(j);
  }

  private void notifyJobTermination() {
    synchronized (this) {
      threadCount--;
    }

    addDebugMessagesWith("Collected job.");
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
    return !pendingJobs.isEmpty();
  }

  private boolean isBelowConcurrentJobsLimit() {
    return threadCount < threadLimit;
  }

  private void addDebugMessagesWith(String text) {
    messageProducer.send(new DebugMessage(this, text));
    messageProducer.send(new DebugMessage(this, "Running jobs: " + threadCount + " (maximum: " + threadLimit + ")"));
  }

}
