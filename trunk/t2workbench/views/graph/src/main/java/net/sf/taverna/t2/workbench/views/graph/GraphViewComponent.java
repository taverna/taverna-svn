package net.sf.taverna.t2.workbench.views.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
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
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
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

	private SVGGraphController graphController;
	
	private Map<Dataflow, SVGGraphController> graphControllerMap = new HashMap<Dataflow, SVGGraphController>();
	
	private Dataflow dataflow;
	
	private JSVGCanvas svgCanvas;

	private JButton resetDiagramButton;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JToggleButton noPorts;
	private JToggleButton allPorts;
	private JToggleButton blobs;
	private JToggleButton vertical;
	private JToggleButton horizontal;
	
	
	public GraphViewComponent() {
		super(new BorderLayout());
		
		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				graphController.setUpdateManager(svgCanvas.getUpdateManager());
			}
		});
		add(svgCanvas, BorderLayout.CENTER);
		
		add(setupToolbar(), BorderLayout.NORTH);
				
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
						svgCanvas.setDocument(graphController.generateSVGDocument());
						revalidate();
					}
					
				}
			}
		});
		
		setTransferHandler(new GraphViewTransferHandler(this));
		
	}

	private JToolBar setupToolbar() {
		JToolBar toolBar = new JToolBar();
		
		resetDiagramButton = new JButton();
		zoomInButton = new JButton();
		zoomOutButton = new JButton();
		
		Action resetDiagramAction = svgCanvas.new ResetTransformAction();
		resetDiagramAction.putValue(Action.NAME, "Reset Diagram");
		resetDiagramButton.setAction(resetDiagramAction);
		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		zoomInAction.putValue(Action.NAME, "Zoom In");
		zoomInButton.setAction(zoomInAction);
		Action zoomOutAction = svgCanvas.new ZoomAction(1/1.2);
		zoomOutAction.putValue(Action.NAME, "Zoom Out");
		zoomOutButton.setAction(zoomOutAction);

		toolBar.add(resetDiagramButton);
		toolBar.add(zoomInButton);
		toolBar.add(zoomOutButton);

		toolBar.addSeparator();
		
		ButtonGroup nodeTypeGroup = new ButtonGroup();

		noPorts = new JToggleButton();
		allPorts = new JToggleButton();
		blobs = new JToggleButton();
		nodeTypeGroup.add(noPorts);
		nodeTypeGroup.add(allPorts);
		nodeTypeGroup.add(blobs);
		noPorts.setSelected(true);
		
		noPorts.setAction(new AbstractAction("No Ports") {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.NONE);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		
		allPorts.setAction(new AbstractAction("All Ports") {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.ALL);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		
		blobs.setAction(new AbstractAction("Blobs") {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.BLOB);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		
		toolBar.add(noPorts);
		toolBar.add(allPorts);
		toolBar.add(blobs);
		
		toolBar.addSeparator();
		
		ButtonGroup alignmentGroup = new ButtonGroup();

		vertical = new JToggleButton();
		horizontal = new JToggleButton();
		alignmentGroup.add(vertical);
		alignmentGroup.add(horizontal);
		vertical.setSelected(true);

		vertical.setAction(new AbstractAction("Vertical") {

			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Vertical");
				graphController.setAlignment(Alignment.VERTICAL);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		horizontal.setAction(new AbstractAction("Horizontal") {

			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Horizontal");
				graphController.setAlignment(Alignment.HORIZONTAL);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		
		toolBar.add(vertical);
		toolBar.add(horizontal);
		return toolBar;
	}
	
	/**
	 * Sets the Dataflow to display in the graph view.
	 * 
	 * @param dataflow
	 */
	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
		if (!graphControllerMap.containsKey(dataflow)) {
			SVGGraphController graphController = new SVGGraphController(dataflow, this);
			graphControllerMap.put(dataflow, graphController);
		}
		graphController = graphControllerMap.get(dataflow);
		
		if (graphController.getPortStyle().equals(PortStyle.NONE)) {
			noPorts.setSelected(true);
		} else if (graphController.getPortStyle().equals(PortStyle.ALL)) {
			allPorts.setSelected(true);
		} else {
			blobs.setSelected(true);
		}
		if (graphController.getAlignment().equals(Alignment.HORIZONTAL)) {
			horizontal.setSelected(true);
		} else {
			vertical.setSelected(true);
		}
		
		svgCanvas.setDocument(graphController.generateSVGDocument());
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
