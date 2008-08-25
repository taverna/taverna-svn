package net.sf.taverna.t2.workbench.views.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.ui.menu.impl.ContextMenuFactory;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

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
	private JToggleButton expandNested;
	
	public GraphViewComponent() {
		super(new BorderLayout());
		setBorder(new EmptyBorder(0,10,10,0));
		
		svgCanvas = new JSVGCanvas();
		svgCanvas.setBorder(new EtchedBorder());
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				graphController.setUpdateManager(svgCanvas.getUpdateManager());
			}
		});
		svgCanvas.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && dataflow != null) {
					ContextMenuFactory.getContextMenu(dataflow, dataflow, svgCanvas).show(svgCanvas, e.getX(), e.getY());
				}
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
		resetDiagramButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		zoomInButton = new JButton();
		zoomInButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		zoomOutButton = new JButton();
		zoomOutButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		
		Action resetDiagramAction = svgCanvas.new ResetTransformAction();
		resetDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Reset Diagram");
		resetDiagramAction.putValue(Action.SMALL_ICON, WorkbenchIcons.refreshIcon);
		resetDiagramButton.setAction(resetDiagramAction);

		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		zoomInAction.putValue(Action.SHORT_DESCRIPTION, "Zoom In");
		zoomInAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomInIcon);
		zoomInButton.setAction(zoomInAction);

		Action zoomOutAction = svgCanvas.new ZoomAction(1/1.2);
		zoomOutAction.putValue(Action.SHORT_DESCRIPTION, "Zoom Out");
		zoomOutAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomOutIcon);
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
		
		noPorts.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.NONE);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		noPorts.getAction().putValue(Action.SHORT_DESCRIPTION, "Display no processor ports");
		noPorts.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.noportIcon);
		noPorts.setFocusPainted(false);		
		
		allPorts.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.ALL);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		allPorts.getAction().putValue(Action.SHORT_DESCRIPTION, "Display all processor ports");
		allPorts.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.allportIcon);
		allPorts.setFocusPainted(false);
		
		blobs.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(GraphController.PortStyle.BLOB);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		blobs.getAction().putValue(Action.SHORT_DESCRIPTION, "Display processors as circles");
		blobs.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.blobIcon);
		blobs.setFocusPainted(false);
		
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

		vertical.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setAlignment(Alignment.VERTICAL);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		vertical.getAction().putValue(Action.SHORT_DESCRIPTION, "Align processors vertically");
		vertical.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.verticalIcon);
		vertical.setFocusPainted(false);
		
		horizontal.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setAlignment(Alignment.HORIZONTAL);
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		horizontal.getAction().putValue(Action.SHORT_DESCRIPTION, "Align processors horizontally");
		horizontal.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.horizontalIcon);
		horizontal.setFocusPainted(false);
		
		toolBar.add(vertical);
		toolBar.add(horizontal);
		
		toolBar.addSeparator();

		expandNested = new JToggleButton();
		expandNested.setSelected(true);

		expandNested.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setExpandNestedDataflows(!graphController.isExpandNestedDataflows());
				svgCanvas.setDocument(graphController.generateSVGDocument());
				revalidate();
			}
			
		});
		expandNested.getAction().putValue(Action.SHORT_DESCRIPTION, "Expand Nested Workflows");
		expandNested.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.expandNestedIcon);
		expandNested.setFocusPainted(false);
		
		toolBar.add(expandNested);
		
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
			graphController.setDataflowSelectionModel(DataflowSelectionManager.getInstance().getDataflowSelectionModel(dataflow));
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

		GraphViewComponent graphView = new GraphViewComponent();

		T2DataflowOpener t2DataflowOpener = new T2DataflowOpener();
		InputStream stream = GraphViewComponent.class.getResourceAsStream("/nested_iteration.t2flow");
		Dataflow dataflow = t2DataflowOpener.openDataflow(new T2FlowFileType(), stream).getDataflow();

		graphView.setDataflow(dataflow);
		
		JFrame frame = new JFrame();
		frame.add(graphView);
		frame.setPreferredSize(new Dimension(600, 800));
		frame.pack();
		frame.setVisible(true);

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
