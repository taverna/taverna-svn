/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentFileType;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentVersionChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraph;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

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
		putValue(SMALL_ICON, ComponentServiceIcon.getIcon());
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
			ComponentRegistry registry = panel.getChosenRegistry();
			ComponentFamily family = panel.getChosenFamily();
			Component component = panel.getChosenComponent();
			ComponentVersion version = panel.getChosenComponentVersion();
			
			ComponentVersionIdentification ident = new ComponentVersionIdentification(registry.getRegistryBase(),family.getName(), component.getName(), version.getVersionNumber() );
			
			try {
				Dataflow d = fm.openDataflow(fileType, ident);
				
				final GraphController gc = GraphViewComponent.graphControllerMap.get(d);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						SVGGraph g = (SVGGraph) gc.getGraph();
						g.setFillColor(Color.RED);
						gc.redraw();
					}});
			} catch (OpenException e) {
				logger.error(e);
			}
		}
	}

}
