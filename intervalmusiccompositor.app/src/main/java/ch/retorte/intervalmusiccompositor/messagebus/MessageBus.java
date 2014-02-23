package ch.retorte.intervalmusiccompositor.messagebus;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;

import com.google.common.collect.Lists;

/**
 * The message bus implements the {@link MessageSubscriber} as well as the {@link MessageProducer} and directs sent messages to the right recipients. It is the
 * main backbone of message transfer of this software.
 * 
 * @author nw
 */
public class MessageBus implements MessageProducer, MessageSubscriber {

  List<MessageHandler<? super Message>> messageHandlers = newArrayList();

  @Override
  public void send(Message message) {
    for (MessageHandler<? super Message> handler : messageHandlers) {
      if (canHandleMessageType(handler, message)) {
        handler.handle(message);
      }
    }
  }

  private boolean canHandleMessageType(MessageHandler<? super Message> handler, Message message) {
    for (Type t : getGenericTypes(handler)) {
      if (((Class<?>) t).isAssignableFrom(message.getClass())) {
        return true;
      }
    }
    return false;
  }

  private List<Type> getGenericTypes(MessageHandler<? super Message> handler) {
    Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
    if (genericInterfaces.length == 0) {
      return Lists.newArrayList();
    }

    ParameterizedType firstGenericInterface = (ParameterizedType) genericInterfaces[0];
    return Lists.newArrayList(firstGenericInterface.getActualTypeArguments());

  }

  @Override
  @SuppressWarnings("unchecked")
  public void addHandler(MessageHandler<? extends Message> messageHandler) {
    messageHandlers.add((MessageHandler<? super Message>) messageHandler);
  }

}
