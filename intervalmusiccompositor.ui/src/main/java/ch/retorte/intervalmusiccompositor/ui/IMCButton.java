package ch.retorte.intervalmusiccompositor.ui;

import java.awt.Dimension;

import javax.swing.JButton;

/**
 * @author nw
 */
public class IMCButton extends JButton {

	private static final long serialVersionUID = 1L;

	public IMCButton(String text) {
		super(text);
		
    Dimension standardSize = new Dimension(96, 38);
    Dimension maximumSize = new Dimension(120, 38);

    setMinimumSize(standardSize);
    setPreferredSize(standardSize);
    setMaximumSize(maximumSize);
	}
	
  @Override
  public void setText(String text) {
    super.setText(text);
    setToolTipText(text);
  }

}
