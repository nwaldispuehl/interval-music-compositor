package ch.retorte.intervalmusiccompositor.messagebus;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.InfoMessage;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
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
