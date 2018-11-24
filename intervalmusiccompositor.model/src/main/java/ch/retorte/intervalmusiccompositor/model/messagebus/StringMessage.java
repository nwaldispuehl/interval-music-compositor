package ch.retorte.intervalmusiccompositor.model.messagebus;

/**
 * Message whose primary payload is a string.
 */
public interface StringMessage extends Message {

  String getMessage();
}
