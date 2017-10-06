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

  private String getOsArchitecture() {
    return System.getProperty("os.arch");
  }

  private String getJvmName() {
    return System.getProperty("java.version");
  }

  private String getJvmArchitecture() {
    return System.getProperty("sun.arch.data.model");
  }

  private String getCurrentHeapSize() {
    return asMegaBytes(Runtime.getRuntime().totalMemory());
  }

  private String getFreeMemory() {
    return asMegaBytes(Runtime.getRuntime().freeMemory());
  }

  private String getMaximalHeapSize() {
    return asMegaBytes(Runtime.getRuntime().maxMemory());
  }

  private String getSystemMemory() {
    return asMegaBytes(((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize());
  }

  private String getTotalHddSize() {
    return asMegaBytes(new File(".").getTotalSpace());
  }

  private String asMegaBytes(long bytes) {
    return bytes / (1024 * 1024) + " MB";
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
        + "free hdd: " + getTotalHddSize();
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
