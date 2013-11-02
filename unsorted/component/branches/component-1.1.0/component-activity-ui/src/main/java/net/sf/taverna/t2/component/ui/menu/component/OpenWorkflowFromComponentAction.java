/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static java.awt.Color.RED;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.workbench.views.graph.GraphViewComponent.graphControllerMap;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentVersionChooserPanel;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraph;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class OpenWorkflowFromComponentAction extends AbstractAction {
	private static final long serialVersionUID = 7382677337746318211L;
	private static final Logger logger = Logger
			.getLogger(OpenWorkflowFromComponentAction.class);
	private static final String ACTION_NAME = "Open component...";
	private static final String ACTION_DESCRIPTION = "Open the workflow that implements a component";
	private static final FileManager fm = FileManager.getInstance();

	public OpenWorkflowFromComponentAction(final java.awt.Component component) {
		putValue(SMALL_ICON, getIcon());
		putValue(NAME, ACTION_NAME);
		putValue(SHORT_DESCRIPTION, ACTION_DESCRIPTION);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ComponentVersionChooserPanel panel = new ComponentVersionChooserPanel();

		int result = showConfirmDialog(null, panel, "Component version choice",
				OK_CANCEL_OPTION);
		if (result == OK_OPTION)
			doOpen(panel.getChosenRegistry(), panel.getChosenFamily(),
					panel.getChosenComponent(),
					panel.getChosenComponentVersion());
	}

	private void doOpen(Registry registry, Family family, Component component,
			Version version) {
		Version.ID ident = new ComponentVersionIdentification(
				registry.getRegistryBase(), family.getName(),
				component.getName(), version.getVersionNumber());

		try {
			Dataflow d = fm.openDataflow(ComponentFileType.instance, ident);

			final GraphController gc = graphControllerMap.get(d);
			invokeLater(new Runnable() {
				@Override
				public void run() {
					SVGGraph g = (SVGGraph) gc.getGraph();
					g.setFillColor(RED);
					gc.redraw();
				}
			});
		} catch (OpenException e) {
			logger.error("failed to open component definition", e);
		}
	}
}
