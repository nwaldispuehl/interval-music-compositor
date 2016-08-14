package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.output.OutputGenerator;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link CompilationGenerator}.
 */
public class CompilationGeneratorTest {

  private Compilation compilation = mock(Compilation.class);
  private OutputGenerator outputGenerator = mock(OutputGenerator.class);
  private MessageProducer messageProducer = mock(MessageProducer.class);
  private CompilationGenerator sut = new CompilationGenerator(compilation, outputGenerator, messageProducer);

  @Test
  public void shouldCreateOutputFilePrefixWithEmptyBreak() {
    // given
    List<Integer> musicPattern = l(10);
    List<Integer> breakPattern = l();
    int iterations = 12;

    // when
    String outputPrefix = sut.createMusicAndBreakPatternPrefixWith(musicPattern, breakPattern, iterations);

    // then
    assertThat(outputPrefix, is("10s-x12"));
  }

  @Test
  public void shouldCreateSimpleOutputFilePrefix() {
    // given
    List<Integer> musicPattern = l(40);
    List<Integer> breakPattern = l(20);
    int iterations = 24;

    // when
    String outputPrefix = sut.createMusicAndBreakPatternPrefixWith(musicPattern, breakPattern, iterations);

    // then
    assertThat(outputPrefix, is("40s_20b-x24"));
  }

  @Test
  public void shouldCreateOutputFilePrefixWithZeroBreaks() {
    // given
    List<Integer> musicPattern = l(10, 20, 30);
    List<Integer> breakPattern = l(5, 0, 8);
    int iterations = 18;

    // when
    String outputPrefix = sut.createMusicAndBreakPatternPrefixWith(musicPattern, breakPattern, iterations);

    // then
    assertThat(outputPrefix, is("10s_5b_20s_0b_30s_8b-x18"));
  }

  @Test
  public void shouldCreateMoreElaborateOutputFilePrefix() {
    // given
    List<Integer> musicPattern = l(10, 20, 30);
    List<Integer> breakPattern = l(2, 4, 6);
    int iterations = 5;

    // when
    String outputPrefix = sut.createMusicAndBreakPatternPrefixWith(musicPattern, breakPattern, iterations);

    // then
    assertThat(outputPrefix, is("10s_2b_20s_4b_30s_6b-x5"));
  }

  private List<Integer> l(Integer... s) {
    return Arrays.asList(s);
  }
}
