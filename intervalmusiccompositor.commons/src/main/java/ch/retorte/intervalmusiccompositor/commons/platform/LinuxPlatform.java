package ch.retorte.intervalmusiccompositor.commons.platform;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * Access to information of the Linux platform.
 */
public class LinuxPlatform extends Platform {

    LinuxPlatform(String osName) {
        super(osName);
    }

    @Override
    public String getOsDownloadDescriptor() {
        return "linux";
    }

    @Override
    public String getDesktopPath() {
        return getHomeDirectoryPath() + "/Desktop/";
    }

    @Override
    public String getHomeDirectoryPath() {
        return FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
    }

    @Override
    protected String getProgramDistributionRawBinPath() {
        return getUserDir() + File.separator + getEffectiveModulePath();
    }

    @Override
    protected String getProgramExecutableName() {
        return "intervalmusiccompositor.app";
    }

    @Override
    public String getUpgradeExecutableName() {
        return "intervalmusiccompositor-updater";
    }
}
