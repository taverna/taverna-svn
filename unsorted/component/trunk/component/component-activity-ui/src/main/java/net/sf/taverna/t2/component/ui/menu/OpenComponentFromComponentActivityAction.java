/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFileType;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.menu.component.OpenWorkflowFromComponentAction;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraph;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author alanrw
 *
 */
public class OpenComponentFromComponentActivityAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(OpenComponentFromComponentActivityAction.class);

	private static final FileManager fileManager = FileManager.getInstance();
	
	private static ComponentFileType fileType = new ComponentFileType();
	
	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();

	public OpenComponentFromComponentActivityAction() {
		super("Open component...", ComponentServiceIcon.getIcon());
	}
	/**
	 * 
	 */
	private ComponentActivity selection;

	@Override
	public void actionPerformed(ActionEvent ev) {
		
		ComponentVersionIdentification ident = selection.getConfiguration();
		
		try {
			Dataflow d = fileManager.openDataflow(new ComponentFileType(), ident);
			
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

	public void setSelection(ComponentActivity selection) {
		this.selection = selection;
	}

}
