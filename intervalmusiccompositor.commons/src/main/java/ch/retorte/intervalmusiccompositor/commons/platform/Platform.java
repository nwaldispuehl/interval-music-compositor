package ch.retorte.intervalmusiccompositor.commons.platform;


/**
 * Central access point for information about the underlying platform.
 * 
 * @author nw
 */
public abstract class Platform {

  private final String osName;

  public Platform(String osName) {
    this.osName = osName;
  }

	public boolean isLinux() {
    return this instanceof LinuxPlatform;
	}
	
	public boolean isWindows() {
    return this instanceof WindowsPlatform;
	}
	
	public boolean isMac() {
    return this instanceof MacOsxPlatform;
	}
	
  public String getOSName() {
    return osName;
	}
	
	public int getCores() {
		return Runtime.getRuntime().availableProcessors();
	}

  public String getArchitecture() {
    return System.getProperty("os.arch");
  }

  /**
   * Returns the absolute path to the desktop directory on the respective operating system.
   */
  public abstract String getDesktopPath();

  /**
   * Returns the absolute path to the current users home directory on the respective operating system.
   */
  public abstract String getHomeDirectoryPath();

}
