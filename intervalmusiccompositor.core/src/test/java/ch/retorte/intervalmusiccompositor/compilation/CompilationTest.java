package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFile;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tritonus.sampled.file.WaveAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for the actual compilation generator.
 */
public class CompilationTest {

    //---- Static

    private static final String TEST_SOUND = "/11_s_square.wav";


    //---- Fields

    private final List<IAudioFile> musicFiles = newArrayList();
    private final List<IAudioFile> breakFiles = newArrayList();

    private final SoundHelper soundHelper = new SoundHelper(m -> {
    });
    private final Compilation sut = new Compilation(soundHelper, m -> {
    });


    //---- Setup methods

    @BeforeEach
    public void setup() throws IOException, UnsupportedAudioFileException {
        musicFiles.add(audioFileFrom(TEST_SOUND));
        breakFiles.add(audioFileFrom(TEST_SOUND));
    }

    @AfterEach
    public void cleanup() throws IOException {
        for (IAudioFile f : musicFiles) {
            f.removeCache();
        }
        for (IAudioFile f : breakFiles) {
            f.removeCache();
        }
    }

    //---- Test methods

    @Test
    public void shouldGenerateCompilation() throws IOException, UnsupportedAudioFileException {
        // given
        Playlist playList = playlistWith(1, l(1), l());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // when
        sut.generateCompilation(playList, baos);

        // then
        // Somehow check byte array...
        byte[] compilation = baos.toByteArray();
        assertEquals(44100 * 2 * 2, compilation.length);
        assertEquals(b(0), compilation[0]);
        assertEquals(b(0), compilation[1]);
        assertEquals(b(-20), compilation[88198]);
        assertEquals(b(-63), compilation[88199]);
        assertEquals(b(0), compilation[176398]);
        assertEquals(b(0), compilation[176399]);
    }

    @Test
    public void shouldGenerateLongerCompilation() throws IOException, UnsupportedAudioFileException {
        // given
        Playlist playList = playlistWith(6, l(1), l());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // when
        sut.generateCompilation(playList, baos);

        // then
        // Somehow check byte array...
        byte[] compilation = baos.toByteArray();
        assertEquals(44100 * 2 * 12, compilation.length);
    }

    @Test
    public void shouldSetBreakVolume() throws IOException, UnsupportedAudioFileException {
        // given
        Playlist playList = playlistWith(1, l(1), l(1), 0.5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // when
        sut.generateCompilation(playList, baos);

        // then
        // Somehow check byte array...
        byte[] compilation = baos.toByteArray();
        assertEquals(44100 * 2 * 2 * 2, compilation.length);

        // Sound part
        assertEquals(b(0), compilation[0]);
        assertEquals(b(0), compilation[1]);
        assertEquals(b(-20), compilation[88198]);
        assertEquals(b(-63), compilation[88199]);
        assertEquals(b(0), compilation[176398]);
        assertEquals(b(0), compilation[176399]);

        // Break part
        assertEquals(b(0), compilation[176400]);
        assertEquals(b(0), compilation[176401]);
        assertEquals(b(-10), compilation[264598]);
        assertEquals(b(-32), compilation[264599]);
        assertEquals(b(0), compilation[352798]);
        assertEquals(b(0), compilation[352798]);

    }


    //---- Helper methods

    private Playlist playlistWith(int iterations, List<Integer> musicIntervals, List<Integer> breakIntervals) {
        return playlistWith(iterations, musicIntervals, breakIntervals, 1);
    }

    private Playlist playlistWith(int iterations, List<Integer> musicIntervals, List<Integer> breakIntervals, double breakVolume) {
        CompilationParameters parameters = new CompilationParameters();
        Playlist playlist = new Playlist(parameters, m -> {
        });
        playlist.generatePlaylist(musicFiles, musicIntervals, breakFiles, breakIntervals, breakVolume, iterations, newArrayList());
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
        AudioFile audioFile = new AudioFile(resourceUrl.getFile(), soundHelper, l(new WaveDecoder()), null, null, a -> a, m -> {
        });
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
