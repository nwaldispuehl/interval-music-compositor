package ch.retorte.intervalmusiccompositor.commons.platform;

import static java.lang.System.getProperty;

/**
 * @author nw
 */
public class PlatformFactory {

  private static final String LINUX_PREFIX = "Lin";
  private static final String WINDOWS_PREFIX = "Win";
  private static final String MACOSX_PREFIX = "Mac";
  private final String osName;

  public PlatformFactory() {
    this.osName = getProperty("os.name");
  }

  PlatformFactory(String osName) {
    this.osName = osName;
  }

  public Platform getPlatform() {
    if (platformStartsWith(LINUX_PREFIX)) {
      return new LinuxPlatform(osName);
    }
    else if (platformStartsWith(WINDOWS_PREFIX)) {
      return new WindowsPlatform(osName);
    }
    else if (platformStartsWith(MACOSX_PREFIX)) {
      return new MacOsxPlatform(osName);
    }

    throw new PlatformNotFoundException("Unknown platform: " + osName);
  }

  private boolean platformStartsWith(String prefix) {
    return osName.startsWith(prefix);
  }

}
