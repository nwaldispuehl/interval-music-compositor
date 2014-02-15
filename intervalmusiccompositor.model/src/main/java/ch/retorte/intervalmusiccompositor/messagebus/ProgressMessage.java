package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Message object used for progress visualization.
 * 
 * @author nw
 */
public class ProgressMessage implements Message {

  private final Integer progressInPercent;
  private final String currentActivity;

  public ProgressMessage(Integer progressInPercent, String currentActivity) {
    this.progressInPercent = progressInPercent;
    this.currentActivity = currentActivity;
  }

  public Integer getProgressInPercent() {
    return progressInPercent;
  }

  public String getCurrentActivity() {
    return currentActivity;
  }

}
