package ch.retorte.intervalmusiccompositor.util.mp3converter;

import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformNotFoundException;

/**
 * @author nw
 */
public class ExternalMp3ConverterFactory {

	public ExternalMp3Converter createConverter() {
		
    Platform platform = new PlatformFactory().getPlatform();
		
		if (platform.isLinux()) {
			return new ExternalMp3ConverterLinux();
		}
		else if (platform.isMac()) {
			return new ExternalMp3ConverterMac();
		}
		else if (platform.isWindows()) {
			return new ExternalMp3ConverterWindows();
		}
		
		throw new PlatformNotFoundException("Unknown platform: " + platform.getOSName());
	}
	
}
