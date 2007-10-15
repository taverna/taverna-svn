package uk.org.mygrid.dataplayground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;
import org.biomoby.client.taverna.plugin.BiomobyProcessor;
import org.biomoby.client.taverna.plugin.MobyParseDatatypeProcessor;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.scufl.view.XScuflView;

import com.sun.media.sound.DataPusher;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

import edu.uci.ics.jung.graph.ArchetypeGraph;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.random.generators.SimpleRandomGenerator;
import edu.uci.ics.jung.utils.TestGraphs;

public class PlaygroundObjectModel {

	private Graph graph;

	// When deleting nodes simply move them to another graph maintaining their
	// structure allowing
	// for easy undo
	private Graph recycleBin = new SparseGraph();

	// holds the workflow currently being recorded
	private static ScuflModel recordedWorkflow = new ScuflModel();

	// Holds a map between the playgroundObjects and the ports they map to in
	// the recorded workflow.
	private static Map<PlaygroundDataObject, Port> recordedMap = new HashMap<PlaygroundDataObject, Port>();

	private static Map<PlaygroundDataObject, MobyParseDatatypeProcessor> recordedParserMap = new HashMap<PlaygroundDataObject, MobyParseDatatypeProcessor>();

	private static Map<PlaygroundPortObject, Port> recordedParserPortMap = new HashMap<PlaygroundPortObject, Port>();

	public PlaygroundObjectModel() {
		graph = new SparseGraph();

	}

	/**
	 * Creates new Vertex for the specified processor and returns the Vertex
	 * 
	 * @param newProcessor
	 */
	public Vertex addProcessor(BiomobyProcessor newProcessor) {

		Vertex v = graph.addVertex(new PlaygroundProcessorObject(newProcessor));
		return v;

	}

	public Vertex addDataObject(BiomobyObjectProcessor newProcessor) {
		Vertex v = graph.addVertex(new PlaygroundDataObject(newProcessor));
		return v;
	}

	public Vertex addDataObject(PlaygroundDataObject pdo) {
		Vertex v = graph.addVertex(pdo);
		return v;
	}

	public void addDataComponent(PlaygroundDataObject parent,
			PlaygroundDataObject component) {

		graph.addVertex(component);
		Edge e = new DirectedSparseEdge(component, parent);
		graph.addEdge(e);

	}

	public Vertex addDataThing(PlaygroundDataThing pdt) {

		Vertex v = graph.addVertex(pdt);
		return v;

	}

	public void mapObjects(PlaygroundObject start, PlaygroundObject end) {

		graph.addEdge(new DirectedSparseEdge(start, end));

	}

	public Graph getGraph() {
		return graph;
	}

	public void addPort(PlaygroundObject parent, PlaygroundPortObject port) {

		graph.addVertex(port);
		Edge e = new DirectedSparseEdge(port, parent);
		graph.addEdge(e);

	}

	public void addPortDataThing(PlaygroundPortObject parent,
			PlaygroundDataThing thing) {
		graph.addVertex(thing);
		Edge e = new DirectedSparseEdge(thing, parent);
		graph.addEdge(e);

	}

	// Handels the Creation of the workflow to produce the new Data Object and
	// if we are in recording mode also constructs the recordedWorkflow in
	// parallel

