/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentFamilyChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceDesc;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

/**
 * @author alanrw
 *
 */
public class OpenWorkflowFromComponentAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(OpenWorkflowFromComponentAction.class);

	
	private static final String ACTION_NAME = "Open component...";

	private static final String ACTION_DESCRIPTION = "Open the workflow that implements a component";
	
	private static FileManager fm = FileManager.getInstance();
	
	private static ComponentFileType fileType = new ComponentFileType();

	public OpenWorkflowFromComponentAction(final java.awt.Component component) {
		putValue(SMALL_ICON, WorkbenchIcons.openurlIcon);
		putValue(NAME, ACTION_NAME);
		putValue(SHORT_DESCRIPTION, ACTION_DESCRIPTION);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		ComponentVersionChooserPanel panel = new ComponentVersionChooserPanel();
		
		int result = JOptionPane.showConfirmDialog(null, panel, "Component version choice", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			ComponentRegistry registry = panel.getSourceChoice();
			ComponentFamily family = panel.getFamilyChoice();
			Component component = panel.getComponentChoice();
			ComponentVersion version = panel.getComponentVersionChoice();
			
			ComponentVersionIdentification ident = new ComponentVersionIdentification(registry.getRegistryBase(),family.getName(), component.getName(), version.getVersionNumber() );
			ComponentServiceDesc desc = new ComponentServiceDesc(ident);
			
			try {
				fm.openDataflow(fileType, desc);
			} catch (OpenException e) {
				logger.error(e);
			}
		}
	}

}
