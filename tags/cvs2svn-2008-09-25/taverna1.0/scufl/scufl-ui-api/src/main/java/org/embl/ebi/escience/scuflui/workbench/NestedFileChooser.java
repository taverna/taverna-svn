package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Checkbox;
import java.awt.Component;
import java.awt.HeadlessException;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * Standard {@link JFileChooser} with a checkbox to state if you want to save a
 * nested workflow in full or just as a URL link
 * 
 * @author Ian Dunlop
 * 
 */
public class NestedFileChooser extends JFileChooser {
	
	private Logger logger = Logger.getLogger(NestedFileChooser.class);

	private Checkbox checkbox;

	public NestedFileChooser() {
		checkbox = new Checkbox("Embed nested workflow(s)");
	}

	/**
	 * Has the user checked the "include full nested workflow" {@link Checkbox}
	 * 
	 * @return
	 */
	public boolean getSaveNested() {
		return checkbox.getState();
	}

	/**
	 * Create the {@link JDialog} which contains the file chooser and add a
	 * {@link Checkbox} to it
	 */
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
