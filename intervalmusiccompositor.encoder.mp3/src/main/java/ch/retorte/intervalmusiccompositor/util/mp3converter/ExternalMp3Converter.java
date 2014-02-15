package ch.retorte.intervalmusiccompositor.util.mp3converter;

import java.io.File;
import java.io.IOException;

/**
 * @author nw
 */
public abstract class ExternalMp3Converter {

  final String options = "--silent -h -V 2";

  public Boolean isLamePresent() {
    return getLocalBinary().exists() || getSystemBinary().exists();
  }

  public void convertToMp3(File inputFile, File outputFile) throws IOException {

    String execution_string = getAbsoluteLamePath() + " " + options + " " + inputFile.getAbsolutePath() + " " + outputFile.getAbsolutePath();

    Process p = Runtime.getRuntime().exec(execution_string);

    try {
      p.waitFor();
    }
    catch (InterruptedException e) {
      // We warm-heartedly convert the thread exception into an io exception.
      throw new IOException(e);
    }
  }

  protected abstract File getSystemBinary();

  protected abstract File getLocalBinary();

  private String getAbsoluteLamePath() {
    if (getLocalBinary().exists()) {
      return getLocalBinary().getAbsolutePath();
    }
    return getSystemBinary().getAbsolutePath();
  }

}
