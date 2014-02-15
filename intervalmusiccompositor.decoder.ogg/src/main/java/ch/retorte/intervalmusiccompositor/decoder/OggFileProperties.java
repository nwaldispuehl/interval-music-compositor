package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFileProperties;

/**
 * @author nw
 */
public class OggFileProperties extends AudioFileProperties {

  private static final String OGG_SUFFIX = "ogg";

  private static final String OGG_MAGIC_NUMBER = "4F 67 67 53";

  @Override
  protected String[] getFileExtensions() {
    return new String[] { OGG_SUFFIX };
  }

  @Override
  protected String[] getMagicNumberHexStrings() {
    return new String[] { OGG_MAGIC_NUMBER };
  }
}
