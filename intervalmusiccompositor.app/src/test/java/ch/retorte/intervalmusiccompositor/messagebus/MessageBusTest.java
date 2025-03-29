package ch.retorte.intervalmusiccompositor.messagebus;

import ch.retorte.intervalmusiccompositor.model.messagebus.Message;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test for the {@link MessageBus}.
 *
 * @author nw
 */
public class MessageBusTest {

    private MessageBus messageBus;
    private MatchingMessageHandler matchingHandler;
    private NonMatchingMessageHandler nonMatchingHandler;

    @BeforeEach
    public void setup() {
        messageBus = new MessageBus(false);
        matchingHandler = new MatchingMessageHandler();
        nonMatchingHandler = new NonMatchingMessageHandler();
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
        messageBus.addHandler(matchingHandler);

        // when
        messageBus.send(mock(NonMatchingMessage.class));

        // then
        assertFalse(matchingHandler.hasBeenCalled());
    }

    @Test
    public void shouldUseMatchingHandlerForMessages() {
        // given
        messageBus.addHandler(matchingHandler);
        messageBus.addHandler(nonMatchingHandler);

        // when
        messageBus.send(mock(MatchingMessage.class));

        // then
        waitForDispatchedHandlerThreadsMs(50);
        assertTrue(matchingHandler.hasBeenCalled());
        assertFalse(nonMatchingHandler.hasBeenCalled());
    }

    @Test
    public void shouldUseMatchingHandlerEvenForSubclassesOfMessages() {
        // given
        messageBus.addHandler(matchingHandler);

        // when
        messageBus.send(mock(SubTypeOfMatchingMessage.class));

        // then
        waitForDispatchedHandlerThreadsMs(50);
        assertTrue(matchingHandler.hasBeenCalled());
    }

    private void waitForDispatchedHandlerThreadsMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // nop
        }
    }

    @Test
    public void shouldCallHandlerOncePerMessage() {
        // given
        messageBus.addHandler(matchingHandler);
        messageBus.addHandler(nonMatchingHandler);

        // when
        messageBus.send(mock(MatchingMessage.class));
        messageBus.send(mock(MatchingMessage.class));
        messageBus.send(mock(MatchingMessage.class));
        messageBus.send(mock(NonMatchingMessage.class));

        // then
        waitForDispatchedHandlerThreadsMs(50);
        assertTrue(matchingHandler.hasBeenCalled(3));
        assertTrue(nonMatchingHandler.hasBeenCalled(1));
    }

    public static class MatchingMessage implements Message {
    }

    public static class SubTypeOfMatchingMessage extends MatchingMessage {
    }

    private static class MatchingMessageHandler extends CallInterceptor implements MessageHandler<MatchingMessage> {

        @Override
        public void handle(MatchingMessage message) {
            callCount++;
        }
    }

    public static class NonMatchingMessage implements Message {
    }

    private static class NonMatchingMessageHandler extends CallInterceptor implements MessageHandler<NonMatchingMessage> {

        @Override
        public void handle(NonMatchingMessage message) {
            callCount++;
        }
    }

    private static class CallInterceptor {
        int callCount = 0;

        boolean hasBeenCalled() {
            return 0 < callCount;
        }

        boolean hasBeenCalled(int nTimes) {
            return callCount == nTimes;
        }
    }

}
