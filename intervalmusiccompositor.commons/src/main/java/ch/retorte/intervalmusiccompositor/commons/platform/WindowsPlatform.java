package ch.retorte.intervalmusiccompositor.commons.platform;

import static javax.swing.filechooser.FileSystemView.getFileSystemView;

/**
 * @author nw
 */
public class WindowsPlatform extends Platform {

  WindowsPlatform(String osName) {
    super(osName);
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
