package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.model.messagebus.StringMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link ModuleReplacingVersionUpgrader}.
 */
public class ModuleReplacingVersionUpgraderTest {

    //---- Fields

    private ModuleReplacingVersionUpgrader upgrader;

    private final Platform platform = new PlatformFactory().getPlatform();
    private final MessageProducer messageProducer = stdOutMessageProducer();


    //---- Test methods

    @Before
    public void setup() {
        upgrader = new ModuleReplacingVersionUpgrader(platform, messageProducer);
    }

    @Test
    public void shouldDownloadNewReleaseFile() throws IOException {
        // given
        final String downloadUrl = "https://github.com/nwaldispuehl/interval-music-compositor/releases/download/v2.11.1/IntervalMusicCompositor-linux-2.11.1.zip";
        final File upgradeDirectory = File.createTempFile("imc.test_", "tmp").getParentFile();
        final ProgressListener progressListener = stdOutProgressListener();

        // when
        final File download = upgrader.downloadToUpgradeDir(upgradeDirectory, downloadUrl, progressListener);
        download.deleteOnExit();

        // then
        assertTrue(download.exists());
    }

    @Test
    public void shouldRetainFilePermissionsWhenUnzippingFile() throws URISyntaxException {
        // given
        final File zipArchive = loadResource("file-permissions-test.zip");
        final File destination = new File("/tmp/zip-test");
        destination.mkdirs();
        destination.deleteOnExit();
        final ProgressListener progressListener = stdOutProgressListener();

        final File executableFile = new File("/tmp/zip-test/my-executable.sh");
        executableFile.deleteOnExit();
        final File nonExecutableFile = new File("/tmp/zip-test/my-non-executable.txt");
        nonExecutableFile.deleteOnExit();

        final Set<String> makeExecutablePattern = Set.of("my-executable.sh");

        // when
        upgrader.unzipFiles(zipArchive, destination, makeExecutablePattern, progressListener);

        // then
        assertTrue(executableFile.exists());
        assertTrue(nonExecutableFile.exists());
        assertTrue(executableFile.canExecute());
        assertFalse(nonExecutableFile.canExecute());
    }

    private File loadResource(String filename) throws URISyntaxException {
        return new File(getClass().getClassLoader().getResource(filename).toURI());
    }


    //---- Helper methods

    private ProgressListener stdOutProgressListener() {
        return new ProgressListener() {
            @Override
            public void onProgressUpdate(String progress) {
                System.out.println(progress);
            }
        };
    }

    private MessageProducer stdOutMessageProducer() {
        return message -> {
            if (message instanceof StringMessage m) {
                System.out.println(m.getMessage());
            }
        };
    }
}
