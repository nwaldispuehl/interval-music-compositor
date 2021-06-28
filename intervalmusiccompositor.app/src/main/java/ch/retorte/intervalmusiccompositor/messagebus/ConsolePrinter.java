package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Prints messages to system consoles.
 */
class ConsolePrinter {

    void printToStdOut(String message) {
        System.out.println(message);
    }

    void printToStdErr(String message) {
        System.err.println(message);
    }
}
