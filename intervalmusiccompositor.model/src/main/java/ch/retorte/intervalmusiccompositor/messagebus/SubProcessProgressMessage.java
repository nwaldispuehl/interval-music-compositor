package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Transports percentage of a sub process.
 */
public class SubProcessProgressMessage implements Message {

  private int percentage;

  public SubProcessProgressMessage(int percentage) {
    this.percentage = percentage;
  }

  public int getPercentage() {
    return percentage;
  }
}
