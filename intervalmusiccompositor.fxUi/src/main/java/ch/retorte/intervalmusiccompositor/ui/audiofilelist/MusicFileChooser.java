package ch.retorte.intervalmusiccompositor.ui.audiofilelist;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Displays a file chooser with filter targeting only supported music files.
 */
public class MusicFileChooser {

  private MessageProducer messageProducer;
  private MessageFormatBundle messageFormatBundle;
  private List<AudioFileDecoder> audioFileDecoders;

  public MusicFileChooser(MessageProducer messageProducer, MessageFormatBundle messageFormatBundle, List<AudioFileDecoder> audioFileDecoders) {
    this.messageProducer = messageProducer;
    this.messageFormatBundle = messageFormatBundle;
    this.audioFileDecoders = audioFileDecoders;
  }

  public List<File> chooseFileIn(Window window) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(messageFormatBundle.getString("ui.form.file_chooser.title"));

    FileChooser.ExtensionFilter allFilesExtensionFilter = new FileChooser.ExtensionFilter(messageFormatBundle.getString("ui.form.file_chooser.filter.all_files"), "*");
    FileChooser.ExtensionFilter musicFileExtensionFilter = new FileChooser.ExtensionFilter(messageFormatBundle.getString("ui.form.file_chooser.filter.music_files"), createMusicFileExtensionList());

    chooser.getExtensionFilters().addAll(musicFileExtensionFilter, allFilesExtensionFilter);
    chooser.setSelectedExtensionFilter(musicFileExtensionFilter);

    messageProducer.send(new DebugMessage(this, "Opening file dialog with filter: " + musicFileExtensionFilter.getExtensions()));
    List<File> result = chooser.showOpenMultipleDialog(window);

    if (result == null) {
      messageProducer.send(new DebugMessage(this, "Aborted."));
      return newArrayList();
    }
    else {
      messageProducer.send(new DebugMessage(this, "Chose files: " + result));
      return result;
    }
  }

  private List<String> createMusicFileExtensionList() {
    List<String> result = newArrayList();
    for (AudioFileDecoder d : audioFileDecoders) {
      result.addAll(d.getExtensions().stream().map(extension -> "*." + extension).collect(Collectors.toList()));
    }
    return result;
  }

}
