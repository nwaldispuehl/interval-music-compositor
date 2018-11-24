package ch.retorte.intervalmusiccompositor.audiofile;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Collection;
import java.util.List;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMCalculator;
import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * @author nw
 */
public class AudioFileFactory {

  private final Collection<AudioFileDecoder> decoders;
  private final Collection<BPMReaderWriter> bpmReaderWriters;
  private SoundHelper soundHelper;
  private BPMCalculator bpmCalculator;
  private AudioStandardizer audioStandardizer;
  private MessageProducer messageProducer;

  public AudioFileFactory(SoundHelper soundHelper, Collection<AudioFileDecoder> decoders, Collection<BPMReaderWriter> bpmReaderWriters, BPMCalculator bpmCalculator, AudioStandardizer audioStandardizer, MessageProducer messageProducer) {
    this.soundHelper = soundHelper;
    this.decoders = decoders;
    this.bpmReaderWriters = bpmReaderWriters;
    this.bpmCalculator = bpmCalculator;
    this.audioStandardizer = audioStandardizer;
    this.messageProducer = messageProducer;
  }

  public AudioFile createAudioFileFrom(File file) {
    List<AudioFileDecoder> audioFileDecoders = findMatchingDecodersFor(file);
    BPMReaderWriter bpmReaderWriter = findMatchingBPMReaderWriterFor(file);

    if (!audioFileDecoders.isEmpty()) {
      addDebugMessage("Selected " + audioFileDecoders.get(0).getClass().getSimpleName() + " for decoding " + file);
    }
    else {
      addDebugMessage("No AudioFileDecoder found for decoding " + file);
    }

    return new AudioFile(file.getAbsolutePath(), soundHelper, audioFileDecoders, bpmReaderWriter, bpmCalculator, audioStandardizer, messageProducer);
  }

  public boolean hasDecoderFor(File file) {
    List<AudioFileDecoder> matchingDecoders = findMatchingDecodersFor(file);
    return !matchingDecoders.isEmpty();
  }

  private List<AudioFileDecoder> findMatchingDecodersFor(File file) {
    List<AudioFileDecoder> result = newArrayList();
    for (AudioFileDecoder decoder : decoders) {
      if (decoder.isAbleToDecode(file)) {
        result.add(decoder);
      }
    }
    return result;
  }

  private BPMReaderWriter findMatchingBPMReaderWriterFor(File file) {
    for (BPMReaderWriter readerWriter : bpmReaderWriters) {
      if (readerWriter.isAbleToReadWriteBPM(file)) {
        return readerWriter;
      }
    }
    return null;
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  public Collection<AudioFileDecoder> getDecoders() {
    return decoders;
  }
}
