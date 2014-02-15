package ch.retorte.intervalmusiccompositor.util.mp3converter;

import java.io.File;

/**
 * @author nw
 */
public class ExternalMp3ConverterWindows extends ExternalMp3Converter {

	@Override
	protected File getSystemBinary() {
		/* We don't know where libraries are installed on the windows platform... */
    return new File("c:\\Program Files\\Lame\\lame.exe");
	}

	@Override
	protected File getLocalBinary() {
		return new File(".\\lame.exe");
	}

}
