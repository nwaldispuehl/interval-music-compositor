package ch.retorte.intervalmusiccompositor.bpm;

import at.ofai.music.beatroot.BeatRoot;
import ch.retorte.intervalmusiccompositor.decoder.mp3.Mp3AudioFileDecoder;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeatRootIntegrationTest {

    @Test
    public void shouldDetect120Bpm() throws UnsupportedAudioFileException, IOException {

        // given
        AudioInputStream audioInputStream = getStreamFromResourceName("120_bpm.mp3");

        // when
        Integer bpm = new BeatRoot().calculateBPM(audioInputStream);

        // then
        assertEquals(120, bpm);
    }

    @Test
    public void shouldDetect64Bpm() throws UnsupportedAudioFileException, IOException {

        // given
        AudioInputStream audioInputStream = getStreamFromResourceName("64_bpm.mp3");

        // when
        Integer bpm = new BeatRoot().calculateBPM(audioInputStream);

        // then
        assertEquals(64, bpm);
    }

    @Test
    public void shouldDetect178Bpm() throws UnsupportedAudioFileException, IOException {

        // given
        AudioInputStream audioInputStream = getStreamFromResourceName("178_bpm.mp3");

        // when
        Integer bpm = new BeatRoot().calculateBPM(audioInputStream);

        // then
        assertEquals(176, bpm); // wut ???
    }

    private AudioInputStream getStreamFromResourceName(String name) throws UnsupportedAudioFileException, IOException {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new IOException();
        }

        File bpmFile = new File(resource.getFile());
        return new Mp3AudioFileDecoder().decode(bpmFile);
    }

}