	@SuppressWarnings("unchecked")
	public static synchronized ArrayList<PlaygroundObject> run(
			PlaygroundProcessorObject processorObject, boolean recording) {

		// the ScuflModel for this workflow
		ScuflModel model = new ScuflModel();
		// A Map generated with the inputs(DataThings) to this workflow
		HashMap<String, DataThing> inputMap = new HashMap<String, DataThing>();
		// A Map to aid in construction of the resulting PlaygroundObjects by
		// mapping which output ports
		// correspond to which dataObjects
		HashMap<PlaygroundObject, ArrayList<String>> resultMap = new HashMap<PlaygroundObject, ArrayList<String>>();

		try {

			model.addProcessor(processorObject.getProcessor());

			if (recording) {
				recordedWorkflow.addProcessor(processorObject.getProcessor());
			}

			/**
			 * For each of the output ports on the processor create a Moby
			 * Parser and map them together then create outputs for each of the
			 * parsers outputs create a playgrounddataObject of the same type as
			 * the output from the processor and add to a map that these outputs
			 * map to this processor?
			 */
			ArrayList<PlaygroundPortObject> playgroundPortObjects = processorObject
					.getOutputPortObjects();

			for (Iterator i = playgroundPortObjects.iterator(); i.hasNext();) {

				Port p = ((PlaygroundPortObject) i.next()).getPort();

				addParser(p, model, (BiomobyProcessor) processorObject
						.getProcessor(), resultMap, recording);

				if (recording) {
					// create an output port for the recorded workflow mapped to
					// the output ports from

					if (!(p.getName().contains("Collection") && !p
							.getName().contains("As Simples"))) {

						InputPort outPort = new InputPort(recordedWorkflow
								.getWorkflowSinkProcessor(), p.getName());
						recordedWorkflow.getWorkflowSinkProcessor().addPort(
								outPort);
						recordedWorkflow.addDataConstraint(new DataConstraint(
								recordedWorkflow, p, outPort));
					}
				}

			}

			/**
			 * get each of the processors inputPortObjects and get the
			 * playgroundDataObject mapped to it (if there is one) if we are
			 * recording check if the Object is mapped to a port in the current
			 * recorded workflow , if so map that port to the input port else
			 * add the Moby data object to the workflow and create the inputs
			 */

			ArrayList<PlaygroundPortObject> inputPortObjects = processorObject
					.getInputPortObjects();

			for (Iterator i = inputPortObjects.iterator(); i.hasNext();) {

				PlaygroundPortObject portObject = (PlaygroundPortObject) i
						.next();
				Port port = portObject.getPort();
				PlaygroundObject playgroundObject = portObject
						.getMappedObject();

				if (playgroundObject instanceof PlaygroundDataObject) {

					PlaygroundDataObject dataObject = (PlaygroundDataObject) playgroundObject;
					if (model.getValidProcessorName(
							dataObject.getProcessor().getName()).equals(
							dataObject.getProcessor().getName())) {
						System.out.println("adding object "
								+ dataObject.getProcessor().getName());
						model.addProcessor(dataObject.getProcessor());
						System.out.println("added object "
								+ dataObject.getProcessor().getName());
						addComponentstoWorkflow(model, dataObject, inputMap);
					}

					model.addDataConstraint(new DataConstraint(model,
							dataObject.getProcessor().locatePort("mobyData"),
							port));

					// if we are recording either generate the moby object and
					// inputs or map the appropriate ports and remove any
					// outputs from these ports
					if (recording) {

						Port mappedPort = recordedMap.get(dataObject);

						if (mappedPort != null) {

							// then this object is produced by a processor in
							// the workflow , remove the outputs from this port
							// in the workflow and map the ports

							DataConstraint[] constraints = recordedWorkflow
									.getDataConstraints();

							for (int x = 0; x < constraints.length; x++) {
								if (constraints[x].getSource() == port) {
									recordedWorkflow.getWorkflowSinkProcessor()
											.removePort(
													constraints[x].getSink());
									recordedWorkflow
											.destroyDataConstraint(constraints[x]);
								}
							}

							recordedWorkflow
									.addDataConstraint(new DataConstraint(
											recordedWorkflow, mappedPort, port));

						} else {

							if (recordedWorkflow
									.getValidProcessorName(
											dataObject.getProcessor().getName())
									.equals(dataObject.getProcessor().getName())) {
								// if this is true then we don't allready have
								// the processor in the workflow
								recordedWorkflow.addProcessor(dataObject
										.getProcessor());
								addComponentstoWorkflow(recordedWorkflow,
										dataObject, null);

							}

							recordedWorkflow
									.addDataConstraint(new DataConstraint(
											recordedWorkflow, dataObject
													.getProcessor().locatePort(
															"mobyData"), port));

						}

					}
				}

			}

			XScuflView v = new XScuflView(model);
			System.out.println("workflow of run");
			System.out.println(v.getXMLText());

			WorkflowLauncher Launcher = new WorkflowLauncher(model);

			HashMap workflowResultMap = (HashMap) Launcher.execute(inputMap);

			// Populate the PlaygroundObjects
			ArrayList<PlaygroundObject> playgroundObjects = new ArrayList<PlaygroundObject>();

			for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {

				PlaygroundDataObject pdo = (PlaygroundDataObject) i.next();
				playgroundObjects.add(pdo);
				Map<String, PlaygroundDataObject> componentsByArticleName = pdo
						.getAllComponents();

				ArrayList<String> portNames = resultMap.get(pdo);
				HashMap<String, PlaygroundPortObject> portObjects = (HashMap) pdo
						.getInputPortObjects();

				
				for (Iterator j = portNames.iterator(); j.hasNext();) {

					String portName = (String) j.next();
					System.out.println("Processing PortName " + portName);
					String portNameArticle = portName.substring(portName
							.lastIndexOf("_"));
					PlaygroundDataThing playgroundDataThing = new PlaygroundDataThing(
							(DataThing) workflowResultMap
									.get(portName), portName
									.substring(portName
											.lastIndexOf("_")));
					if (portNameArticle.contains(new StringBuffer("namespace"))) {

						PlaygroundPortObject portObject = (PlaygroundPortObject) portObjects
								.get("namespace");
						playgroundObjects.add(playgroundDataThing);
						portObject.setMappedObject(playgroundDataThing);

					} else if (portNameArticle.contains(new StringBuffer("id"))) {
						System.out.println("id detected");
						if (portName.indexOf("'") != -1) {
							// we are looking at a sub component
							// get the article name , get the
							// playgroundDataObject and set its ,mapped object
							// to the dataThing in the result map
							String halfway = portName.substring(0, portName
									.lastIndexOf("'"));
							String articleName = halfway.substring(halfway
									.lastIndexOf("'") + 1);
							componentsByArticleName
									.get(articleName)
									.getInputPortObjects()
									.get("id")
									.setMappedObject(
											playgroundDataThing);

						} else {
							// we are looking at top level object
							PlaygroundPortObject portObject = (PlaygroundPortObject) portObjects
									.get("id");
							portObject
									.setMappedObject(playgroundDataThing);
							playgroundObjects.add(playgroundDataThing);
						}

					} else if (portNameArticle.contains("ns")) {
						System.out.println("ns detected");

						if (portName.indexOf("'") != -1) {
							// we are looking at a sub component
							// get the article name , get the
							// playgroundDataObject and set its ,mapped object
							// to the dataThing in the result map
							String halfway = portName.substring(0, portName
									.lastIndexOf("'"));
							String articleName = halfway.substring(halfway
									.lastIndexOf("'") + 1);
							componentsByArticleName
									.get(articleName)
									.getInputPortObjects()
									.get("namespace")
									.setMappedObject(
											playgroundDataThing);

						}

					} else if (portNameArticle.contains(new StringBuffer(
							"value"))) {

						System.out.println("value detected");

						// we are looking at top level object
						PlaygroundPortObject portObject = (PlaygroundPortObject) portObjects
								.get("value");
						portObject.setMappedObject(playgroundDataThing);
					} else {
						System.out.println("default detected");
						// in the default case we are looking at the value port
						// of a subComponent or our top level object!

						if (portName.indexOf("'") != -1) {
							String halfway = portName.substring(0, portName
									.lastIndexOf("'"));
							String articleName = halfway.substring(halfway
									.lastIndexOf("'") + 1);
							System.out
									.println("article name =  " + articleName);
							componentsByArticleName
									.get(articleName)
									.getInputPortObjects()
									.get("value")
									.setMappedObject(
											playgroundDataThing);
						} else {

							PlaygroundPortObject portObject = (PlaygroundPortObject) portObjects
									.get("value");
							portObject
									.setMappedObject(playgroundDataThing);

						}

					}

				}// end for resultMap

			}
			//return playgroundObjects;
			return new ArrayList<PlaygroundObject>(resultMap.keySet());

		} catch (PortCreationException e) {
			e.printStackTrace();
		} catch (DuplicatePortNameException e) {
			e.printStackTrace();
		} catch (DataConstraintCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WorkflowSubmissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateProcessorNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	private static void addParser(Port p, ScuflModel model,
			BiomobyProcessor processor,
			Map<PlaygroundObject, ArrayList<String>> resultMap,
			boolean recording) throws ProcessorCreationException,
			DuplicateProcessorNameException, DuplicatePortNameException,
			PortCreationException, DataConstraintCreationException {

		String portName = p.getName();
		String datatype = portName.split("\\(")[0];
		String name = datatype;
		String workflowName = model.getValidProcessorName("Parse Moby Data("
				+ name + ")");
		String articlename = "";

		if (portName.contains(("Collection"))) {

			// we only want to consider the output from a processor that gives a
			// collection that returns it as "Simples"
			if (!portName.contains("As Simples"))
				return;

			articlename = portName.substring(portName.indexOf("'") + 1,
					portName.lastIndexOf("'"));
			System.out.println("article name = " + articlename);

		} else {
			articlename = portName.substring(portName.indexOf("(") + 1,
					portName.lastIndexOf(")"));
			System.out.println("article name = " + articlename);
		}

		// create the parser object for this dataType
		MobyParseDatatypeProcessor parser = new MobyParseDatatypeProcessor(
				model, workflowName, name, articlename,
				((BiomobyProcessor) processor).getMobyEndpoint());

		// add the parser to the model
		model.addProcessor(parser);

		System.out.println("Parser name = " + parser.getName());

		// map the port to the parsers input port (it only has one)

		model.addDataConstraint(new DataConstraint(model, p, parser
				.getInputPorts()[0]));

		// create an ObjectProcessor of the same dataType to hold the reult in

		BiomobyObjectProcessor resultProcessor = new BiomobyObjectProcessor(
				new ScuflModel(), model.getValidProcessorName(datatype), "",
				datatype, processor.getMobyEndpoint(), false);

		System.out.println("result Processor created = "
				+ resultProcessor.getName());

		// now create a workflow output for each outputport of the parser

		OutputPort[] parserPorts = parser.getOutputPorts();
		ArrayList<String> portNames = new ArrayList<String>();

		for (int i = 0; i < parserPorts.length; i++) {

			OutputPort parserPort = parserPorts[i];

			InputPort outPort = new InputPort(model.getWorkflowSinkProcessor(),
					"" + Math.random() + workflowName + "_"
							+ parserPort.getName());
			model.getWorkflowSinkProcessor().addPort(outPort);
			System.out.println("portname = " + outPort.getName());
			model.addDataConstraint(new DataConstraint(model, parserPort,
					outPort));

			portNames.add(outPort.getName());

		}

		PlaygroundDataObject resultPlaygroundDataObject = new PlaygroundDataObject(
				resultProcessor);

		if (recording) {
			recordedMap.put(resultPlaygroundDataObject, p);
			recordedParserMap.put(resultPlaygroundDataObject, parser);
			// TODO for all subcomponents map to the the playground parser

			// resultPlaygroundDataObject.getAllComponents()

		}

		resultMap.put(resultPlaygroundDataObject, portNames);

	}

	private static void addComponentstoWorkflow(ScuflModel model,
			PlaygroundDataObject dataObject, HashMap<String, DataThing> inputMap)
			throws DataConstraintCreationException, UnknownPortException,
			DuplicatePortNameException, PortCreationException {

		ArrayList<PlaygroundDataObject> dataComponents = dataObject
				.getDataComponents();

		// if we have no dataComponents then we are at the end datatype and
		// need to add an input if a dataThing is mapped to it
		if (dataComponents.size() <= 0) {
			addInputToWorkflow(model, dataObject, inputMap);
		}

		// else we need to add the data components to the workflow and
		// map them to the input ports on the parent dataObject

		for (Iterator i = dataComponents.iterator(); i.hasNext();) {

			PlaygroundDataObject child = (PlaygroundDataObject) i.next();
			BiomobyObjectProcessor childProcessor = child.getProcessor();
			System.out.println("adding ...dataComponent "
					+ childProcessor.getName());
			model.addProcessor(childProcessor);
			System.out.println("added dataComponent "
					+ childProcessor.getName());
			model.addDataConstraint(new DataConstraint(model, childProcessor
					.locatePort("mobyData"), dataObject.getPortMappings().get(
					child)));
			addComponentstoWorkflow(model, child, inputMap);
		}

	}

	private static void addInputToWorkflow(ScuflModel model,
			PlaygroundDataObject dataObject, HashMap<String, DataThing> inputMap)
			throws DataConstraintCreationException, DuplicatePortNameException,
			PortCreationException {

		// if( dataObject.getDataType().equals("Object")){

		ArrayList<PlaygroundPortObject> inputPortObjects = new ArrayList(
				dataObject.getInputPortObjects().values());

		for (Iterator i = inputPortObjects.iterator(); i.hasNext();) {

			PlaygroundPortObject portObject = (PlaygroundPortObject) i.next();
			Port port = portObject.getPort();
			PlaygroundObject playgroundObject = portObject.getMappedObject();

			if (playgroundObject != null) {

				if (playgroundObject instanceof PlaygroundDataThing) {

					PlaygroundDataThing playgroundDataThing = (PlaygroundDataThing) playgroundObject;

					OutputPort newInput = new OutputPort(model
							.getWorkflowSourceProcessor(), dataObject
							.getProcessor().getName()
							+ "_" + playgroundDataThing.getName());

					model.getWorkflowSourceProcessor().addPort(newInput);

					model.addDataConstraint(new DataConstraint(model, newInput,
							port));

					// add this portName->datathing mapping to the inputMap
					if (inputMap != null) {
						inputMap.put(newInput.getName(), playgroundDataThing
								.getDataThing());
					}
				}
			}
		}
		// } //if object
	}

	// resets the state of the recording
	public void stopRecording() {

		recordedWorkflow = new ScuflModel();
		recordedMap = new HashMap<PlaygroundDataObject, Port>();
	}

	public static ScuflModel getRecordedWorkflow() {
		return recordedWorkflow;
	}

}
