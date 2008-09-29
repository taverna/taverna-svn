package uk.org.mygrid.dataplayground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;

public class PlaygroundDataObject extends PlaygroundObject {

	//a shared scufl model to ensure processors don't have conflicting names;
	private static ScuflModel model = new ScuflModel();
	
	private BiomobyObjectProcessor d;
	private ArrayList<PlaygroundDataObject> dataComponents = new ArrayList<PlaygroundDataObject>();
	private String dataType;
	private String articleName;
	
	//set if this is the top level object of a set of "collapsed" verticies;
	boolean collapsed;
	//set if this vertex is supposed to be hidden in the graph;
	boolean hidden;
	
	
	//list of the PortObjects to be exposed on the playground , in this to expose id and namespace or value
	Map<String,PlaygroundPortObject> inputPortObjects;
	
	//contains the objects mapped to this DataObjects ports , they may be other data objects
	//for a complex data type or dataThings for the leaf dataObjects
	HashMap<PlaygroundObject ,Port> portMappings;
	
	public PlaygroundDataObject(BiomobyObjectProcessor d) {
		super();
		
		d.setName(model.getValidProcessorName(d.getName()));
		model.addProcessor(d);
		this.d = d;
		setName(d.getName());
		portMappings = new HashMap<PlaygroundObject,Port>();
		inputPortObjects = new HashMap<String,PlaygroundPortObject>();
		
		
        if(d.getServiceName().equalsIgnoreCase("Object")){
        	
        	setDataType(d.getServiceName());
        	InputPort[] ports = d.getInputPorts();
			
			for (int i = 0; i < ports.length; i++) {
				InputPort p = ports[i];
				
				if (p.getName().equals("namespace") || p.getName().equals("id")){
						
					inputPortObjects.put(p.getName(),new PlaygroundPortObject(p));
				}
        
        }
		
        } else if ( d.getServiceName().equalsIgnoreCase("String")
				|| d.getServiceName().equalsIgnoreCase("Integer")
				|| d.getServiceName().equalsIgnoreCase("DateTime")) {
        	InputPort[] ports = d.getInputPorts();
			
			for (int i = 0; i < ports.length; i++) {
				InputPort p = ports[i];
				
				if (p.getName().equals("namespace") || p.getName().equals("id")){
						PlaygroundPortObject po = new PlaygroundPortObject(p);
						po.setInvisible(true);
					inputPortObjects.put(p.getName(),po);
				
				}
				if(p.getName().equalsIgnoreCase("value")){
					inputPortObjects.put(p.getName(),new PlaygroundPortObject(p));
				}
			}
			
			setDataType(d.getServiceName());
			return;
			
		} else {
			setDataType(d.getServiceName());
			createDataComponents();
		
            //if this has no data components then expose namespace and id
			if(dataComponents.size() == 0){
				InputPort[] ports = d.getInputPorts();
				
				for (int i = 0; i < ports.length; i++) {
					InputPort p = ports[i];
					
					if (p.getName().equals("namespace") || p.getName().equals("id")){
							
						inputPortObjects.put(p.getName(),new PlaygroundPortObject(p));
					}
				}
				
			}else{
				//we want to add the id and namespace components but we want to keep them hidden unless requested
		        InputPort[] ports = d.getInputPorts();
				
				for (int i = 0; i < ports.length; i++) {
					InputPort p = ports[i];
					
					if (p.getName().equals("namespace") || p.getName().equals("id")){
							PlaygroundPortObject po = new PlaygroundPortObject(p);
							po.setInvisible(true);
						inputPortObjects.put(p.getName(),po);
					
					}
				}
								
			  }
		
		}
		
		

	}

