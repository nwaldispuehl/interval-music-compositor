package ch.retorte.intervalmusiccompositor.messagebus;

import static ch.retorte.intervalmusiccompositor.messagebus.DebugMessageHandler.DATE_FORMAT;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DebugMessageHandlerTest {

  private DebugMessageHandler handler;
  private Date currentDate = new Date();

  @Before
  public void setup() {
    handler = Mockito.spy(new DebugMessageHandler());
  }

  @Test
  public void shouldWriteDebugMessagesToStdOut() {
    // given
    DebugMessage message = mock(DebugMessage.class);
    doReturn(mock(Date.class)).when(message).getDate();

    // when
    handler.handle(message);

    // then
    verify(handler, times(1)).printToStdOut(anyString());
  }

  @Test
  public void shouldNicelyFormatDebugMessage() {
    // given
    DebugMessage message = mock(DebugMessage.class);
    doReturn(currentDate).when(message).getDate();
    doReturn("SomeClassName").when(message).getCallerClassName();
    doReturn("SomeMessage").when(message).getMessage();

    // when
    handler.handle(message);

    // then
    verify(handler, times(1)).printToStdOut(formattedDate() + " - SomeClassName - SomeMessage");
  }

  private String formattedDate() {
    return new SimpleDateFormat(DATE_FORMAT).format(currentDate);
  }

}
