package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.update.UpdateAvailabilityCheckerException;
import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Contacts home server for text file containing current version.
 */
public class UpdateChecker implements UpdateAvailabilityChecker {

    //---- Fields

    private final MessageFormatBundle bundle = new CoreBundleProvider().getBundle();
    private final Platform platform = new PlatformFactory().getPlatform();
    private final MessageProducer messageProducer;
    private final ApplicationData applicationData;

    private Version remoteVersion;


    //---- Constructor

    public UpdateChecker(ApplicationData applicationData, MessageProducer messageProducer) {
        this.applicationData = applicationData;
        this.messageProducer = messageProducer;
    }


    //---- Methods

    private Version getRemoteVersion(URL url) throws IOException {
        addDebugMessage("Attempting to fetch remote version on URL: " + url);
        addDebugMessage("Setting user agent to: " + compileUserAgentString());

        String versionString;
        BufferedReader in = null;
        try {
            URLConnection urlc = url.openConnection();
            urlc.setRequestProperty("User-Agent", compileUserAgentString());

            in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

            versionString = in.readLine();
            in.close();

            addDebugMessage("Fetched remote version string: " + versionString);
        } catch (IOException e) {
            addDebugMessage(e);
            throw new IOException("Not able to contact home server!");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // nop
                }
            }
        }

        return new Version(versionString);
    }

    private String compileUserAgentString() {
        return bundle.getString("web.userAgent.update") + " (" //
            + "v: " + applicationData.getProgramVersion() + ", " //
            + platform.getSystemDiagnosisString() + ", " //
            + "locale: " + bundle.getLocale() + ")";
    }

    @Override
    public boolean isUpdateAvailable() {
        Version currentVersion = applicationData.getProgramVersion();

        try {
            remoteVersion = getRemoteVersion(new URI(bundle.getString("web.current_version.url")).toURL());
        } catch (Exception e) {
            addDebugMessage("Failed to get remote version: " + e.getMessage());
            throw new UpdateAvailabilityCheckerException();
        }

        return currentVersion.compareTo(remoteVersion) < 0;
    }

    @Override
    public Version getLatestVersion() {
        if (remoteVersion == null) {
            isUpdateAvailable();
        }

        return remoteVersion;
    }

    private void addDebugMessage(String message) {
        messageProducer.send(new DebugMessage(this, message));
    }

    private void addDebugMessage(Throwable throwable) {
        messageProducer.send(new DebugMessage(this, throwable));
    }

}
