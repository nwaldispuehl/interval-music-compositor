package ch.retorte.intervalmusiccompositor.messagebus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;

/**
 * Test for the {@link MessageBus}.
 * 
 * @author nw
 */
public class MessageBusTest {

  private MessageBus messageBus;

  @Before
  public void setup() {
    messageBus = new MessageBus();
  }

  @Test
  public void shouldIgnoreMessagesIfNoHandlerExists() {
    // given
    Message message = mock(Message.class);

    // when
    messageBus.send(message);

    // then nothing happens
  }

  @Test
  public void shouldIgnoreMessagesIfWrongHandlerExists() {
    // given
    MatchingMessageHandler handler = new MatchingMessageHandler();
    messageBus.addHandler(handler);

    // when
    messageBus.send(mock(NonMatchingMessage.class));

    // then
    assertFalse(handler.hasBeenCalled());
  }

  @Test
  public void shouldUseMatchingHandlerForMessages() {
    // given
    MatchingMessageHandler handler = new MatchingMessageHandler();
    NonMatchingMessageHandler nonMatchingHandler = new NonMatchingMessageHandler();
    messageBus.addHandler(handler);
    messageBus.addHandler(nonMatchingHandler);

    // when
    messageBus.send(mock(MatchingMessage.class));

    // then
    assertTrue(handler.hasBeenCalled());
    assertFalse(nonMatchingHandler.hasBeenCalled());
  }

  @Test
  public void shouldUseMatchingHandlerEvenForSubclassesOfMessages() {
    // given
    MatchingMessageHandler handler = new MatchingMessageHandler();
    messageBus.addHandler(handler);

    // when
    messageBus.send(mock(SubTypeOfMatchingMessage.class));

    // then
    assertTrue(handler.hasBeenCalled());
  }

  @Test
  public void shouldCallHandlerOncePerMessage() {
    // given
    MatchingMessageHandler handler = new MatchingMessageHandler();
    NonMatchingMessageHandler nonMatchingHandler = new NonMatchingMessageHandler();
    messageBus.addHandler(handler);
    messageBus.addHandler(nonMatchingHandler);

    // when
    messageBus.send(mock(MatchingMessage.class));
    messageBus.send(mock(MatchingMessage.class));
    messageBus.send(mock(MatchingMessage.class));
    messageBus.send(mock(NonMatchingMessage.class));

    // then
    assertTrue(handler.hasBeenCalled(3));
    assertTrue(nonMatchingHandler.hasBeenCalled(1));
  }

  private class MatchingMessage implements Message {
  }

  private class SubTypeOfMatchingMessage extends MatchingMessage {
  }

  private class MatchingMessageHandler extends CallInterceptor implements MessageHandler<MatchingMessage> {

    @Override
    public void handle(MatchingMessage message) {
      callCount++;
    }
  }

  private class NonMatchingMessage implements Message {
  }

  private class NonMatchingMessageHandler extends CallInterceptor implements MessageHandler<NonMatchingMessage> {

    @Override
    public void handle(NonMatchingMessage message) {
      callCount++;
    }
  }

  private class CallInterceptor {
    protected int callCount = 0;

    public boolean hasBeenCalled() {
      return 0 < callCount;
    }

    public boolean hasBeenCalled(int nTimes) {
      return callCount == nTimes;
    }
  }

}
