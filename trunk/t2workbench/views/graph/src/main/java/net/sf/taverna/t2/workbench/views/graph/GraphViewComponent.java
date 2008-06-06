package net.sf.taverna.t2.workbench.views.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

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

public class GraphViewComponent extends JPanel implements UIComponentSPI, Observer<ModelMap.ModelMapEvent> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(GraphViewComponent.class);

	private GraphController graphController = new GraphController();

	private SVGGraph svgDiagram = new SVGGraph(graphController);;

	public GraphViewComponent() {
		super(new BorderLayout());
		System.setProperty("taverna.dotlocation", "/Applications/Taverna-1.7.1.app/Contents/MacOS/dot");
		add(svgDiagram, BorderLayout.CENTER);
		graphController.getSelectionModel().addObserver(svgDiagram);
		ModelMap.getInstance().addObserver(this);
	}
	
	public void setDataflow(Dataflow dataflow) {
		Graph graph = graphController.generateGraph(dataflow);
		svgDiagram.setGraph(graph);
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

	public void notify(Observable<ModelMapEvent> sender, ModelMapEvent message)
			throws Exception {
		if (message.modelName.equals(ModelMapConstants.CURRENT_DATAFLOW)) {
			if (message.newModel instanceof Dataflow) {
				setDataflow((Dataflow) message.newModel);
			}
		}
	}

}
