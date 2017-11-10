package ch.retorte.intervalmusiccompositor;

/**
 * Collects any command line arguments and starts the actual software.
 *
 * The following flags are supported:
 * <pre>
 *   -d   Debug mode
 *        Prints debug information out to the terminal (i.e. to the standard out)
 *
 *   -c   Clear
 *        Clears all user preferences.
 * </pre>
 *
 */
public class Main {

  public static void main(String[] args) {
    new IntervalMusicCompositor().startApp(isDebugFlagSet(args), isClearFlagSet(args));
  }

  private static boolean isDebugFlagSet(String[] args) {
    return contains(args, "-d");
  }

  private static boolean isClearFlagSet(String[] args) {
    return contains(args, "-c");
  }

  private static boolean contains(String[] args, String arg) {
    for (String s : args) {
      if (s.equals(arg)) {
        return true;
      }
    }
    return false;
  }

}
