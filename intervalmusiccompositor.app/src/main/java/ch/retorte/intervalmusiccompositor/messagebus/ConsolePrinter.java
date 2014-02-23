package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Prints messages to system consoles.
 * 
 * @author nw
 */
public class ConsolePrinter {
  public void printToStdOut(String message) {
    System.out.println(message);
  }

  public void printToStdErr(String message) {
    System.err.println(message);
  }
}
