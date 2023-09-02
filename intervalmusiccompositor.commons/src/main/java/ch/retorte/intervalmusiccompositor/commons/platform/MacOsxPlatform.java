package ch.retorte.intervalmusiccompositor.commons.platform;

import java.io.File;

/**
 * Access to information of the Mac OSX platform.
 */
public class MacOsxPlatform extends Platform {

    MacOsxPlatform(String osName) {
        super(osName);
    }

    @Override
    public String getOsIdentifier() {
        return "mac";
    }

    @Override
    public String getDesktopPath() {
        return System.getProperty("user.home") + "/Desktop/";
    }

    @Override
    public String getHomeDirectoryPath() {
        return System.getProperty("user.home");
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
        return 255;
    }
}
