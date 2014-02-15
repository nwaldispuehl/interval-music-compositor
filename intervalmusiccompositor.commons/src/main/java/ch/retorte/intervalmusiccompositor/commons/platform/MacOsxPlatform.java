package ch.retorte.intervalmusiccompositor.commons.platform;

/**
 * @author nw
 */
public class MacOsxPlatform extends Platform {

  public MacOsxPlatform(String osName) {
    super(osName);
  }

  @Override
  public String getDesktopPath() {
    return System.getProperty("user.home") + "/Desktop/";
  }

  @Override
  public String getHomeDirectoryPath() {
    return System.getProperty("user.home");
  }

}
