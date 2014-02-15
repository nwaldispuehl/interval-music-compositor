package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFileProperties;

/**
 * @author nw
 */
public class FlacFileProperties extends AudioFileProperties {

  private final static String FLAC_SUFFIX = "flac";

  private final static String FLAC_MAGIC_NUMBER = "66 4C 61 43";

  @Override
  protected String[] getFileExtensions() {
    return new String[] { FLAC_SUFFIX };
  }

  @Override
  protected String[] getMagicNumberHexStrings() {
    return new String[] { FLAC_MAGIC_NUMBER };
  }
}
