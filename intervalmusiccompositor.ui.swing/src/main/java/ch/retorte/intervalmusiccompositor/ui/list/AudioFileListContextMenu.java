package ch.retorte.intervalmusiccompositor.ui.list;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.list.AudioFileListType;
import ch.retorte.intervalmusiccompositor.ui.SwingUserInterface;

/**
 * @author nw
 */
public class AudioFileListContextMenu extends JPopupMenu {

  private MessageFormatBundle bundle = getBundle("ui_imc");

  private static final long serialVersionUID = 6462320097282371763L;

  public AudioFileListContextMenu(final DraggableAudioFileList list, final SwingUserInterface ui) {

    JMenuItem remove = new JMenuItem(bundle.getString("ui.list_context.remove_track_pl"));
    JMenuItem changeBpm = new JMenuItem(bundle.getString("ui.list_context.change_bpm"));
    JMenuItem writeBpm = new JMenuItem(bundle.getString("ui.list_context.write_bpm"));

    add(remove);
    addSeparator();
    add(changeBpm);
    add(writeBpm);

    if (1 < list.getSelectedIndices().length) {
      // If more than one item is selected, render 'changeBPM' inactive
      changeBpm.setEnabled(false);
    }

    if (list.getSelectedIndices().length == 1) {
      // If only one item is selected, change text to singular
      remove.setText(bundle.getString("ui.list_context.remove_track_sg"));

      // If this item does not support bpm, deactivate the 'write' option
      if (!((((DefaultListModel<IAudioFile>) list.getModel()).get(list.getSelectedIndex()))).isBpmSupported()) {
        writeBpm.setEnabled(false);
      }

      // If this item has not an ok status, deactivate the bpm menue items
      if (!((((DefaultListModel<IAudioFile>) list.getModel()).get(list.getSelectedIndex()))).isOK()) {
        changeBpm.setEnabled(false);
        writeBpm.setEnabled(false);
      }
    }

    if (list.getSelectedIndices().length == 0) {
      // If there are no entries at all, deactivate all menu items
      remove.setEnabled(false);
      changeBpm.setEnabled(false);
      writeBpm.setEnabled(false);
    }

    remove.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (list.getType().equals(AudioFileListType.MUSIC)) {
          list.getMusicListControl().removeMusicTracks(list.getSelectedIndices());
        } else {
          list.getMusicListControl().removeBreakTracks(list.getSelectedIndices());
        }
      }
    });

    changeBpm.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        ui.startBpmDeterminationFor(list.getType(), list.getSelectedIndex());
      }
    });

    writeBpm.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int[] indexList = list.getSelectedIndices();
        if (list.getType().equals(AudioFileListType.MUSIC)) {
          list.getMusicListControl().writeMusicBpm(indexList);
        } else {
          list.getMusicListControl().writeBreakBpm(indexList);
        }
      }
    });
  }
}
