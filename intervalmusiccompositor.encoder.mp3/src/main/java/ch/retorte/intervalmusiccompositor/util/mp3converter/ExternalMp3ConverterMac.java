package ch.retorte.intervalmusiccompositor.util.mp3converter;

import java.io.File;

/**
 * @author nw
 */
public class ExternalMp3ConverterMac extends ExternalMp3Converter {

	@Override
	protected File getSystemBinary() {
		return new File("/usr/local/bin/lame");
	}

	@Override
	protected File getLocalBinary() {
		return new File("./lame");
	}

}
