/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;

/**
 * @author alanrw
 * 
 */
public class ComponentProfileDeleteAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5697971204434020559L;
	private static final String DELETE_PROFILE = "Delete profile...";

	public ComponentProfileDeleteAction() {
		super(DELETE_PROFILE, ComponentServiceIcon.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		showMessageDialog(null, "Not yet implemented");
	}

}
