package ch.retorte.intervalmusiccompositor.ui.soundeffects;

import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.ui.bundle.UiBundleProvider;
import javafx.scene.control.ListCell;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Cell for the special effects combo box. Acts as a label provider as we only use text.
 */
public class SoundEffectListCell extends ListCell<SoundEffect> {

    //---- Static

    private static final Map<String, String> soundEffectTitleMap = new HashMap<>();

    static {
        soundEffectTitleMap.put("gong", "ui.form.sound_effects.names.gong");
        soundEffectTitleMap.put("whistle", "ui.form.sound_effects.names.whistle");
        soundEffectTitleMap.put("double_whistle", "ui.form.sound_effects.names.double_whistle");
    }

    //---- Fields

    private final MessageFormatBundle bundle = new UiBundleProvider().getBundle();

    private final FormatTime formatTime = new FormatTime();


    //---- Methods

    @Override
    protected void updateItem(SoundEffect item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            String itemId = item.getId();
            double itemDurationSeconds = item.getDisplayDurationMillis() / 1000.0;

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
