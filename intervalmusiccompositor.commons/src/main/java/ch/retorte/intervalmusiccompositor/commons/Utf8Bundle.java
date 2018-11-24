package ch.retorte.intervalmusiccompositor.commons;

import java.util.ResourceBundle;

/**
 * @author nw
 */
public class Utf8Bundle {

  public static MessageFormatBundle getBundle(String baseName) {
    // TODO removed utf8 control; how to fix?
    return new MessageFormatBundle(ResourceBundle.getBundle(baseName));
  }

}
