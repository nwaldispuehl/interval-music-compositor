package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFile;
import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tritonus.sampled.file.WaveAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the actual compilation generator.
 */
public class CompilationTest {

  //---- Static

  private static final String TEST_SOUND = "/11_s_square.wav";


  //---- Fields

  private List<IAudioFile> musicFiles = newArrayList();
  private List<IAudioFile> breakFiles = newArrayList();

  private SoundHelper soundHelper = new SoundHelper(m -> {});
  private Compilation sut = new Compilation(soundHelper, m -> {});


  //---- Setup methods


  @Before
  public void setup() throws IOException, UnsupportedAudioFileException {
    musicFiles.add(audioFileFrom(TEST_SOUND));
  }

  @After
  public void cleanup() throws IOException {
    for (IAudioFile f : musicFiles) {
      f.removeCache();
    }
  }

  //---- Test methods

  @Test
  public void shouldGenerateCompilation() throws IOException, UnsupportedAudioFileException {
    // given
    Playlist playList = playlistWith(1, l(1), l());

    // when
    byte[] compilation = sut.generateCompilation(playList);

    // then
    // Somehow check byte array...
    assertThat(compilation.length, is(44100 * 2 * 2));
    assertThat(compilation[0], is(b(0)));
    assertThat(compilation[1], is(b(0)));
    assertThat(compilation[88198], is(b(101)));
    assertThat(compilation[88199], is(b(-103)));
    assertThat(compilation[176398], is(b(0)));
    assertThat(compilation[176399], is(b(0)));
  }


  //---- Helper methods

  private Playlist playlistWith(int iterations, List<Integer> musicIntervals, List<Integer> breakIntervals) {
    CompilationParameters parameters = new CompilationParameters();
    Playlist playlist = new Playlist(parameters, m -> {});
    playlist.generatePlaylist(musicFiles, musicIntervals, breakFiles, breakIntervals, iterations, newArrayList());
    return playlist;
  }

  private <T> List<T> l(T... items) {
    return newArrayList(items);
  }

  private byte b(int i) {
    return (byte) i;
  }

  private IAudioFile audioFileFrom(String resource) throws IOException, UnsupportedAudioFileException {
    URL resourceUrl = getClass().getResource(resource);
    AudioFile audioFile = new AudioFile(resourceUrl.getFile(), soundHelper, l(new WaveDecoder()), null, null, a -> a, m -> {});
    audioFile.createCache();
    return audioFile;
  }


  //---- Inner classes

  private class WaveDecoder implements AudioFileDecoder {

    @Override
    public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {
      return new WaveAudioFileReader().getAudioInputStream(inputFile);
    }

    @Override
    public boolean isAbleToDecode(File file) {
      return true;
    }

    @Override
    public Collection<String> getExtensions() {
      return l("wav");
    }
  }
}
