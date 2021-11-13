package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.model.messagebus.StringMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
        String downloadUrl = "https://github.com/nwaldispuehl/interval-music-compositor/releases/download/v2.11.1/IntervalMusicCompositor-linux-2.11.1.zip";
        File upgradeDirectory = File.createTempFile("imc.test_", "tmp").getParentFile();
        ProgressListener progressListener = stdOutProgressListener();

        // when
        File download = upgrader.downloadToUpgradeDir(upgradeDirectory, downloadUrl, progressListener);

        // then
        assertTrue(download.exists());
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
