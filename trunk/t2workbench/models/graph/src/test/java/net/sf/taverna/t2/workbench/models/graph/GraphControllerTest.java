package net.sf.taverna.t2.workbench.models.graph;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import net.sf.taverna.t2.activities.testutils.TranslatorTestHelper;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GraphControllerTest extends TranslatorTestHelper {

	Dataflow dataflow;
	
	GraphController graphController;
		
	@Before
	public void setUp() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		dataflow = WorkflowModelTranslator.doTranslation(loadScufl("nested_iteration.xml"));
		graphController = new GraphController(dataflow, new GraphModelFactory() {

			public GraphEdge createGraphEdge() {
				return new GraphEdge();
			}

			public Graph createGraphModel() {
				return new Graph();
			}

			public GraphNode createGraphNode() {
				return new GraphNode();
			}
			
		});
		graphController.setPortStyle(PortStyle.NONE);
	}

	@Test
	@Ignore
	public void testGenerateGraph() throws IOException, InterruptedException {
		Graph graph = graphController.generateGraph();
		assertEquals(5, graph.getNodes().size());
		assertEquals(9, graph.getEdges().size());
		assertEquals(1, graph.getSubgraphs().size());		
	}

}
