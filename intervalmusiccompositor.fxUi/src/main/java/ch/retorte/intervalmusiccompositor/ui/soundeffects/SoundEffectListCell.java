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
      double itemDurationSeconds = item.getDurationMillis() / 1000;

      // We cheat a bit here to prevent short samples from being listed as of length 0.
      if (0.2 < itemDurationSeconds && itemDurationSeconds < 1) {
        itemDurationSeconds = 1;
      }

      setText(StringUtils.capitalize(itemTitle) + " (" + formatTime.getStrictFormattedTime(itemDurationSeconds) + "s)");
    }
  }
}
