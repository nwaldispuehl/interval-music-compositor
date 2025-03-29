package ch.retorte.intervalmusiccompositor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link SoundHelper}.
 */
public class SoundHelperTest {

    // ---- Fields

    private final SoundHelper soundHelper = new SoundHelper(message -> {});


    // ---- Methods

    /**
     * With our `TARGET_AUDIO_FORMAT` (sample_rate = 41.1K, frame_size = 4) it should yield:
     * <pre>
     *     samples = extract_duration_in_s * sample_rate * frame_size
     *
     *     e.g. for a one second extract:
     *
     *     1 * 44'100 * 4 = 176'400
     * </pre>
     */
    @Test
    public void shouldGetSamplesFromMilliseconds() {
        assertEquals(0, soundHelper.getSamplesFromMilliseconds(0));
        assertEquals(176360, soundHelper.getSamplesFromMilliseconds(999.7732543945312));
        assertEquals(176400, soundHelper.getSamplesFromMilliseconds(1000));
        assertEquals(1764000, soundHelper.getSamplesFromMilliseconds(10000));
        assertEquals(17640000, soundHelper.getSamplesFromMilliseconds(100000));
        assertEquals(176400000, soundHelper.getSamplesFromMilliseconds(1000000));
        assertEquals(1764000000, soundHelper.getSamplesFromMilliseconds(10000000));
    }

}
