package ch.retorte.intervalmusiccompositor.ui.list;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SHUFFLE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;

/**
 * @author nw
 */
public class AudioFileCellRenderer extends JLabel implements ListCellRenderer<IAudioFile> {


  public static final Color SELECTED_BACKGROUND = new Color(255, 143, 81);
  public static final Color DEFAULT_BACKGROUND = new Color(255, 203, 164);
  private final MessageFormatBundle bundle = getBundle("ui_imc");

  private final ImageIcon musicIcon = new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("imc.audiofile.musicicon")));
  private final ImageIcon tooShortIcon = new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("imc.audiofile.tooshorticon")));
  private final ImageIcon errorIcon = new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("imc.audiofile.erroricon")));
  private final ImageIcon waitingIcon = new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("imc.audiofile.waitingicon")));

  private static final long serialVersionUID = 1L;

  private List<Integer> soundPattern;
  private ListSortMode listSortMode;
  private BlendMode blendMode;
  private double blendTime;

  public AudioFileCellRenderer() {

    setOpaque(true);
    setHorizontalAlignment(LEFT);
    setVerticalAlignment(TOP);
    setBorder(BorderFactory.createLineBorder(Color.white, 1));
    setCursor(new Cursor(Cursor.MOVE_CURSOR));
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IAudioFile> list, IAudioFile audioFile, int index, boolean isSelected, boolean cellHasFocus) {

    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }

    StringBuilder sb = new StringBuilder();

    ImageIcon imgIcon = musicIcon;

    String fileName = audioFile.getDisplayName();
    if (fileName.length() > 32) {
      fileName = fileName.substring(0, 30) + "...";
    }

    sb.append("<html><body>");
    sb.append("Track ").append(index + 1).append("<br/>");

    if (audioFile.isOK()) {

      setToolTipText(audioFile.getDisplayName());

      sb.append("<b>").append(fileName).append("</b>").append("<br/>");
      sb.append("<i>");
      sb.append(bundle.getString("ui.list.duration")).append(new FormatTime().getStrictFormattedTime((int) (audioFile.getDuration() / 1000)));

      appendBpmInformation(sb, audioFile);

      sb.append("</i><br/>");

      // If the file is technically ok, but too short for the current chosen configuration,
      // indicate that by an other icon and a respective tool tip
      if (!isTrackLongEnough(audioFile, index)) {
        setToolTipText(bundle.getString("ui.form.music_list.tooshort_error"));
        imgIcon = tooShortIcon;
      }

    } else if (audioFile.hasError()) {

      setToolTipText(audioFile.getErrorMessage());

      sb.append("<b>").append(fileName).append("</b>").append("<br/>");
      sb.append("<i>").append(audioFile.getErrorMessage()).append("</i>");

      imgIcon = errorIcon;

      if (isSelected) {
        setBackground(SELECTED_BACKGROUND);
      } else {
        setBackground(DEFAULT_BACKGROUND);
      }

    }
    else {

      sb.append("<b>").append(fileName).append("</b>").append("<br/>");

      sb.append("<i>");
      if (audioFile.isQueued()) {
        sb.append(bundle.getString("ui.list.waiting"));
      }
      else {
        sb.append(bundle.getString("ui.list.importing"));
      }
      sb.append("</i>");

      imgIcon = waitingIcon;
      imgIcon.setImageObserver(list);

      setForeground(new Color(92, 92, 92));

    }

    sb.append("</body></html>");
    setText(sb.toString());
    setFont(list.getFont());

    setIcon(imgIcon);
    setIconTextGap(4);

    return this;
  }

  private boolean isTrackLongEnough(IAudioFile audioFile, int index) {

    int extractLength = 0;
    if (!soundPattern.isEmpty()) {
      extractLength = soundPattern.get(index % soundPattern.size());
    }

    //TODO: Does this work also with lists of patterns? -> Not when the pattern list is > track list.

    if (listSortMode == SHUFFLE) {
      int longestPattern = 0;
      for (Integer i : soundPattern) {
        longestPattern = Math.max(longestPattern, i);
      }
      extractLength = longestPattern;
    }

    if (blendMode == CROSS) {
      extractLength += blendTime;
    }

//    return (extractLength * 1000 + startCutoffTimeMillis + endCutoffTimeMillis) <= audioFile.getDuration();
    return audioFile.isLongEnoughFor(extractLength);
  }

  private void appendBpmInformation(StringBuilder sb, IAudioFile audioFile) {
    if (audioFile.hasBpm()) {

      // Indicate the status by colors
      if (audioFile.isBpmStored()) {

        // Display in blue
        sb.append(", <font color='#2F60AB'>").append(audioFile.getBpm()).append(" bpm</font>");

      } else if (audioFile.isBpmReliable()) {

        // Display in green
        sb.append(", <font color='#5BA853'>").append(audioFile.getBpm()).append(" bpm</font>");

      } else {

        // Display in orange
        sb.append(", <font color='#D7AD04'>").append(audioFile.getBpm()).append(" bpm</font>");

      }

    }
  }

  public void setDurationPatterns(List<Integer> soundPattern, ListSortMode listSortMode, BlendMode blendMode, double blendTime) {
    this.soundPattern = soundPattern;
    this.listSortMode = listSortMode;
    this.blendMode = blendMode;
    this.blendTime = blendTime;
  }
}
