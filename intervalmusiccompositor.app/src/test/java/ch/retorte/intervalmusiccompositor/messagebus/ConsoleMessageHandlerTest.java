package ch.retorte.intervalmusiccompositor.messagebus;

import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.InfoMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@Disabled
public class ConsoleMessageHandlerTest {


  @Test
  public void shouldWriteInfoMessagesToStdOut() {
    // given
    ConsoleMessageHandler handler = spy(new ConsoleMessageHandler());

    // when
    handler.handle(mock(InfoMessage.class));

    // then
    verify(handler, times(1)).printToStdOut(anyString());
  }

  @Test
  public void shouldWriteErrorMessagesToStdErr() {
    // given
    ConsoleMessageHandler handler = spy(new ConsoleMessageHandler());

    // when
    handler.handle(mock(ErrorMessage.class));

    // then
    verify(handler, times(1)).printToStdErr(anyString());
  }
}
