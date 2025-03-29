package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the {@link UpdateChecker}.
 */
public class UpdateCheckerTest {

    //---- Fields

    UpdateChecker updateChecker;
    ApplicationData applicationData = mock(ApplicationData.class);
    MessageProducer messageProducer = mock(MessageProducer.class);


    //---- Methods

    @BeforeEach
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
