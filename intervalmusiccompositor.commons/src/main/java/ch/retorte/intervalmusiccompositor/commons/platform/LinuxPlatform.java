package ch.retorte.intervalmusiccompositor.commons.platform;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Access to information of the Linux platform.
 */
public class LinuxPlatform extends Platform {

    //---- Statics

    private static final String DEFAULT_DESKTOP_DIRECTORY_SUFFIX = "/Desktop/";
    private static final String XDG_USER_DIR_FILE_SUFFIX = "/.config/user-dirs.dirs";
    private static final String XDG_USER_DIR_FILE_DESKTOP_KEY = "XDG_DESKTOP_DIR";


    //---- Constructor

    LinuxPlatform(String osName) {
        super(osName);
    }


    //---- Methods

    @Override
    public String getOsIdentifier() {
        return "linux";
    }

    @Override
    public String getDesktopPath() {
        String desktopPath = getHomeDirectoryPath() + DEFAULT_DESKTOP_DIRECTORY_SUFFIX;

        if (!new File(desktopPath).exists()) {
            // If this obvious desktop path does not exist, we look up the path in the XDG user directory file.
            try {
                Properties userDirProperties = new Properties();
                userDirProperties.load(new FileReader(getHomeDirectoryPath() + XDG_USER_DIR_FILE_SUFFIX));
                String userDirFileDesktop = userDirProperties.getProperty(XDG_USER_DIR_FILE_DESKTOP_KEY);
                return new File(cleanup(userDirFileDesktop)).getAbsolutePath();
            }
            catch (Exception e) {
                // In case of any failure, we fall back to the home directory.
                return getHomeDirectoryPath();
            }
        }

        return desktopPath;
    }

    private String cleanup(String userDirFileDesktop) {
        return userDirFileDesktop.replaceAll( "\"", "").replaceAll("\\$HOME", getHomeDirectoryPath());
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

    @Override
    public int getMaximumPathLength() {
        /* Note: This is actually the maximum filename length. */
        return 255;
    }
}
