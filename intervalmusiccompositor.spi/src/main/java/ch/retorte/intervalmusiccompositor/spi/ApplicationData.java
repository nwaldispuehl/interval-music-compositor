package ch.retorte.intervalmusiccompositor.spi;

import ch.retorte.intervalmusiccompositor.model.update.Version;

import java.util.List;
import java.util.Locale;

/**
 * Offers fundamental application data.
 */
public interface ApplicationData {

  String getProgramName();

  Version getProgramVersion();

  String getChangeLog();

  List<Locale> getKnownLocales();

}
