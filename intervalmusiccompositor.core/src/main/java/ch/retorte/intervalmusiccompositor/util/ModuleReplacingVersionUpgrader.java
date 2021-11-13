package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import ch.retorte.intervalmusiccompositor.spi.update.VersionUpgrader;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.valueOf;

/**
 * Version upgrader which downloads the archive from GitHub and replaces the `modules` file in the `lib` directory.
 */
public class ModuleReplacingVersionUpgrader implements VersionUpgrader {

    //---- Constants

    private static final String TEMPORARY_UPGRADE_ARCHIVE = "upgrade.zip";

    private static final String MESSAGE_KEY_DOWNLOAD_URL_PATTERN = "upgrade.downloadUrl.pattern";

    private static final String MESSAGE_KEY_ERROR_DOWNLOAD_NOT_POSSIBLE = "upgrade.error.download_not_possible";

    private static final String MESSAGE_KEY_PROGRESS_DOWNLOAD = "upgrade.progress.download";
    private static final String MESSAGE_KEY_PROGRESS_EXTRACT = "upgrade.progress.extract";
    private static final String MESSAGE_KEY_PROGRESS_REPLACE_RESTART = "upgrade.progress.replace_and_restart";


    //---- Fields

    private final MessageFormatBundle coreBundle = new CoreBundleProvider().getBundle();

    private final Platform platform;
    private final MessageProducer messageProducer;


    //---- Constructor

    public ModuleReplacingVersionUpgrader(Platform platform, MessageProducer messageProducer) {
        this.platform = platform;
        this.messageProducer = messageProducer;
    }


    //---- Methods

    @Override
    public void upgradeTo(Version version, ProgressListener progressListener) {
        // Create upgrade directory
        File upgradeDirectory = platform.getUpgradePath();
        debug("Determined upgrade directory: " + upgradeDirectory.getAbsolutePath());
        if (upgradeDirectory.getParentFile().exists() && !upgradeDirectory.exists()) {
            debug("Upgrade directory does not exist. Creating...");
            boolean upgradeDirectoryCreated = upgradeDirectory.mkdirs();
            if (!upgradeDirectoryCreated) {
                throw new IllegalStateException("Not able to create upgrade directory.");
            }
            debug("Successfully created upgrade directory.");
        }

        // Download new version
        progressListener.onProgressUpdate(coreBundle.getString(MESSAGE_KEY_PROGRESS_DOWNLOAD));
        String downloadUrl = getDownloadUrlWith(version);
        debug("Acquired download URL: " + downloadUrl);
        File newVersionZipArchive = downloadToUpgradeDir(upgradeDirectory, downloadUrl, progressListener);
        newVersionZipArchive.deleteOnExit();
        debug("New version downloaded to: " + newVersionZipArchive.getAbsolutePath());

        // Extract new version
        progressListener.onProgressUpdate(coreBundle.getString(MESSAGE_KEY_PROGRESS_EXTRACT));
        unzipFiles(newVersionZipArchive, platform.getUpgradePath(), progressListener);

        // Installing new standalone updater
        progressListener.onProgressUpdate(coreBundle.getString(MESSAGE_KEY_PROGRESS_REPLACE_RESTART));

        try {
            platform.copyFromUpgradeDir(pathJoin(Platform.BINARY_DIRECTORY, platform.getUpgradeExecutableName()));
            if(!pathJoin(platform.getBinaryPath().getAbsolutePath(), platform.getUpgradeExecutableName()).toFile().setExecutable(true)) {
                progressListener.onProgressUpdate("Unable to execute upgrade software.");
                // And then?
            }
        }
        catch (IOException e) {
            messageProducer.send(new ErrorMessage(e));
            throw new RuntimeException("Unable to replace Standalone Updater.");
        }

        // 'Install' new version via standalone 'Updater'.
        try {
            final List<String> command = new ArrayList<>();
            command.add(platform.getBinaryPath().getAbsolutePath() + File.separator + platform.getUpgradeExecutableName());
            command.add(platform.getDistributionRootPath());
            command.add(valueOf(ProcessHandle.current().pid()));

            debug("Executing command: " + String.join(" ", command));

            new ProcessBuilder(command).start();
        }
        catch (Exception e) {
            messageProducer.send(new ErrorMessage(e));
            debug("Problem with executing runtime: " + e.getMessage());
            return;
        }

        debug("Quitting main app to start updater.");
        System.exit(0);
    }

    /**
     * Unzips new version archive into destination.
     *
     * Code taken from https://www.baeldung.com/java-compress-and-uncompress.
     */
    private void unzipFiles(File newVersionZipArchive, File destination, ProgressListener progressListener) {
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(newVersionZipArchive));
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = newFile(destination, zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
        catch (IOException e) {
            progressListener.onProgressUpdate(e.getMessage());
            throw new RuntimeException(coreBundle.getString(MESSAGE_KEY_ERROR_DOWNLOAD_NOT_POSSIBLE), e);
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private String getDownloadUrlWith(Version version) {
        return coreBundle.getString(MESSAGE_KEY_DOWNLOAD_URL_PATTERN, version, platform.getOsDownloadDescriptor());
    }

    /* Visible for testing. */
    File downloadToUpgradeDir(File upgradeDirectory, String downloadUrl, ProgressListener progressListener) {
        try {
            File upgradeArchive = new File(upgradeDirectory.getAbsolutePath() + File.separator + TEMPORARY_UPGRADE_ARCHIVE);
            debug("Download file: " + upgradeArchive.getAbsolutePath());
            if (upgradeArchive.exists()) {
                debug("Download file already exists; deleting first.");
                boolean deleted = upgradeArchive.delete();
                if (deleted) {
                    debug("Successfully deleted.");
                }
            }
            boolean created = upgradeArchive.createNewFile();
            if (created) {
                debug("Successfully created empty file.");
            }

            debug("Starting download.");
            URL documentUrl = new URL(downloadUrl);
            ReadableByteChannel rbc = Channels.newChannel(documentUrl.openStream());
            FileOutputStream fos = new FileOutputStream(upgradeArchive);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return upgradeArchive;
        }
        catch (IOException e) {
            progressListener.onProgressUpdate(e.getMessage());
            throw new RuntimeException(coreBundle.getString(MESSAGE_KEY_ERROR_DOWNLOAD_NOT_POSSIBLE), e);
        }
    }

    private Path pathJoin(String... path) {
        return new File(String.join(File.separator, path)).toPath();
    }

    private void debug(String message) {
        messageProducer.send(new DebugMessage(this, message));
    }
}
