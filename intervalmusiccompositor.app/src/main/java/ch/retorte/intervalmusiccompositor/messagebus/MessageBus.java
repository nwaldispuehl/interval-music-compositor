package ch.retorte.intervalmusiccompositor.messagebus;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.LogBuffer;
import ch.retorte.intervalmusiccompositor.model.messagebus.Message;
import ch.retorte.intervalmusiccompositor.model.messagebus.StringMessage;
import com.google.common.collect.Lists;

/**
 * The message bus implements the {@link MessageSubscriber} as well as the {@link MessageProducer} and directs sent messages to the right recipients. It is the
 * main backbone of message transfer of this software.
 * 
 * @author nw
 */
public class MessageBus implements MessageProducer, MessageSubscriber {



  //---- Fields

  private LogBuffer logBuffer = new LogBuffer();
  private List<MessageHandler<? super Message>> messageHandlers = newArrayList();
  private boolean storeMessages;

  //---- Constructor

  public MessageBus(boolean storeMessages) {
    this.storeMessages = storeMessages;
  }

  @Override
  public void send(Message message) {
    conditionallyStore(message);

    messageHandlers.stream()
        .filter(handler -> canHandleMessageType(handler, message))
        .forEach(handler -> new Thread(() -> handler.handle(message)).start());
  }

  private void conditionallyStore(Message message) {
    if (storeMessages && (message instanceof StringMessage)) {
      LocalDateTime date = null;
      String caller = null;
      if (message instanceof DebugMessage) {
        date = ((DebugMessage) message).getDate();
        caller = ((DebugMessage) message).getCallerClassName();
      }

      store(date, caller, ((StringMessage) message).getMessage());
    }
  }

  private void store(LocalDateTime date, String caller, String message) {
    logBuffer.append(date, caller, message);
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

  public LogBuffer getLogBuffer() {
    return logBuffer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addHandler(MessageHandler<? extends Message> messageHandler) {
    messageHandlers.add((MessageHandler<? super Message>) messageHandler);
  }

}
