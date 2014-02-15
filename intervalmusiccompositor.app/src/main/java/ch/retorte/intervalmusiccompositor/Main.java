package ch.retorte.intervalmusiccompositor;

/**
 * Collects any command line arguments and starts the actual software.
 * 
 * @author nw
 */
public class Main {

  public static void main(String[] args) {
    new IntervalMusicCompositor().startApp(isDebugFlagSet(args));
  }

  private static boolean isDebugFlagSet(String[] args) {
    for (String s : args) {
      if (s.equals("-d")) {
        return true;
      }
    }
    return false;
  }

}
