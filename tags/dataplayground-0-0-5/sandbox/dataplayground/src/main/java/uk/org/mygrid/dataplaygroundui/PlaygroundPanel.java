package uk.org.mygrid.dataplaygroundui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;
import org.biomoby.client.taverna.plugin.BiomobyProcessor;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Element;

import uk.org.mygrid.dataplayground.PlaygroundDataObject;
import uk.org.mygrid.dataplayground.PlaygroundDataThing;
import uk.org.mygrid.dataplayground.PlaygroundObject;
import uk.org.mygrid.dataplayground.PlaygroundObjectModel;
import uk.org.mygrid.dataplayground.PlaygroundPortObject;
import uk.org.mygrid.dataplayground.PlaygroundProcessorObject;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.DefaultGraphLabelRenderer;
import edu.uci.ics.jung.visualization.DefaultSettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;

public class PlaygroundPanel extends JPanel implements UIComponentSPI,
		WorkflowModelViewSPI, DropTargetListener {

	private static PlaygroundPanel instance;

	public static UIComponentSPI getInstance() {
		if (instance == null) {
			instance = new PlaygroundPanel(new PlaygroundObjectModel());
		}
		return instance;
	}

	private VisualizationViewer vv;

	private AbstractLayout layout;

	private PluggableRenderer pr;

	private PluggableGraphMouse pgm;

	private DefaultSettableVertexLocationFunction vertexLocations;

	private PlaygroundObjectModel playgroundModel;

	private PlaygroundInputPanel playgroundInputPanel;

	private PlaygroundRendererPanel playgroundRendererPanel;

	private boolean recording;

	private JToolBar toolbar;

	private ScuflModel tavernaModel;

	private StopAction stopAction;

	private RecordAction recordAction;

	public PlaygroundPanel(PlaygroundObjectModel playgroundModel) {
		init(playgroundModel);
	}

	public void addData(Point2D point, BiomobyObjectProcessor d) {

		Vertex v = playgroundModel.addDataObject(d);
		vertexLocations.setLocation(v, vv.inverseTransform(point));
		ArrayList<PlaygroundDataObject> components = ((PlaygroundDataObject) v)
				.getDataComponents();
		((FRLayout) layout).update();
		vv.repaint();

		for (Iterator iterator = vv.getGraphLayout().getGraph().getVertices()
				.iterator(); iterator.hasNext();) {
			layout.lockVertex((Vertex) iterator.next());
		}

		addComponents((PlaygroundDataObject) v, components);

		for (Iterator iterator = vv.getGraphLayout().getGraph().getVertices()
				.iterator(); iterator.hasNext();) {
			layout.unlockVertex((Vertex) iterator.next());
		}

		((FRLayout) layout).update();
		vv.repaint();

		this.repaint();

	}

	public void addDataObject(Point2D point, PlaygroundDataObject v) {

		playgroundModel.addDataObject(v);
		vertexLocations.setLocation(v, vv.inverseTransform(point));
		ArrayList<PlaygroundDataObject> components = ((PlaygroundDataObject) v)
				.getDataComponents();
		((FRLayout) layout).update();
		vv.repaint();

		for (Iterator iterator = vv.getGraphLayout().getGraph().getVertices()
				.iterator(); iterator.hasNext();) {
			layout.lockVertex((Vertex) iterator.next());
		}

		addComponents((PlaygroundDataObject) v, components);

		for (Iterator iterator = vv.getGraphLayout().getGraph().getVertices()
				.iterator(); iterator.hasNext();) {
			layout.unlockVertex((Vertex) iterator.next());
		}

		((FRLayout) layout).update();
		vv.repaint();

		this.repaint();

	}

	public void addProcessor(Point2D point, BiomobyProcessor p) {

		Vertex v = playgroundModel.addProcessor(p);
		vertexLocations.setLocation(v, vv.inverseTransform(point));

		addPorts((PlaygroundObject) v);

		((FRLayout) layout).update();

		vv.repaint();

		this.repaint();

	}

	public void addResults(ArrayList<PlaygroundObject> results, Point2D point) {

		for (Iterator i = results.iterator(); i.hasNext();) {

			PlaygroundDataObject newObject = (PlaygroundDataObject) i.next();
			addDataObject(point, newObject);

		}

	}

	public void attachToModel(ScuflModel arg0) {
		System.out.println("Attaching to model!");
		tavernaModel = arg0;

	}

	public void detachFromModel() {
		// TODO Auto-generated method stub

	}

	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void drop(DropTargetDropEvent e) {
		try {
			Point a = vv.getMousePosition(true);
			DataFlavor f = SpecFragmentTransferable.factorySpecFragmentFlavor;
			Transferable t = e.getTransferable();

			if (e.isDataFlavorSupported(f)) {

				// We Have something of type factorySpecFragmentFlavor;
				FactorySpecFragment fsf = (FactorySpecFragment) t
						.getTransferData(f);

				Point point = e.getLocation();
				int x = (int) point.getX();
				int y = (int) point.getY();

				ScuflModel scuflModel = new ScuflModel();
				String validName = scuflModel.getValidProcessorName(fsf
						.getFactoryNodeName());
				Element wrapperElement = new Element("wrapper");
				wrapperElement.addContent(fsf.getElement());

				Processor newProcessor = ProcessorHelper.loadProcessorFromXML(
						wrapperElement, scuflModel, validName);

				if (newProcessor instanceof BiomobyProcessor) {

					addProcessor(a, (BiomobyProcessor) newProcessor);

				} else if (newProcessor instanceof BiomobyObjectProcessor) {

					addData(a, (BiomobyObjectProcessor) newProcessor);
				}
			}
			e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			// System.out.print("Accepted. Number of Verticies = "
			// + playgroundModel.getGraph().numVertices());
		} catch (Exception ex) {
			ex.printStackTrace();
			e.rejectDrop();
		}
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub

	}

	public ImageIcon getIcon() {
		URL iconURL = PlaygroundPanel.class.getResource("user-desktop.gif");
		// System.out.println("url = " + iconURL);
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Data Playground";
	}

	public PlaygroundObjectModel getPlaygroundModel() {
		return playgroundModel;
	}

	public boolean isRecording() {
		return recording;
	}

	public void onDisplay() {

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	private void init(PlaygroundObjectModel playgroundModel) {
		instance = this;
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		setMaximumSize(new Dimension(400, 400));
		playgroundRendererPanel = (PlaygroundRendererPanel) PlaygroundRendererPanel
				.getInstance();

		// create the toolbar with the record and stop actions
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 18));
		toolbar.setBorderPainted(true);
		toolbar.add(new JLabel("Data Playground"));
		toolbar.addSeparator();
		recordAction = new RecordAction();
		toolbar.add(recordAction);
		toolbar.addSeparator();
		stopAction = new StopAction(recordAction);
		toolbar.add(stopAction);

		// create a new layout with the graph from the playgroundModel , create
		// the new
		// Pluggable Renderer for this graph and then create the
		// VisualisationViewer using
		// these and set the PlaygroundPanels content to this.

		this.playgroundModel = playgroundModel;
		vertexLocations = new DefaultSettableVertexLocationFunction();
		layout = new FRLayout(this.playgroundModel.getGraph());
		layout.initialize(new Dimension(800, 400), vertexLocations);

		pr = new PluggableRenderer();

		vv = new VisualizationViewer(layout, pr);
		vv.setSize(900, 600);
		vv.setPickSupport(new PlaygroundPickSupport());

		PluggableGraphMouse graphMouse = new PluggableGraphMouse();
		PlaygroundGraphMousePlugin playgroundGraphMousePlugin = new PlaygroundGraphMousePlugin();
		graphMouse.add(playgroundGraphMousePlugin);
		graphMouse.add(new PickingGraphMousePlugin());
		graphMouse.add(new PlaygroundPopupGraphMousePlugin(vertexLocations,
				this));

		vv.addKeyListener(playgroundGraphMousePlugin);
		vv.setGraphMouse(graphMouse);
		vv.setBackground(Color.white);
		/*
		 * vv.addPostRenderPaintable(new VisualizationViewer.Paintable(){
		 * 
		 * 
		 * public void paint(Graphics g) { } public boolean useTransform() {
		 * return false; } });
		 */
		PickedState ps = vv.getPickedState();
		pr.setEdgePaintFunction(new PickableEdgePaintFunction(ps, Color.black,
				Color.cyan));
		pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());
		pr.setVertexPaintFunction(new PlaygroundVertexPaintFunction(ps));
		pr.setGraphLabelRenderer(new DefaultGraphLabelRenderer(Color.cyan,
				Color.cyan));
		pr.setVertexIncludePredicate(new PlaygroundVertexPredicate());
		pr.setVertexShapeFunction(new PlaygroundVertexShapeSizeFunction());

		pr.setVertexStringer(new VertexStringer() {

			public String getLabel(ArchetypeVertex v) {
				return v.toString();
			}
		});

		vv.setToolTipFunction(new DefaultToolTipFunction());

		// GraphZoomScrollPane graphZoomScrollPane = new
		// GraphZoomScrollPane(vv);
		// graphZoomScrollPane.setSize(this.getSize());
		this.setLayout(new BorderLayout());
		this.add(toolbar, BorderLayout.PAGE_START);
		this.add(vv, BorderLayout.CENTER);
	}

	// recursive method to add components of BioMobyDataobjects
	protected void addComponents(PlaygroundDataObject parent,
			ArrayList<PlaygroundDataObject> components) {

		Iterator iterator = components.iterator();

		while (iterator.hasNext()) {

			PlaygroundDataObject pdo = (PlaygroundDataObject) iterator.next();
			playgroundModel
					.addDataComponent((PlaygroundDataObject) parent, pdo);
			System.out.println("Component  " + pdo);
			Point2D point = vertexLocations.getLocation(parent);
			vertexLocations.setLocation(pdo, new Point2D.Double(point.getX()
					+ ((int) (Math.random() * 80) - 40), point.getY()
					+ ((int) (Math.random() * 80) - 40)));
			addComponents(pdo, pdo.getDataComponents());

		}

		// if the data type is Object we need to expose the namespace and id
		// ports
		// if(parent.getDataType().equalsIgnoreCase("Object")){

		ArrayList<PlaygroundPortObject> ports = new ArrayList(parent
				.getInputPortObjects().values());
		iterator = ports.iterator();

		while (iterator.hasNext()) {

			PlaygroundPortObject ppo = (PlaygroundPortObject) iterator.next();
			if (!ppo.isInvisible()) {
				playgroundModel.addPort((PlaygroundDataObject) parent, ppo);

				Point2D point = vertexLocations.getLocation(parent);
				vertexLocations.setLocation(ppo, new Point2D.Double(point
						.getX()
						+ ((int) (Math.random() * 40) - 20), point.getY()
						+ ((int) (Math.random() * 40) - 20)));

				if (ppo.getMappedObject() != null) {

					playgroundModel.addPortDataThing(ppo,
							(PlaygroundDataThing) ppo.getMappedObject());
					playgroundRendererPanel.add((PlaygroundDataThing) ppo
							.getMappedObject());
					Point2D p = vertexLocations.getLocation(ppo);
					vertexLocations.setLocation(ppo.getMappedObject(),
							new Point2D.Double(p.getX()
									+ ((int) (Math.random() * 70) - 35), p
									.getY()
									+ ((int) (Math.random() * 70) - 35)));

				}
			}

		}

		// }if object

	}

	protected void addPorts(PlaygroundObject parent) {

		ArrayList<PlaygroundPortObject> ports = new ArrayList<PlaygroundPortObject>();
		if (parent instanceof PlaygroundProcessorObject) {
			ports = ((PlaygroundProcessorObject) parent).getInputPortObjects();
		}
		if (parent instanceof PlaygroundDataObject) {
			ports = new ArrayList(((PlaygroundDataObject) parent)
					.getInputPortObjects().values());
		}

		Iterator iterator = ports.iterator();
		while (iterator.hasNext()) {

			PlaygroundPortObject ppo = (PlaygroundPortObject) iterator.next();
			if (!ppo.isInvisible()) {
				playgroundModel.addPort(parent, ppo);
				System.out.println("Component  " + ppo);
				Point2D point = vertexLocations.getLocation(parent);
				vertexLocations.setLocation(ppo, new Point2D.Double(point
						.getX()
						+ ((int) (Math.random() * 40) - 20), point.getY()
						+ ((int) (Math.random() * 40) - 20)));
			}
		}

	}

	public class RecordAction extends AbstractAction {

		ImageIcon off;
		ImageIcon on;

		public RecordAction() {

			URL iconOffURL = PlaygroundUIComponentFactory.class
					.getResource("media-record-small-off.gif");
			URL iconOnURL = PlaygroundUIComponentFactory.class
					.getResource("media-record-small.gif");

			off = new ImageIcon(iconOffURL);
			on = new ImageIcon(iconOnURL);

			putValue(SMALL_ICON, off);
			putValue(NAME, "Record");
			putValue(SHORT_DESCRIPTION, "Record your actions");

		}

		public void actionPerformed(ActionEvent e) {
			putValue(SMALL_ICON, on);

			recording = true;

		}

		public void stop() {
			putValue(SMALL_ICON, off);
		}

	}

	public class StopAction extends AbstractAction {

		RecordAction r;

		public StopAction(RecordAction r) {

			URL iconURL = PlaygroundUIComponentFactory.class
					.getResource("media-playback-stop-small.gif");

			this.r = r;
			putValue(SMALL_ICON, new ImageIcon(iconURL));
			putValue(NAME, "Stop");
			putValue(SHORT_DESCRIPTION, "Stop recording");

		}

		public void actionPerformed(ActionEvent e) {
			System.out.println("Stopping");
			recording = false;
			r.stop();
			try {
				System.out.println("cloning model");
				// tavernaModel = playgroundModel.getRecordedWorkflow().clone();

				XScuflView v = new XScuflView(playgroundModel
						.getRecordedWorkflow());
				System.out.println(v.getXMLText());
				XScuflParser.populate(v.getXMLText(), tavernaModel, null);

			} catch (Exception e1) {

				e1.printStackTrace();
			}

			playgroundModel.stopRecording();

		}
	}
}
