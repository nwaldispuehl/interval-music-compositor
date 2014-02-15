package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author nw
 */
public class UpdateCheckerTest {

  UpdateChecker updateChecker;
  ApplicationData applicationData = mock(ApplicationData.class);
  MessageProducer messageProducer = mock(MessageProducer.class);

  @Before
  public void setup() {
    updateChecker = Mockito.spy(new UpdateChecker(applicationData, messageProducer));
  }

  @Test
  public void shouldFindOutIsUpdateAvailable() {
    // given
    Version v1 = new Version(5, 4, 3, 2);
    doReturn(v1).when(updateChecker).getLatestVersion();

    // when

    // then

  }


}