	private void createDataComponents() {

		if (d instanceof BiomobyObjectProcessor) {
			BiomobyObjectProcessor processor = (BiomobyObjectProcessor) d;
			
			if (model == null)
				return;
			
			InputPort[] ports = processor.getInputPorts();
			
			for (int i = 0; i < ports.length; i++) {
				InputPort p = ports[i];
				
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
					subComponentProcessor = new BiomobyObjectProcessor(
							model, model
									.getValidProcessorName(datatype), "",
							datatype, processor.getMobyEndpoint(), false);
					//scuflModel.addProcessor(subComponentProcessor);
					// scuflModel.addDataConstraint(new DataConstraint(scuflModel,
					//       subComponentProcessor.locatePort("mobyData"), p));

					PlaygroundDataObject pdo = new PlaygroundDataObject(
							(BiomobyObjectProcessor) subComponentProcessor);
					pdo.setDataType(datatype);	
					pdo.setArticleName(articleName);
					dataComponents.add(pdo);
					portMappings.put(pdo,p);
					System.out.println("Component added now = " + pdo);

				} catch (ProcessorCreationException pce) {
					JOptionPane.showMessageDialog(null,
							"Processor creation exception : \n"
									+ pce.getMessage(), "Exception!",
							JOptionPane.ERROR_MESSAGE);
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
	
	public BiomobyObjectProcessor getProcessor(){
		
		return d;
	}

	public ArrayList<PlaygroundDataObject> getDataComponents() {
		return dataComponents;
	}

	
	//returns a map of ArticleName->PlaygroundDataObject
	
	public Map<String,PlaygroundDataObject> getAllComponents(){
		
		HashMap<String,PlaygroundDataObject> result = new HashMap<String,PlaygroundDataObject>();
		
		for(Iterator i = dataComponents.iterator(); i.hasNext();){
			PlaygroundDataObject pdo =(PlaygroundDataObject) i.next();
			result.put(pdo.getArticleName(),pdo);
			result.putAll(pdo.getAllComponents());
			
		}
		
		
		return result;
	}
	
	
	public void collapse(){
		
	//if(getDataType().equalsIgnoreCase("Object")){
			
			ArrayList<PlaygroundPortObject> portObjects = new ArrayList(getInputPortObjects().values());
			for(Iterator k = portObjects.iterator(); k.hasNext();){
				PlaygroundPortObject portObject = (PlaygroundPortObject) k.next();
				portObject.setHidden(!portObject.isHidden());
				if(portObject.getMappedObject() != null){
					((PlaygroundDataThing)portObject.getMappedObject()).setHidden(!((PlaygroundDataThing)portObject.getMappedObject()).isHidden());
				}
			}
			
		//}else{
		
	    HashMap<String,PlaygroundDataObject> components =(HashMap<String,PlaygroundDataObject>) getAllComponents();
		
	
		for(Iterator i = components.values().iterator(); i.hasNext();){
			
      			
			PlaygroundDataObject pdo =(PlaygroundDataObject) i.next();
			pdo.setHidden(!pdo.isHidden());
			//if(pdo.getInputPortObjects().values().size() > 0){
				
				ArrayList<PlaygroundPortObject> componentPortObjects = new ArrayList( pdo.getInputPortObjects().values());
				for(Iterator k = componentPortObjects.iterator(); k.hasNext();){
					PlaygroundPortObject portObject = (PlaygroundPortObject) k.next();
					portObject.setHidden(!portObject.isHidden());
					if(portObject.getMappedObject() != null){
						((PlaygroundDataThing)portObject.getMappedObject()).setHidden(!((PlaygroundDataThing)portObject.getMappedObject()).isHidden());
					}
				}
				
			//}end if
		}
		//}end else
		collapsed = !collapsed;
	}
	
	
	public String toString() {

		return getName();
	}

	

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Map<String,PlaygroundPortObject> getInputPortObjects() {
		return inputPortObjects;
	}

	public HashMap<PlaygroundObject, Port> getPortMappings() {
		return portMappings;
	}

	public String getArticleName() {
		return articleName;
	}

	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	

	
}
