package ch.retorte.intervalmusiccompositor.commons.platform;


/**
 * Central access point for information about the underlying platform.
 */
public abstract class Platform {

  //---- Fields

  private final String osName;


  //---- Constructor

  public Platform(String osName) {
    this.osName = osName;
  }


  //---- Methods: Operating system

  boolean isLinux() {
    return this instanceof LinuxPlatform;
  }

  boolean isWindows() {
    return this instanceof WindowsPlatform;
  }

  boolean isMac() {
    return this instanceof MacOsxPlatform;
  }

  private String getOsName() {
    return osName;
  }


  //---- Methods: Platform properties

  public int getCores() {
    return Runtime.getRuntime().availableProcessors();
  }

  private String getOsArchitecture() {
    return System.getProperty("os.arch");
  }

  private String getJvmName() {
    return System.getProperty("java.version");
  }

  private String getJvmArchitecture() {
    return System.getProperty("sun.arch.data.model");
  }

  private String getMaximalHeapSize() {
    return Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB";
  }

  public String getSystemIdentificationString() {
    return "os: " + getOsName() + ", " //
        + "os arch: " + getOsArchitecture() + ", " //
        + "jvm: " + getJvmName() + ", " //
        + "jvm arch: " + getJvmArchitecture() + ", " //
        + "max heap size: " + getMaximalHeapSize();
  }


  //---- Abstract methods

  /**
   * Returns the absolute path to the desktop directory on the respective operating system.
   */
  public abstract String getDesktopPath();

  /**
   * Returns the absolute path to the current users home directory on the respective operating system.
   */
  public abstract String getHomeDirectoryPath();

}
