package ch.retorte.intervalmusiccompositor.ui.list;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;

/**
 * @author nw
 */
public class AudioFileTransferable implements Transferable {

  private DataFlavor[] supportedFlavors = { new DataFlavor(IAudioFile.class, "AudioFile") };
  private List<IAudioFile> audioFileList;
	
  public AudioFileTransferable(List<IAudioFile> audioFileList) {
		this.audioFileList = audioFileList;
	}
	
  public AudioFileTransferable(IAudioFile audioFile) {
    audioFileList = new ArrayList<IAudioFile>();
		audioFileList.add(audioFile);
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(supportedFlavors).contains(flavor);
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if(isDataFlavorSupported(flavor)) {
			return audioFileList;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
