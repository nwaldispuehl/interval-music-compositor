package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Message whose primary payload is a string.
 */
interface StringMessage extends Message {

  String getMessage();
}
