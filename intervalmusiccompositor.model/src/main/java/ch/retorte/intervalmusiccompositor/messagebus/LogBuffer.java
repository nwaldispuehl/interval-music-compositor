package ch.retorte.intervalmusiccompositor.messagebus;

import com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Observable;

/**
 * The log buffer holds all relevant log messages for later retrieval.
 */
public class LogBuffer extends Observable {

  //---- Static

  private static final String LINE_SEPARATOR = System.lineSeparator();

  //---- Fields

  private LinkedList<String> buffer = Lists.newLinkedList();


  //---- Constructor

  LogBuffer() {}

  //---- Methods

  void append(LocalDateTime localDateTime, String caller, String line) {
      buffer.add(getFormattedDateFrom(localDateTime) + " - " + getFormattedCallerFrom(caller) + " - " + line);
      setChanged();
      notifyObservers();
  }

  private String getFormattedDateFrom(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return DebugMessage.format(LocalDateTime.now());
    }
    else {
      return DebugMessage.format(localDateTime);
    }
  }

  private String getFormattedCallerFrom(String caller) {
    if (caller == null) {
      return "unknown";
    }
    else {
      return caller;
    }
  }

  @Override
  public String toString() {
    return ((LinkedList<String>) buffer.clone()).stream().reduce("", (s, s2) -> s + LINE_SEPARATOR + s2);
  }
}
