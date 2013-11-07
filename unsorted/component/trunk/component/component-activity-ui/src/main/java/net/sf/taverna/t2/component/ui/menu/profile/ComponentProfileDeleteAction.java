/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentProfileDeleteAction extends AbstractAction {
	private static final long serialVersionUID = -5697971204434020559L;
	@SuppressWarnings("unused")
	private static final Logger log = getLogger(ComponentProfileDeleteAction.class);
	private static final String DELETE_PROFILE = "Delete profile...";

	public ComponentProfileDeleteAction() {
		super(DELETE_PROFILE, getIcon());
		this.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// FIXME Not yet implemented
		showMessageDialog(null, "Not yet implemented");
	}

}
