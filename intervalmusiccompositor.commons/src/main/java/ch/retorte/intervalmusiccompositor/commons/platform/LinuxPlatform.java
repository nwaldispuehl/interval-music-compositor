package ch.retorte.intervalmusiccompositor.commons.platform;

import javax.swing.filechooser.FileSystemView;

/**
 * @author nw
 */
public class LinuxPlatform extends Platform {

  public LinuxPlatform(String osName) {
    super(osName);
  }

  @Override
  public String getDesktopPath() {
    return getHomeDirectoryPath() + "/Desktop/";
  }

  @Override
  public String getHomeDirectoryPath() {
    return FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
  }

}
