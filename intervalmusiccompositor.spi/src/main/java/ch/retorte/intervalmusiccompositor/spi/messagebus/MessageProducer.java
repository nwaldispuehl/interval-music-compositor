package ch.retorte.intervalmusiccompositor.spi.messagebus;

import ch.retorte.intervalmusiccompositor.model.messagebus.Message;


/**
 * The {@link MessageProducer} is the part of the message bus which feed it with new messages.
 */
public interface MessageProducer {

    /**
     * Sends a message into the bus.
     */
    void send(Message message);
}
