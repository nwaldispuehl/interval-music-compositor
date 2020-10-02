package ch.retorte.intervalmusiccompositor.decoder.mp3;

import ch.retorte.intervalmusiccompositor.model.audiofile.AudioFileProperties;

/**
 * @author nw
 */
public class Mp3FileProperties extends AudioFileProperties {

  private static final String MP3_SUFFIX = "mp3";

  private static final String MP3_MAGIC_NUMBER = "FF FA";
  private static final String MP3_ALT_MAGIC_NUMBER = "FF FB";
  private static final String MP3_ALT2_MAGIC_NUMBER = "FF F3";
  private static final String ID3_MAGIC_NUMBER = "49 44 33";

  @Override
  protected String[] getFileExtensions() {
    return new String[] { MP3_SUFFIX };
  }

  @Override
  protected String[] getMagicNumberHexStrings() {
    return new String[] { MP3_MAGIC_NUMBER, ID3_MAGIC_NUMBER, MP3_ALT_MAGIC_NUMBER, MP3_ALT2_MAGIC_NUMBER };
  }

}
