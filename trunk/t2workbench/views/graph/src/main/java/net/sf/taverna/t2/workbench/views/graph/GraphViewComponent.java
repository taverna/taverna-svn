package net.sf.taverna.t2.workbench.views.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphComponent;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphModelFactory;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

public class GraphViewComponent extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(GraphViewComponent.class);

	private GraphController graphController;
	
	private Map<Dataflow, GraphController> graphControllerMap = new HashMap<Dataflow, GraphController>();
	
	private Dataflow dataflow;
	
	private Component currentComponent;

	public GraphViewComponent() {
		super(new BorderLayout());
		
		JToolBar toolBar = new JToolBar();
//		Action resetDiagramAction = currentComponent.new ResetTransformAction();
//		resetDiagramAction.putValue(Action.NAME, "Reset Diagram");
//		toolBar.add(resetDiagramAction);
//		Action zoomInAction = currentComponent.new ZoomAction(1.2);
//		zoomInAction.putValue(Action.NAME, "Zoom In");
//		toolBar.add(zoomInAction);
//		Action zoomOutAction = currentComponent.new ZoomAction(1/1.2);
//		zoomOutAction.putValue(Action.NAME, "Zoom Out");
//		toolBar.add(zoomOutAction);

		toolBar.add(new AbstractAction("No Ports") {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.NONE);
				graphController.redraw();
				graphController.resetSelection();
			}
			
		});
		
		toolBar.add(new AbstractAction("All Ports") {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.ALL);
				graphController.redraw();
				graphController.resetSelection();
			}
			
		});
		
		toolBar.add(new AbstractAction("Blobs") {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.BLOB);
				graphController.redraw();
				graphController.resetSelection();
			}
			
		});
		
		add(toolBar, BorderLayout.NORTH);
		
		
		ModelMap.getInstance().addObserver(new Observer<ModelMap.ModelMapEvent>() {
			public void notify(Observable<ModelMapEvent> sender, ModelMapEvent message) {
				if (message.getModelName().equals(ModelMapConstants.CURRENT_DATAFLOW)) {
					if (message.getNewModel() instanceof Dataflow) {
						setDataflow((Dataflow) message.getNewModel());
					}
				}
			}
		});
		
		EditManager.getInstance().addObserver(new Observer<EditManagerEvent>() {
			public void notify(Observable<EditManagerEvent> sender,
					EditManagerEvent message) throws Exception {
				if (message instanceof AbstractDataflowEditEvent) {
					AbstractDataflowEditEvent dataflowEditEvent = (AbstractDataflowEditEvent) message;
					if (dataflowEditEvent.getDataFlow() == dataflow ) {
						graphController.redraw();
					}
					
				}
			}
		});
		
		setTransferHandler(new GraphViewTransferHandler(this));
	}
	
//	/**
//	 * Sets the Dataflow to display in the graph view.
//	 * 
//	 * @param dataflow
//	 */
//	public void setDataflow(Dataflow dataflow) {
//		this.dataflow = dataflow;
//		if (!graphControllerMap.containsKey(dataflow)) {
//			GraphController graphController = new GraphController(dataflow, new SVGGraphModelFactory(), svgGraphComponent);
//			graphControllerMap.put(dataflow, graphController);
//		}
//		graphController = graphControllerMap.get(dataflow);
//		svgGraphComponent.setGraphController(graphController);
//	}
	
	/**
	 * Sets the Dataflow to display in the graph view.
	 * 
	 * @param dataflow
	 */
	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
		if (!graphControllerMap.containsKey(dataflow)) {
			GraphController graphController = new SVGGraphController(dataflow, this);
			graphControllerMap.put(dataflow, graphController);
		}
		graphController = graphControllerMap.get(dataflow);
		graphController.redraw();
		
		if (currentComponent != null) {
			remove(currentComponent);
		}
		currentComponent = graphController.getComponent();
		add(currentComponent, BorderLayout.CENTER);
		revalidate();
	}
	
	/**
	 * Returns the dataflow.
	 *
	 * @return the dataflow
	 */
	public Dataflow getDataflow() {
		return dataflow;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("raven.eclipse", "true");
		System.setProperty("taverna.dotlocation", "/Applications/Taverna-1.7.1.app/Contents/MacOS/dot");

		setUpRavenRepository();
		GraphViewComponent graphView = new GraphViewComponent();

		Dataflow dataflow = WorkflowModelTranslator.doTranslation(loadScufl("nested_iteration.xml"));

		graphView.setDataflow(dataflow);
		
		JFrame frame = new JFrame();
		frame.add(graphView);
		frame.setPreferredSize(new Dimension(600, 800));
		frame.pack();
		frame.setVisible(true);

	}

	protected static void setUpRavenRepository() throws IOException {
		File tmpDir = File.createTempFile("taverna", "raven");
		tmpDir.delete();
		tmpDir.mkdir();
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		TavernaSPIRegistry.setRepository(tempRepository);
	}

	protected static ScuflModel loadScufl(String resourceName)
			throws UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			IOException {
		ScuflModel model = new ScuflModel();
		InputStream inStream = GraphViewComponent.class.getResourceAsStream("/"+resourceName);
		XScuflParser.populate(inStream,model,null);
		return model;
	}

	@Override
	public String getName() {
		return "Graph View Component";
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
