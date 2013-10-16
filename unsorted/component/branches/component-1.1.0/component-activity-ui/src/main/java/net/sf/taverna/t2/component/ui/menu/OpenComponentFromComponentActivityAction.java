/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.workbench.views.graph.GraphViewComponent.graphControllerMap;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraph;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings("serial")
public class OpenComponentFromComponentActivityAction extends AbstractAction {

	private static Logger logger = Logger
			.getLogger(OpenComponentFromComponentActivityAction.class);

	private static final FileManager fileManager = FileManager.getInstance();

	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();

	public OpenComponentFromComponentActivityAction() {
		super("Open component...", ComponentServiceIcon.getIcon());
	}

	private ComponentActivity selection;

	@Override
	public void actionPerformed(ActionEvent ev) {

		Version.ID ident = selection.getConfiguration();

		try {
			Dataflow d = fileManager.openDataflow(ComponentFileType.instance,
					ident);

			final GraphController gc = graphControllerMap.get(d);
			invokeLater(new Runnable() {
				@Override
				public void run() {
					SVGGraph g = (SVGGraph) gc.getGraph();
					g.setFillColor(Color.RED);
					gc.redraw();
				}
			});
		} catch (OpenException e) {
			logger.error(e);
		}
	}

	public void setSelection(ComponentActivity selection) {
		this.selection = selection;
	}

}
