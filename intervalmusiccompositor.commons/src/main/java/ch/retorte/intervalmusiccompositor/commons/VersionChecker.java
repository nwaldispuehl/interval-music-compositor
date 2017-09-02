package ch.retorte.intervalmusiccompositor.commons;

import ch.retorte.intervalmusiccompositor.ChangeListener;
import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Checks for new program version.
 */
public class VersionChecker {

  //---- Fields

  private UpdateAvailabilityChecker updateAvailabilityChecker;


  //---- Constructor

  public VersionChecker(UpdateAvailabilityChecker updateAvailabilityChecker) {
    this.updateAvailabilityChecker = updateAvailabilityChecker;
  }


  //---- Methods

  public void startVersionCheckWith(ChangeListener<Version> newVersionListener, ChangeListener<Void> noNewVersionListener, ChangeListener<Exception> errorListener) {
    // We are creating a thread executor, but shutting it down right away so it gets collected once the task finishes.
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(new VersionCheckTask(newVersionListener, noNewVersionListener, errorListener));
    executorService.shutdown();
  }


  //---- Inner classes

  private class VersionCheckTask extends Task<String> {

    //---- Fields

    private final ChangeListener<Version> newVersionListener;
    private final ChangeListener<Void> noNewVersionListener;
    private final ChangeListener<Exception> errorListener;


    //---- Constructor

    VersionCheckTask(ChangeListener<Version> newVersionListener, ChangeListener<Void> noNewVersionListener, ChangeListener<Exception> errorListener) {
      this.newVersionListener = newVersionListener;
      this.noNewVersionListener = noNewVersionListener;
      this.errorListener = errorListener;
    }


    //---- Methods

    @Override
    protected String call() throws Exception {
      try {
        boolean updateAvailable = updateAvailabilityChecker.isUpdateAvailable();
        if (updateAvailable) {
          newVersionListener.changed(updateAvailabilityChecker.getLatestVersion());
        }
        else {
          noNewVersionListener.changed(null);
        }
      }
      catch (Exception e) {
        errorListener.changed(e);
      }
      return null;
    }
  }

}
