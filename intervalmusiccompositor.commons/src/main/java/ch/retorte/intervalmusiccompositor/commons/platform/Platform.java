package ch.retorte.intervalmusiccompositor.commons.platform;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Central access point for information about the underlying platform.
 */
public abstract class Platform {

    //---- Constants

    public static final String BINARY_DIRECTORY = "bin";
    public static final String LIBRARY_DIRECTORY = "lib";
    public static final String LOG_DIRECTORY = "log";
    public static final String UPGRADE_DIRECTORY = "upgrade";

    protected static final String ARCHIVE_NAME = "IntervalMusicCompositor";

    protected static final String MODULE_PATH_SUFFIX = "/../app";




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

    /**
     * Name as used to specify the OS in the download files.
     */
    public abstract String getOsDownloadDescriptor();


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
        return beautify(((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize());
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
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
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

    protected String getUserDir() {
        return System.getProperty("user.dir");
    }

    protected String getEffectiveModulePath() {
        String modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null && MODULE_PATH_SUFFIX.length() < modulePath.length()) {
            return modulePath.substring(0, modulePath.length() - MODULE_PATH_SUFFIX.length());
        }
        return "";
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

    /**
     * Returns the absolute path to the binary path of the program distribution.
     */
    protected abstract String getProgramDistributionRawBinPath();

    /**
     * Returns a file pointing to the base path of the runtime. The assumption is that the program resides in the jlink directory structure:
     * <pre>
     *     distribution/
     *       bin/
     *       lib/
     *       ...
     * </pre>
     */
    public String getDistributionRootPath() {
        return getDistributionRootPathWith(getProgramDistributionRawBinPath());
    }

    /* Visible for testing. */
    String getDistributionRootPathWith(String binaryPath) {
        // If multiple paths are present, we use the first.
        if (binaryPath.contains(File.pathSeparator)) {
            binaryPath = binaryPath.split(File.pathSeparator)[0];
        }

        // Normalize
        String normalizedBinaryPath = Path.of(binaryPath).normalize().toString();

        // Eliminate any direct file references
        File rawBinaryPath = new File(normalizedBinaryPath);
        if (rawBinaryPath.isFile()) {
            rawBinaryPath = rawBinaryPath.getParentFile();
        }

        File rawRootPath = rawBinaryPath.getParentFile();
        return rawRootPath.getAbsolutePath();
    }

    /**
     * Returns a file pointing to the library path of the runtime.
     */
    public File getLibraryPath() {
        return getSubdirectoryFromDistributionRoot(LIBRARY_DIRECTORY);
    }

    /**
     * Returns a file pointing to the library path based on an arbitrary base path.
     */
    public File getLibraryPathFrom(String distributionRoot) {
        return getSubdirectoryFrom(distributionRoot, LIBRARY_DIRECTORY);
    }

    /**
     * Returns a file pointing to the binary path of the runtime.
     */
    public File getBinaryPath() {
        return getSubdirectoryFromDistributionRoot(BINARY_DIRECTORY);
    }

    /**
     * Returns a file pointing to the binary path based on an arbitrary base path.
     */
    public File getBinaryPathFrom(String distributionRoot) {
        return getSubdirectoryFrom(distributionRoot, BINARY_DIRECTORY);
    }

    /**
     * Returns a file pointing to the upgrade path of the runtime.
     */
    public File getUpgradePath() {
        return getSubdirectoryFromDistributionRoot(UPGRADE_DIRECTORY);
    }

    /**
     * Returns a file pointing to the upgrade path based on an arbitrary base path.
     */
    public File getUpgradePathFrom(String distributionRoot) {
        return getSubdirectoryFrom(distributionRoot, UPGRADE_DIRECTORY);
    }

    /**
     * Returns a file pointing to the log path of the runtime.
     */
    public File getLogPath() {
        return getSubdirectoryFromDistributionRoot(LOG_DIRECTORY);
    }

    /**
     * Returns a file pointing to the log path based on an arbitrary base path.
     */
    public File getLogPathFrom(String distributionRoot) {
        return getSubdirectoryFrom(distributionRoot, LOG_DIRECTORY);
    }

    private File getSubdirectoryFromDistributionRoot(String subdirectory) {
        String basePath = getDistributionRootPath();
        return getSubdirectoryFrom(basePath, subdirectory);
    }

    private File getSubdirectoryFrom(String basePath, String subdirectory) {
        return new File(basePath + File.separator + subdirectory);
    }

    public void copyFromUpgradeDir(Path path) throws IOException {
        copyFromUpgradeDirWith(getDistributionRootPath(), path);
    }

    public void copyFromUpgradeDirWith(String basePath, Path path) throws IOException {
        Files.copy(
            pathJoin(getUpgradePathFrom(basePath).getAbsolutePath(), ARCHIVE_NAME, path.toString()),
            pathJoin(basePath, path.toString()),
            StandardCopyOption.REPLACE_EXISTING
        );
    }

    private Path pathJoin(String... path) {
        return new File(String.join(File.separator, path)).toPath();
    }

    protected abstract String getProgramExecutableName();

    public abstract String getUpgradeExecutableName();

    /**
     * Produces a file to the executable which starts the program.
     */
    public File getProgramExecutablePath() {
        return getProgramExecutablePathFrom(getDistributionRootPath());
    }

    /**
     * Produces a file to the executable which starts the program based on the provided distribution root directory.
     */
    public File getProgramExecutablePathFrom(String distributionRoot) {
        return new File(getBinaryPathFrom(distributionRoot).getAbsolutePath() + File.separator + getProgramExecutableName());
    }

}
