package ch.retorte.intervalmusiccompositor.spi.messagebus;

import ch.retorte.intervalmusiccompositor.model.messagebus.Message;


/**
 * The {@link MessageSubscriber} is the part of the message bus which is able to receive messages, that is, hand them over to matching {@link MessageHandler}.
 * The contract is that the {@link MessageSubscriber}s only hands matching {@link Message}s to a {@link MessageHandler}.
 * 
 * We did not choose the term consumer since it is not supposed to consume them. There can be arbitrary many {@link MessageSubscriber}, any every one of them
 * receives the {@link Message} intended for them.
 * 
 * 
 * @author nw
 */
public interface MessageSubscriber {

  /**
   * Subscribes to a certain message type with the provided handler. The message type is determined by the generic parameter of the {@link MessageHandler}.
   * 
   * @param messageHandler
   *          the handler to react on sent messages.
   */
  void addHandler(MessageHandler<? extends Message> messageHandler);

}
