package ch.retorte.intervalmusiccompositor;

/**
 * Collects any command line arguments and starts the actual software.
 * <p>
 * The following flags are supported:
 * <pre>
 *   -d   Debug mode
 *        Prints debug information out to the terminal (i.e. to the standard out)
 *
 *   -c   Clear
 *        Clears all user preferences.
 * </pre>
 */
public class Main {

    //---- Constants

    private static final String DEBUG_FLAG = "-d";
    private static final String CLEAR_FLAG = "-c";


    //---- Main method

    public static void main(String[] args) {
        new IntervalMusicCompositor().startApp(isDebugFlagSet(args), isClearFlagSet(args));
    }


    //---- Methods

    private static boolean isDebugFlagSet(String[] args) {
        return contains(args, DEBUG_FLAG);
    }

    private static boolean isClearFlagSet(String[] args) {
        return contains(args, CLEAR_FLAG);
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
