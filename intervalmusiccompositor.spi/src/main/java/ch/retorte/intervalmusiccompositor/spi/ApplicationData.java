package ch.retorte.intervalmusiccompositor.spi;

import ch.retorte.intervalmusiccompositor.Version;

/**
 * @author nw
 */
public interface ApplicationData {

  String getProgramName();

  Version getProgramVersion();

}
