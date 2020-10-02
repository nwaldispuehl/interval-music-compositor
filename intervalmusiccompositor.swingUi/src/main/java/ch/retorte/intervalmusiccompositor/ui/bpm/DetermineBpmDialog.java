package ch.retorte.intervalmusiccompositor.ui.bpm;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.ui.IMCButton;

/**
 * @author nw
 */
public class DetermineBpmDialog extends JFrame {

  private static final long serialVersionUID = 1L;

  private MessageFormatBundle bundle = getBundle("ui_imc");

  private JPanel contentPane;
  public JSpinner bpmField;
  public JLabel tapLabel;

  /**
   * Create the frame.
   */
  public DetermineBpmDialog(final DetermineBpmDialogControl bpmc) {

    setTitle(bundle.getString("ui.determine_bpm.title"));

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        bpmc.closeDetermineBpmDialog();
      }
    });

    setAlwaysOnTop(true);

    setBounds(200, 200, 400, 200);
    setResizable(false);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);

    JLabel bpmDescription = new JLabel(bundle.getString("ui.determine_bpm.description"));

    JPanel playerPanel = new JPanel();
    playerPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

    JPanel bpmPanel = new JPanel();

    IMCButton okButton = new IMCButton(bundle.getString("ui.determine_bpm.ok_button.text"));

    okButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        bpmc.updateBpm((Integer) bpmField.getValue());
        bpmc.closeDetermineBpmDialog();
      }
    });

    IMCButton cancelButton = new IMCButton(bundle.getString("ui.determine_bpm.cancel_button.text"));
    cancelButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        bpmc.closeDetermineBpmDialog();
      }
    });

    GroupLayout gl_contentPane = new GroupLayout(contentPane);
    gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(
        gl_contentPane
            .createSequentialGroup()
            .addContainerGap()
            .addGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                    // was: 438
                    .addComponent(bpmPanel, 380, 380, 380).addComponent(playerPanel, 380, 380, 380)
                    // was: 450
                    .addComponent(bpmDescription, 380, 380, 380)
                    .addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup().addComponent(cancelButton).addGap(18).addComponent(okButton)))
            .addContainerGap()));
    gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
        gl_contentPane.createSequentialGroup().addContainerGap()
            .addComponent(bpmDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18)
            .addComponent(playerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18)
            .addComponent(bpmPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18)
            .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(okButton).addComponent(cancelButton))
            .addContainerGap(61, Short.MAX_VALUE)));
    bpmPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    JLabel bpmLabel = new JLabel(bundle.getString("ui.determine_bpm.bpm_field.label"));
    bpmPanel.add(bpmLabel);

    bpmField = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    bpmPanel.add(bpmField);
    playerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    JButton playButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.determine_bpm.play_button.icon"))));
    playButton.setToolTipText(bundle.getString("ui.determine_bpm.play_button.tooltip"));
    playerPanel.add(playButton);

    playButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        bpmc.startPlayingMusic();
      }
    });

    JButton stopButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.determine_bpm.stop_button.icon"))));
    stopButton.setToolTipText(bundle.getString("ui.determine_bpm.stop_button.tooltip"));
    playerPanel.add(stopButton);

    stopButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        bpmc.stopPlayingMusic();
      }
    });

    // Make some space
    playerPanel.add(new Separator());

    tapLabel = new JLabel(bundle.getString("ui.determine_bpm.tap_label"));

    Dimension labelSize = new Dimension(92, 24);
    tapLabel.setMinimumSize(labelSize);
    tapLabel.setPreferredSize(labelSize);
    tapLabel.setMaximumSize(labelSize);
    tapLabel.setHorizontalAlignment(SwingConstants.CENTER);

    playerPanel.add(tapLabel);

    // Make some space
    playerPanel.add(new Separator());

    JButton tapButton = new JButton(bundle.getString("ui.determine_bpm.tap_button.text"));
    tapButton.setToolTipText(bundle.getString("ui.determine_bpm.tap_button.tooltip"));

    playerPanel.add(tapButton);

    Dimension buttonSize = new Dimension(92, 54);

    tapButton.setMinimumSize(buttonSize);
    tapButton.setPreferredSize(buttonSize);
    tapButton.setMaximumSize(buttonSize);

    tapButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        bpmc.tap();
      }
    });

    contentPane.setLayout(gl_contentPane);

    pack();
  }
}
