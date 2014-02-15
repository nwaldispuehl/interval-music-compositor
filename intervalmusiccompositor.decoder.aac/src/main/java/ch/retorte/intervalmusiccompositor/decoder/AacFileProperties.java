package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFileProperties;

/**
 * @author nw
 */
public class AacFileProperties extends AudioFileProperties {

  public final static String AAC_SUFFIX = "aac";
  public final static String M4A_SUFFIX = "m4a";
  public final static String MP4_SUFFIX = "mp4";

  public final static String M4A_MAGIC_NUMBER = "00 00 00 nn 66 74 79 70 4D 34 41";
  public final static String AAC_MAGIC_NUMBER = "FF F1";
  public final static String AAC_ALT_MAGIC_NUMBER = "FF F9";

  @Override
  protected String[] getFileExtensions() {
    return new String[] { AAC_SUFFIX, M4A_SUFFIX, MP4_SUFFIX };
  }

  @Override
  protected String[] getMagicNumberHexStrings() {
    return new String[] { M4A_MAGIC_NUMBER, AAC_MAGIC_NUMBER, AAC_ALT_MAGIC_NUMBER };
  }

}
