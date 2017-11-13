package ch.retorte.intervalmusiccompositor.commons.platform;


import java.io.File;
import java.lang.management.ManagementFactory;

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

  protected String getOsArchitecture() {
    return System.getProperty("os.arch");
  }

  private String getJvmName() {
    return System.getProperty("java.version");
  }

  private String getJvmArchitecture() {
    return System.getProperty("sun.arch.data.model");
  }

  private String getCurrentHeapSize() {
    return beautify(Runtime.getRuntime().totalMemory());
  }

  private String getFreeMemory() {
    return beautify(Runtime.getRuntime().freeMemory());
  }

  private String getMaximalHeapSize() {
    return beautify(Runtime.getRuntime().maxMemory());
  }

  private String getSystemMemory() {
    return beautify(((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize());
  }

  private String getFreeHddSize() {
    return beautify(new File(".").getFreeSpace());
  }

  private String getTotalHddSize() {
    return beautify(new File(".").getTotalSpace());
  }

  private String beautify(long bytes) {
    return humanReadableByteCount(bytes, true);
  }

  /**
   * Helper method taken from https://stackoverflow.com/a/3758880.
   */
  private static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public String getSystemDiagnosisString() {
    return "os: " + getOsName() + ", " //
        + "os arch: " + getOsArchitecture() + ", " //
        + "jvm: " + getJvmName() + ", " //
        + "jvm arch: " + getJvmArchitecture() + ", " //
        + "current heap: " + getCurrentHeapSize() + ", " //
        + "max heap: " + getMaximalHeapSize() + ", " //
        + "free mem: " + getFreeMemory() + ", " //
        + "total mem: " + getSystemMemory() + ", " //
        + "free hdd: " + getFreeHddSize() + ", " //
        + "total hdd: " + getTotalHddSize();
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
