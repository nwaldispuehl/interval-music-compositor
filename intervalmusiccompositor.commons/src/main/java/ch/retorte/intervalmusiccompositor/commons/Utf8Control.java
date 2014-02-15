package ch.retorte.intervalmusiccompositor.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

/**
 * Provides a bundle control to deal with text in a UTF-8 charset.
 * 
 * @author nw
 */
public class Utf8Control extends Control {

  /*
   * Code taken from: http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle/4660195#4660195
   */
  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException,
      InstantiationException, IOException {

    String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
    ResourceBundle bundle = null;
    InputStream stream = null;

    if (reload) {
      URL url = loader.getResource(resourceName);
      if (url != null) {
        URLConnection connection = url.openConnection();
        if (connection != null) {
          connection.setUseCaches(false);
          stream = connection.getInputStream();
        }
      }
    }
    else {
      stream = loader.getResourceAsStream(resourceName);
    }
    if (stream != null) {
      try {
        // Only this line is changed to make it to read properties files as UTF-8.
        bundle = new PropertyResourceBundle(new InputStreamReader(stream, Charsets.UTF_8));
      }
      finally {
        stream.close();
      }
    }
    return bundle;
  }

  @Override
  public List<String> getFormats(String baseName) {
    return Lists.newArrayList("java.properties");
  }

}
