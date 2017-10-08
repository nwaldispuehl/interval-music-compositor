package ch.retorte.intervalmusiccompositor.commons.platform;

import static javax.swing.filechooser.FileSystemView.getFileSystemView;

/**
 * @author nw
 */
public class WindowsPlatform extends Platform {

  WindowsPlatform(String osName) {
    super(osName);
  }

  /**
   * This is something of a hack to determine the os type even with a 32bit JVM.
   *
   * Taken from: <a href="https://stackoverflow.com/a/5940770">https://stackoverflow.com/a/5940770</a>.
   */
  @Override
  protected String getOsArchitecture() {
    String arch = System.getenv("PROCESSOR_ARCHITECTURE");
    String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
    return arch != null && arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "amd64" : "x86";
  }

  @Override
  public String getDesktopPath() {
    return getFileSystemView().getHomeDirectory().getAbsolutePath();
  }

  @Override
  public String getHomeDirectoryPath() {
    return getFileSystemView().getDefaultDirectory().getAbsolutePath();
  }

}
