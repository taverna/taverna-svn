package uk.org.mygrid.dataplayground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

public class PlaygroundDataObject extends PlaygroundObject {

	// a shared scufl model to ensure processors don't have conflicting names;
	private static ScuflModel model = new ScuflModel();

	private BiomobyObjectProcessor biomobyProcessor;
	private ArrayList<PlaygroundDataObject> dataComponents = new ArrayList<PlaygroundDataObject>();
	private String dataType;
	private String articleName;

	// set if this is the top level object of a set of "collapsed" verticies;
	boolean collapsed;
	// set if this vertex is supposed to be hidden in the graph;
	boolean hidden;

	// list of the PortObjects to be exposed on the playground , in this to
	// expose id and namespace or value
	Map<String, PlaygroundPortObject> inputPortObjects;

	// contains the objects mapped to this DataObjects ports , they may be other
	// data objects
	// for a complex data type or dataThings for the leaf dataObjects
	HashMap<PlaygroundObject, Port> portMappings;

	public PlaygroundDataObject(BiomobyObjectProcessor biomobyProcessor) {
		super();

		biomobyProcessor.setName(model.getValidProcessorName(biomobyProcessor
				.getName()));
		model.addProcessor(biomobyProcessor);
		this.biomobyProcessor = biomobyProcessor;
		setName(biomobyProcessor.getName());
		portMappings = new HashMap<PlaygroundObject, Port>();
		inputPortObjects = new HashMap<String, PlaygroundPortObject>();

		if (biomobyProcessor.getServiceName().equalsIgnoreCase("Object")) {
			setDataType(biomobyProcessor.getServiceName());
			InputPort[] ports = biomobyProcessor.getInputPorts();
			for (InputPort p : ports) {
				if (p.getName().equals("namespace") || p.getName().equals("id")) {
					inputPortObjects.put(p.getName(), new PlaygroundPortObject(
							p));
				}
			}
		} else if (biomobyProcessor.getServiceName().equalsIgnoreCase("String")
				|| biomobyProcessor.getServiceName()
						.equalsIgnoreCase("Integer")
				|| biomobyProcessor.getServiceName().equalsIgnoreCase(
						"DateTime")) {
			for (InputPort p : biomobyProcessor.getInputPorts()) {
				if (p.getName().equals("namespace") || p.getName().equals("id")) {
					PlaygroundPortObject po = new PlaygroundPortObject(p);
					po.setInvisible(true);
					inputPortObjects.put(p.getName(), po);
				}
				if (p.getName().equalsIgnoreCase("value")) {
					inputPortObjects.put(p.getName(), new PlaygroundPortObject(
							p));
				}
			}
			setDataType(biomobyProcessor.getServiceName());
			return;
		} else {
			setDataType(biomobyProcessor.getServiceName());
			createDataComponents();

			// if this has no data components then expose namespace and id
			if (dataComponents.isEmpty()) {
				for (InputPort p : biomobyProcessor.getInputPorts()) {
					if (p.getName().equals("namespace")
							|| p.getName().equals("id")) {

						inputPortObjects.put(p.getName(),
								new PlaygroundPortObject(p));
					}
				}

			} else {
				// we want to add the id and namespace components but we want to
				// keep them hidden unless requested
				for (InputPort p : biomobyProcessor.getInputPorts()) {
					if (p.getName().equals("namespace")
							|| p.getName().equals("id")) {
						PlaygroundPortObject po = new PlaygroundPortObject(p);
						po.setInvisible(true);
						inputPortObjects.put(p.getName(), po);
					}
				}
			}
		}
	}

	public void collapse() {
		ArrayList<PlaygroundPortObject> portObjects = new ArrayList<PlaygroundPortObject>(
				getInputPortObjects().values());
		for (PlaygroundPortObject portObject : portObjects) {
			portObject.setHidden(!portObject.isHidden());
			if (portObject.getMappedObject() != null) {
				PlaygroundDataThing mappedThing = ((PlaygroundDataThing) portObject
						.getMappedObject());
				mappedThing.setHidden(!mappedThing.isHidden());
			}
		}
		HashMap<String, PlaygroundDataObject> components = (HashMap<String, PlaygroundDataObject>) getAllComponents();
		for (PlaygroundDataObject pdo : components.values()) {
			pdo.setHidden(!pdo.isHidden());
			// if(pdo.getInputPortObjects().values().size() > 0){

			ArrayList<PlaygroundPortObject> componentPortObjects = new ArrayList<PlaygroundPortObject>(
					pdo.getInputPortObjects().values());
			for (PlaygroundPortObject portObject : componentPortObjects) {
				portObject.setHidden(!portObject.isHidden());
				if (portObject.getMappedObject() != null) {
					PlaygroundDataThing mappedThing = ((PlaygroundDataThing) portObject
							.getMappedObject());
					mappedThing.setHidden(!mappedThing.isHidden());
				}
			}
		}
		collapsed = !collapsed;
	}

	/**
	 * 
	 * @return A map of ArticleName->PlaygroundDataObject
	 */
	public Map<String, PlaygroundDataObject> getAllComponents() {
		HashMap<String, PlaygroundDataObject> result = new HashMap<String, PlaygroundDataObject>();
		for (PlaygroundDataObject pdo : dataComponents) {
			result.put(pdo.getArticleName(), pdo);
			result.putAll(pdo.getAllComponents());
		}
		return result;
	}

	public String getArticleName() {
		return articleName;
	}

	public ArrayList<PlaygroundDataObject> getDataComponents() {
		return dataComponents;
	}

	public String getDataType() {
		return dataType;
	}

	public Map<String, PlaygroundPortObject> getInputPortObjects() {
		return inputPortObjects;
	}

	public HashMap<PlaygroundObject, Port> getPortMappings() {
		return portMappings;
	}

	public BiomobyObjectProcessor getProcessor() {
		return biomobyProcessor;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String toString() {
		return getName();
	}

	private void createDataComponents() {
		if (!(biomobyProcessor instanceof BiomobyObjectProcessor)) {
			return;
		}
		BiomobyObjectProcessor processor = (BiomobyObjectProcessor) biomobyProcessor;
		if (model == null)
			return;

		for (InputPort p : biomobyProcessor.getInputPorts()) {
			if (p.getName().equals("namespace") || p.getName().equals("id")
					|| p.getName().equals("article name")
					|| p.getName().equals("value")) {
				continue;
			}

			String portName = p.getName();
			String datatype = portName.split("\\(")[0];
			String articleName = portName.split("\\(")[1].split("\\)")[0];

			Processor subComponentProcessor;

			try {
				subComponentProcessor = new BiomobyObjectProcessor(model, model
						.getValidProcessorName(datatype), "", datatype,
						processor.getMobyEndpoint(), false);
				// scuflModel.addProcessor(subComponentProcessor);
				// scuflModel.addDataConstraint(new
				// DataConstraint(scuflModel,
				// subComponentProcessor.locatePort("mobyData"), p));

				PlaygroundDataObject pdo = new PlaygroundDataObject(
						(BiomobyObjectProcessor) subComponentProcessor);
				pdo.setDataType(datatype);
				pdo.setArticleName(articleName);
				dataComponents.add(pdo);
				portMappings.put(pdo, p);
				System.out.println("Component added now = " + pdo);

			} catch (ProcessorCreationException pce) {
				JOptionPane.showMessageDialog(null,
						"Processor creation exception : \n" + pce.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
				return;
			} catch (DuplicateProcessorNameException dpne) {
				JOptionPane.showMessageDialog(null, "Duplicate name : \n"
						+ dpne.getMessage(), "Exception!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

		}
	}

}
