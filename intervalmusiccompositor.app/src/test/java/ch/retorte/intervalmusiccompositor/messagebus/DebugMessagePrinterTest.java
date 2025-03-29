package ch.retorte.intervalmusiccompositor.messagebus;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@Disabled
public class DebugMessagePrinterTest {

  private DebugMessagePrinter handler;
  private final LocalDateTime currentDate = LocalDateTime.now();

  @BeforeEach
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
