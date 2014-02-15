package ch.retorte.intervalmusiccompositor.commons;

import java.util.ResourceBundle;

/**
 * @author nw
 */
public class Utf8Bundle {

  public static MessageFormatBundle getBundle(String baseName) {
    return new MessageFormatBundle(ResourceBundle.getBundle(baseName, new Utf8Control()));
  }

}
