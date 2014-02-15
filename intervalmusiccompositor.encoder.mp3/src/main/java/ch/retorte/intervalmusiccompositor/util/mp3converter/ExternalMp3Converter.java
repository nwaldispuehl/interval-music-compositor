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

  public void convertToMp3(File inputFile, File outputFile) {

    String execution_string = getAbsoluteLamePath() + " " + options + " " + inputFile.getAbsolutePath() + " " + outputFile.getAbsolutePath();

    Process p = null;
    try {
      p = Runtime.getRuntime().exec(execution_string);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    try {
      p.waitFor();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
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
