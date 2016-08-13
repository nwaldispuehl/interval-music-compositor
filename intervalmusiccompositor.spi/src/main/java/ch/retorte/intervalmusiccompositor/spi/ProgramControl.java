package ch.retorte.intervalmusiccompositor.spi;

import ch.retorte.intervalmusiccompositor.messagebus.LogBuffer;

/**
 * @author nw
 */
public interface ProgramControl {

  void quit();

  LogBuffer getLogBuffer();

}
