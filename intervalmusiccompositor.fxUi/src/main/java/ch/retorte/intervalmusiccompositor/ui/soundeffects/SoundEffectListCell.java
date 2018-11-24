package ch.retorte.intervalmusiccompositor.ui.soundeffects;

import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import javafx.scene.control.ListCell;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Cell for the special effects combo box. Acts as a label provider as we only use text.
 */
public class SoundEffectListCell extends ListCell<SoundEffect> {

  //---- Static

  private static Map<String, String> soundEffectTitleMap = newHashMap();

  static {
    soundEffectTitleMap.put("gong", "ui.form.sound_effects.names.gong");
    soundEffectTitleMap.put("whistle", "ui.form.sound_effects.names.whistle");
    soundEffectTitleMap.put("double_whistle", "ui.form.sound_effects.names.double_whistle");
  }

  //---- Fields

  private MessageFormatBundle bundle = getBundle(UI_RESOURCE_BUNDLE_NAME);

  private FormatTime formatTime = new FormatTime();


  //---- Methods

  @Override
  protected void updateItem(SoundEffect item, boolean empty) {
    super.updateItem(item, empty);

    if (item != null) {
      String itemId = item.getId();
      double itemDurationSeconds = item.getDisplayDurationMillis() / 1000;

      setText(getNameFor(itemId) + " (" + formatTime.getStrictFormattedTime(itemDurationSeconds) + "s)");
    }
  }

  private String getNameFor(String itemId) {
    if (soundEffectTitleMap.containsKey(itemId)) {
      return bundle.getString(soundEffectTitleMap.get(itemId));
    }

    return StringUtils.capitalize(itemId);
  }
}
