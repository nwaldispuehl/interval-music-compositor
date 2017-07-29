package ch.retorte.intervalmusiccompositor.ui.soundeffects;

import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import javafx.scene.control.ListCell;
import org.apache.commons.lang3.StringUtils;

/**
 * Cell for the special effects combo box. Acts as a label provider as we only use text.
 */
public class SoundEffectListCell extends ListCell<SoundEffect> {

  private FormatTime formatTime = new FormatTime();

  @Override
  protected void updateItem(SoundEffect item, boolean empty) {
    super.updateItem(item, empty);

    if (item != null) {
      String itemTitle = item.getId();
      double itemDurationSeconds = item.getDisplayDurationMillis() / 1000;

      setText(StringUtils.capitalize(itemTitle) + " (" + formatTime.getStrictFormattedTime(itemDurationSeconds) + "s)");
    }
  }
}
