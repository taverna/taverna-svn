package org.embl.ebi.escience.scufl.shared;

import java.awt.Checkbox;
import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class NestedFileChooser extends JFileChooser {
	

	private Logger logger = Logger.getLogger(NestedFileChooser.class);
	
	private Checkbox checkbox;
	
	public NestedFileChooser() {
		checkbox = new Checkbox("include full nested workflow");
	}
	
	public boolean getSaveNested() {
		return checkbox.getState();
	}
	
	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException {
		JDialog dialog = super.createDialog(parent);
		JPanel panel = createPanel();
		dialog.add(panel, java.awt.BorderLayout.SOUTH);
		dialog.pack();
		return dialog;
	}
	
	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.add(checkbox);
		return panel;
	}
}
