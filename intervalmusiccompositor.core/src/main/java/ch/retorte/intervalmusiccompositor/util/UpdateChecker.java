package ch.retorte.intervalmusiccompositor.util;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityCheckerException;

/**
 * Contacts home server for text file containing current version.
 * 
 * @author nw
 */
public class UpdateChecker implements UpdateAvailabilityChecker {

  private MessageFormatBundle bundle = getBundle("core_imc");

  private Version remoteVersion;

  private MessageProducer messageProducer;

  private Platform platform = new PlatformFactory().getPlatform();

  private ApplicationData applicationData;

  public UpdateChecker(ApplicationData applicationData, MessageProducer messageProducer) {
    this.applicationData = applicationData;
    this.messageProducer = messageProducer;
  }

  private Version getRemoteVersion(URL url) throws IOException {
    addDebugMessage("Attempting to fetch remote version on URL: " + url);

    String versionString = "";
    BufferedReader in = null;
    try {
      URLConnection urlc = url.openConnection();
      urlc.setRequestProperty("User-Agent", compileUserAgentString());

      in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

      versionString = in.readLine();
      in.close();

      addDebugMessage("Fetched remote version string: " + versionString);
    }
    catch (IOException e) {
      throw new IOException("Not able to contact home server!");
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException e) {
          // nop
        }
      }
    }

    return new Version(versionString);
  }

  private String compileUserAgentString() {
    return bundle.getString("web.userAgent.update") + " (" //
        + applicationData.getProgramVersion() + ", " //
        + platform.getOSName() + ", " //
        + platform.getArchitecture() + ", " //
        + bundle.getLocale() + ")";
  }

  @Override
  public boolean isUpdateAvailable() {
    Version currentVersion = applicationData.getProgramVersion();

    try {
      remoteVersion = getRemoteVersion(new URL(bundle.getString("web.current_version.url")));
    }
    catch (Exception e) {
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

}
