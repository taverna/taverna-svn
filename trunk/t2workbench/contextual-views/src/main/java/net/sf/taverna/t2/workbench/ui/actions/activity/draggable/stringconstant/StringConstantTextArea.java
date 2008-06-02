package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;

/**
 * Might be required to add any listener events at a later date
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantTextArea extends JTextArea implements ActionListener {

	public StringConstantTextArea(String string) {
		super(string);
	}

	public void actionPerformed(ActionEvent e) {
		// this.setText(e.)
	}

}
