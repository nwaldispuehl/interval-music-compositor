package ch.retorte.intervalmusiccompositor.ui.about;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.net.URI;

import javax.swing.JLabel;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.ui.SwingUserInterface;
import ch.retorte.intervalmusiccompositor.update.UpdateAvailabilityCheckerException;

/**
 * @author nw
 */
public class AboutDialogControl {

  private MessageFormatBundle bundle = getBundle("ui_imc");

  private AboutDialog aboutDialog = null;

  private SwingUserInterface ui;
  private UpdateAvailabilityChecker updateAvailabilityChecker;
  private MessageProducer messageProducer;

  private ApplicationData applicationData;

  public AboutDialogControl(SwingUserInterface ui, UpdateAvailabilityChecker updateAvailabilityChecker, ApplicationData applicationData,
      MessageProducer messageProducer) {
    this.ui = ui;
    this.updateAvailabilityChecker = updateAvailabilityChecker;
    this.applicationData = applicationData;
    this.messageProducer = messageProducer;
  }
	
	public void startGui() {
		
		if(aboutDialog == null) {
      aboutDialog = new AboutDialog(ui, applicationData, this);
			aboutDialog.setAlwaysOnTop(true);
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	        		aboutDialog.setVisible(true);    	
	            }
	        });
		} else {
			aboutDialog.toFront();
			aboutDialog.requestFocus();
		}
	}
	
	
	public void close() {
    aboutDialog.setVisible(false);
		aboutDialog.dispose();
		aboutDialog = null;
	}
	
	public void checkForUpdates(final JLabel label, final Container host) {

		// Create new thread which checks for update and writes result into label
		new Thread(new Runnable() {
			
			@Override
			public void run() {
        host.setCursor(new Cursor(Cursor.WAIT_CURSOR));

				String message = "";
				
				try {
          if (updateAvailabilityChecker.isUpdateAvailable()) {
            message = bundle.getString("ui.about.update.message.new", updateAvailabilityChecker.getLatestVersion());
					} else {
            message = bundle.getString("ui.about.update.message.latest");
					}
        } catch (UpdateAvailabilityCheckerException e) {
          message = bundle.getString("ui.about.update.message.problem");
				}
				
				label.setText(message);
        host.setCursor(ui.getCursor());
				
				aboutDialog.pack();
			}
		}).start();
		
	}

  public void openWebsite() {
    if (isLinkClickable()) {
      try {
        Desktop.getDesktop().browse(new URI(bundle.getString("ui.about.website.url")));
      }
      catch (Exception e) {
        messageProducer.send(new DebugMessage(this, e.getMessage()));
      }
    }
  }

  public boolean isLinkClickable() {
    return Desktop.isDesktopSupported();
  }

}
