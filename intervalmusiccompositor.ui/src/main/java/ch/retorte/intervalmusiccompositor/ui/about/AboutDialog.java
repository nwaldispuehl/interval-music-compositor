package ch.retorte.intervalmusiccompositor.ui.about;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.ui.IMCButton;
import ch.retorte.intervalmusiccompositor.ui.SwingUserInterface;

/**
 * @author nw
 */
public class AboutDialog extends JFrame {

  private MessageFormatBundle bundle = getBundle("ui_imc");

  private static final long serialVersionUID = 1L;

  private JPanel contentPane;
  private SwingUserInterface ui;
  private AboutDialogControl control;

  /**
   * Create the frame.
   */
  public AboutDialog(final SwingUserInterface ui, ApplicationData applicationData, final AboutDialogControl control) {
    this.ui = ui;
    this.control = control;

    setTitle(bundle.getString("ui.about.title"));
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        control.close();
      }
    });

    setBounds(200, 200, 440, 350);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);

    JLabel label = new JLabel("");
    label.setIcon(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.about.icon"))));
    JLabel lblRetorteIntervalMusic = new JLabel(bundle.getString("ui.about.text", applicationData.getProgramVersion()));
    lblRetorteIntervalMusic.setVerticalAlignment(SwingConstants.TOP);

    IMCButton closeButton = new IMCButton(bundle.getString("ui.about.close.button"));
    closeButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        control.close();
      }
    });

    JPanel panel = new JPanel();
    panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

    JLabel hyperlinkLabel = createHyperlinkLabel();

    GroupLayout gl_contentPane = new GroupLayout(contentPane);
    gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(
        gl_contentPane
            .createSequentialGroup()
            .addContainerGap()
            .addGroup(
                gl_contentPane
                    .createParallelGroup(Alignment.LEADING)
                    .addComponent(panel, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(
                        gl_contentPane
                            .createSequentialGroup()
                            .addComponent(label)
                            .addGap(18)
                            .addGroup(
                                gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                    .addComponent(hyperlinkLabel, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                    .addComponent(lblRetorteIntervalMusic, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)))
                    .addComponent(closeButton, Alignment.TRAILING)).addContainerGap()));
    gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
        gl_contentPane
            .createSequentialGroup()
            .addContainerGap()
            .addGroup(
                gl_contentPane
                    .createParallelGroup(Alignment.LEADING, false)
                    .addComponent(label)
                    .addGroup(
                        gl_contentPane.createSequentialGroup()
                            .addComponent(lblRetorteIntervalMusic, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(hyperlinkLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addGap(16)
            .addComponent(panel, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(closeButton).addGap(6)));

    JButton btnCheckForUpdates = new JButton(bundle.getString("ui.about.update.button"));
    btnCheckForUpdates.setHorizontalAlignment(SwingConstants.CENTER);

    final JLabel lblNotSearchedYet = new JLabel(bundle.getString("ui.about.update.message.notyet"));
    lblNotSearchedYet.setHorizontalAlignment(SwingConstants.CENTER);

    btnCheckForUpdates.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        control.checkForUpdates(lblNotSearchedYet, getContentPane());
      }
    });

    GroupLayout gl_panel = new GroupLayout(panel);
    gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
        gl_panel
            .createSequentialGroup()
            .addContainerGap()
            .addGroup(
                gl_panel.createParallelGroup(Alignment.LEADING).addComponent(lblNotSearchedYet, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(btnCheckForUpdates)).addContainerGap()));
    gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
        gl_panel.createSequentialGroup().addContainerGap().addComponent(btnCheckForUpdates).addGap(12)
            .addComponent(lblNotSearchedYet, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    panel.setLayout(gl_panel);
    contentPane.setLayout(gl_contentPane);
    pack();
  }

  private JLabel createHyperlinkLabel() {
    if (control.isLinkClickable()) {
      return getClickableLink();
    }
    return getTextOnlyLink();
  }

  private JLabel getTextOnlyLink() {
    JLabel result = new JLabel(bundle.getString("ui.about.plainHyperlink"));
    result.setToolTipText(bundle.getString("ui.about.website.url"));
    return result;
  }

  private JLabel getClickableLink() {
    JLabel result = new JLabel(bundle.getString("ui.about.hyperlink"));
    result.setToolTipText(bundle.getString("ui.about.website.url"));
    result.addMouseListener(new MouseListener() {

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setCursor(ui.getCursor());
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        control.openWebsite();
      }
    });
    return result;
  }

}
