package ch.retorte.intervalmusiccompositor.spi;

import ch.retorte.intervalmusiccompositor.Version;

import java.util.List;
import java.util.Locale;

/**
 * Offers fundamental application data.
 */
public interface ApplicationData {

  String getProgramName();

  Version getProgramVersion();

  List<Locale> getKnownLocales();

  Locale getLocale();

}
