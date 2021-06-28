package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.audiofile.AudioFileFactory;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author nw
 */
@Ignore // TODO: Fix
public class AudioFilesLoaderTest {

    private static final String OWN_FILE_INFIX = "XXX";

    private MusicListControl musicListControl;

    private AudioFilesLoader loader;

    @Before
    public void setup() {
        musicListControl = mock(MusicListControl.class);
        AudioFileFactory audioFileFactory = mock(AudioFileFactory.class);
        MessageProducer messageProducer = mock(MessageProducer.class);

        loader = spy(new AudioFilesLoader(musicListControl, audioFileFactory, messageProducer));

        doReturn(OWN_FILE_INFIX).when(loader).getOwnFileInfix();
        doReturn(true).when(audioFileFactory).hasDecoderFor(any(File.class));
    }

    @Test
    public void shouldRecognizeSelfProducedFiles() {
        // given
        File file1 = new File("abcd" + OWN_FILE_INFIX + "asd");
        File file2 = new File("acegikm");

        // when / then
        assertFalse(loader.isNotSelfProduced(file1));
        assertTrue(loader.isNotSelfProduced(file2));
    }

    @Test
    public void shouldKeepKnownNewSoundFiles() {
        // given
        File file1 = new File("a");
        File file2 = new File("b");
        File ownFile = new File(OWN_FILE_INFIX);

        // when
        List<File> knownSoundFiles = loader.keepKnownNewSoundFilesFrom(newArrayList(file1, file2, ownFile));

        // then
        assertThat(knownSoundFiles.size(), is(2));
        assertTrue(knownSoundFiles.contains(file1));
        assertTrue(knownSoundFiles.contains(file2));
        assertFalse(knownSoundFiles.contains(ownFile));
    }

    @Test
    public void shouldLoadAudioFiles() {
        // given
        File file1 = new File("a");
        File file2 = new File("b");
        File file3 = new File("c");
        File ownFile = new File(OWN_FILE_INFIX);
        doReturn(newArrayList(file1, file2, file3, ownFile)).when(loader).getAllFilesFromCurrentDirectory();

        // when
        loader.loadAudioFiles();

        // then
        verify(musicListControl, times(1)).addMusicTrack(0, file3);
        verify(musicListControl, times(1)).addMusicTrack(0, file2);
        verify(musicListControl, times(1)).addMusicTrack(0, file1);
    }
}
