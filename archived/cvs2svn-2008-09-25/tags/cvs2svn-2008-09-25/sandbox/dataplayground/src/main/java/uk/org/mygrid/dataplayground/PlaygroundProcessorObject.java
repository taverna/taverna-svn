package uk.org.mygrid.dataplayground;

import java.util.ArrayList;
import java.util.Map;

import org.biomoby.client.CentralImpl;
import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;
import org.biomoby.client.taverna.plugin.BiomobyProcessor;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.biomoby.shared.NoSuccessException;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;

import edu.uci.ics.jung.graph.Vertex;

public class PlaygroundProcessorObject extends PlaygroundObject {

	BiomobyProcessor p;
	ArrayList<PlaygroundPortObject> inputPortObjects;
	ArrayList<PlaygroundPortObject> outputPortObjects;
	// Map<PlaygroundDataObject,Port> portMappings;

	boolean running;

	ScuflModel model = new ScuflModel();

	public PlaygroundProcessorObject(BiomobyProcessor p) {

		super();
		this.p = p;

		setName(p.getServiceName());

		InputPort[] inPorts = p.getInputPorts();
		OutputPort[] outPorts = p.getOutputPorts();

		inputPortObjects = new ArrayList<PlaygroundPortObject>();
		outputPortObjects = new ArrayList<PlaygroundPortObject>();

		for (int i = 0; i < inPorts.length; i++) {
			InputPort port = inPorts[i];

			if (port.getName().equals("namespace")
					|| port.getName().equals("id")
					|| port.getName().equals("article name")
					|| port.getName().equals("input")) {
				continue;
			}

			String portName = port.getName();
			String datatype = portName.split("\\(")[0];
			System.out.println("Input PortName =  " + datatype);

			inputPortObjects.add(new PlaygroundPortObject(port));

		}

		for (int i = 0; i < outPorts.length; i++) {
			OutputPort port = outPorts[i];

			if (port.getName().equals("namespace")
					|| port.getName().equals("id")
					|| port.getName().equals("article name")
					|| port.getName().equals("output")) {
				continue;
			}

			String portName = port.getName();
			String datatype = portName.split("\\(")[0];

			System.out.println("Output PortName =  " + datatype);

			outputPortObjects.add(new PlaygroundPortObject(port));

		}

	}

	public Map<String, DataThing> addComponentsToModel(ScuflModel model,
			PlaygroundDataObject parent) {

		BiomobyObjectProcessor processor = parent.getProcessor();
		ArrayList<PlaygroundDataObject> dataComponents = parent
				.getDataComponents();

		for (PlaygroundDataObject child : dataComponents) {
			BiomobyObjectProcessor childProcessor = child.getProcessor();
			try {

				model.addDataConstraint(new DataConstraint(model,
						childProcessor.locatePort("mobyData"), processor
								.locatePort(child.getName())));
				if (child.getDataComponents().size() > 0) {
					addComponentsToModel(model, child);
				} else {
					// create input and add to map the dataThing & input port
					// mapping
				}
			} catch (DataConstraintCreationException e) {
				e.printStackTrace();
			} catch (UnknownPortException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void addInputType(BiomobyObjectProcessor o) {

		MobyPrimaryDataSimple m = new MobyPrimaryDataSimple("input");

		try {

			Central worker = new CentralImpl(p.getMobyEndpoint());
			m.setDataType(worker.getDataType(o.getServiceName()));
			p.getMobyService().addInput(m);
		} catch (MobyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean combine(Vertex v) {

		// get the dataObject from v
		// map the output from the Object (mobyData) to the input of the
		// processor
		// make the workflow output the output(s) from this processor excluding
		// "output"
		// get the dataThing(s) attached to the dataObject (getPredecessors)
		// use it/them as input to this workflow
		// get the result & hopefully encapsulate as the correct moby type ?

		/*
		 * if(v instanceof PlaygroundDataObject){
		 * 
		 * PlaygroundDataObject d = (PlaygroundDataObject)v;
		 * System.out.println("Combine " + this + " " + v); Map
		 * <String,DataThing>inputMap = new HashMap<String,DataThing>();
		 * 
		 * //easiest case if we only have 1 input then just map the dataObject
		 * to the input and //run a workflow using the dataObjects dataThing(if
		 * it has one) if(inputPortObjects.size() == 1){
		 * 
		 * model.addProcessor(p); BiomobyObjectProcessor object =
		 * d.getProcessor(); model.addProcessor(d.getProcessor()); try {
		 * 
		 * model.addDataConstraint(new
		 * DataConstraint(model,object.locatePort("mobyData"),inputPortObjects.iterator().next().getPort())); }
		 * catch (DataConstraintCreationException e) { e.printStackTrace();
		 * return false; } catch (UnknownPortException e) { e.printStackTrace();
		 * return false; }
		 * 
		 * //check if the data object is a simple of complex one
		 * if(d.getDataComponents().size() > 0){
		 * 
		 * inputMap = addComponentsToModel(model,d); } else{ //we have just a
		 * simple data object //add workflow input connecting to the dataObject
		 * and create map with this dataObjects dataThing (if connected)
		 * 
		 * try {
		 * 
		 * PlaygroundDataThing playgroundDataThing = d.getData();
		 * 
		 * if(playgroundDataThing == null) return false;
		 * 
		 * 
		 * 
		 * 
		 * 
		 * //add workflow input
		 * 
		 * OutputPort newInput = new OutputPort(model
		 * .getWorkflowSourceProcessor(), playgroundDataThing.getName());
		 * 
		 * model.getWorkflowSourceProcessor().addPort(newInput);
		 * 
		 * //add this portName->datathing mapping to the inputMap
		 * 
		 * inputMap.put(d.getData().getName(),d.getData().getDataThing());
		 * 
		 * 
		 * 
		 * 
		 * 
		 * //loop through the Objects inputs till we find one that isn't input ,
		 * namespace , etc
		 * 
		 * //InputPort[] ports = object.getInputPorts(); // for (int i = 0; i <
		 * ports.length; i++) { // InputPort p = ports[i]; // // if (
		 * p.getName().equals("value")) { // model.addDataConstraint(new
		 * DataConstraint(model,newInput,p)); // } // }
		 * 
		 *  } catch (DuplicatePortNameException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); return false; } catch
		 * (PortCreationException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); return false; } catch
		 * (DataConstraintCreationException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); return false; } } } }
		 */
		return true;
	}

	// returns true if we were sucessful or false otherwise n.b. a "sucessful"
	// workflow run may not
	// mean that we have a valid output

	public ArrayList<PlaygroundPortObject> getInputPortObjects() {
		return inputPortObjects;
	}

	// adds the DataObjects components to the model and returns the input map
	// required
	// for this part of the generated workflow

	public ArrayList<PlaygroundPortObject> getOutputPortObjects() {
		return outputPortObjects;
	}

	public Processor getProcessor() {

		return p;

	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String toString() {

		return getName();
	}

}

// scuflModel.addProcessor(subComponentProcessor);
// scuflModel.addDataConstraint(new DataConstraint(scuflModel,
// subComponentProcessor.locatePort("mobyData"), p));

