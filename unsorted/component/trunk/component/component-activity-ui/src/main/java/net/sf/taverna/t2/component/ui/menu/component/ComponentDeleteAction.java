/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.panel.ProfileChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentDeleteAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(ComponentDeleteAction.class);
		
	private static final String DELETE_COMPONENT = "Delete component...";
	
	public ComponentDeleteAction() {
		super (DELETE_COMPONENT, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		JOptionPane.showMessageDialog(null, "Not yet implemented");
	}

}
