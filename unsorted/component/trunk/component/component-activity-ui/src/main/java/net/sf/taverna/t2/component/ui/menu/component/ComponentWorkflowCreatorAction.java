/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ComponentWorkflowCreatorAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(ComponentWorkflowCreatorAction.class);

	
	private static final String CREATE_COMPONENT = "Create component...";
	
	public ComponentWorkflowCreatorAction() {
		super(CREATE_COMPONENT, ComponentServiceIcon.getIcon());
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Dataflow d = FileManager.getInstance().getCurrentDataflow();
		try {
			ComponentServiceCreatorAction.saveWorkflowAsComponent(d, d.getLocalName());
		} catch (Exception e1) {
			logger.error(e1);
		}
	}

}
