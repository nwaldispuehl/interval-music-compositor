package ch.retorte.intervalmusiccompositor.messagebus;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DebugMessagePrinterTest {

  private DebugMessagePrinter handler;
  private LocalDateTime currentDate = LocalDateTime.now();

  @Before
  public void setup() {
    handler = spy(new DebugMessagePrinter());
  }

  @Test
  public void shouldWriteDebugMessagesToStdOut() {
    // given
    DebugMessage message = mock(DebugMessage.class);
    doReturn(currentDate).when(message).getDate();

    // when
    handler.handle(message);

    // then
    verify(handler, times(1)).printToStdOut(anyString());
  }

  @Test
  public void shouldNicelyFormatDebugMessage() {
    // given
    DebugMessage message = new DebugMessage("SomeClassName", "SomeMessage", currentDate);

    // when
    handler.handle(message);

    // then
    verify(handler, times(1)).printToStdOut(formattedDate() + " - SomeClassName - SomeMessage");
  }

  private String formattedDate() {
    return DebugMessage.format(currentDate);
  }

}
