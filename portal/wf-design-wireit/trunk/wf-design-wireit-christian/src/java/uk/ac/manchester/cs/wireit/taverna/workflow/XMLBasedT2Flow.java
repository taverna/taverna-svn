package uk.ac.manchester.cs.wireit.taverna.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;
import uk.ac.manchester.cs.wireit.taverna.XMLReader;

/**
 * XMLBased hack for extracting the required information from a T2Flow workflow.
 *
 * This implementation is a hack which obtains the information based on the expected XML structure.
 * 
 * @author Christian
 */
public class XMLBasedT2Flow implements TavernaWorkflow{

    /**
     * Name of the workflowas given in the T2Flow
     */
    private String workflowName;
    
    /**
     * Map of expected inputs by name and the corrospondoing depth for each input.
     */
    private HashMap<String,Integer>inputs;
    
    /**
     * List of the expected outputs of the workflow
     */
    private ArrayList<String> outputs;
    
    /**
     * Extracts the required information by parsing the file as XML and looking for the required information.
     * 
     * Expected locations of information are hard coded in.
     * Only works for the expected T2Flow format.
     * 
     * @param file A T2Flow file
     * @throws TavernaException if the file is not XML parsable or not in the hardcode format.
     */
    public XMLBasedT2Flow(File file) throws TavernaException{
        Document workflow;
        try {
            workflow = XMLReader.readFile(file);
        } catch (Exception ex) {
            throw new TavernaException ("Unable to parse workflow file as XML", ex);
        }
        updateWorkflow(workflow);
    }
      
    /**
     * Extracts the required information by parsing the uri as XML and looking for the required information.
     * 
     * Expected locations of information are hard coded in.
     * Only works for the expected T2Flow format.
     * 
     * @param uri URI to a T2Flow file
     * @throws TavernaException if the file pointed to by the uri is not XML parsable or not in the hardcode format.
     */
    public XMLBasedT2Flow(String uri) throws TavernaException{
        Document workflow; 
        try {
            workflow = XMLReader.readFile(uri);
        } catch (Exception ex) {
            throw new TavernaException ("Unable to parse workflow file as XML", ex);
        }
        updateWorkflow(workflow);
    } 

    /**
     * Extracts the required information from the XML document by looking for the required information.
     * 
     * Expected locations of information are hard coded in.
     * Only works for the expected T2Flow format.
     * 
     * @param workflow XML DOM of a T2Flow 
     * @throws TavernaException if the DOM is not in the hardcode format.
     */
    private void updateWorkflow(Document workflow) throws TavernaException{
        inputs = new HashMap<String,Integer>();
        outputs = new ArrayList<String>();
        try { 
            HashMap<String,Integer> newInputs = new HashMap<String,Integer>();
            Element root = workflow.getDocumentElement();
            Element dataflow = XMLReader.getDirectOnlyChildrenByName(root, "dataflow");
            Element name = XMLReader.getDirectOnlyChildrenByName(dataflow, "name");
            workflowName = XMLReader.getText(name);
            Element inputPorts = XMLReader.getDirectOnlyChildrenByName(dataflow, "inputPorts");
            List<Node> ports = XMLReader.getDirectChildrenByName(inputPorts,"port");
            for (Node port: ports){
                name = XMLReader.getDirectOnlyChildrenByName(port, "name");
                String theName = XMLReader.getText(name);
                Element depth = XMLReader.getDirectOnlyChildrenByName(port, "depth");
                Integer theDepth = new Integer(XMLReader.getText(depth));
                inputs.put(theName, theDepth);
            }
            Element outputPorts = XMLReader.getDirectOnlyChildrenByName(dataflow, "outputPorts");
            ports = XMLReader.getDirectChildrenByName(outputPorts,"port");
            for (Node port: ports){
                name = XMLReader.getDirectOnlyChildrenByName(port, "name");
                String theName = XMLReader.getText(name);
                outputs.add(theName);
            }
        } catch (Exception ex) {
            throw new TavernaException ("Unable to parse XML as a workflow.", ex);
        }
   }

    @Override
    public String getWorkflowName() {
        return workflowName;
    }

    @Override
    public Map<String, Integer> getInputs() {
        return inputs;
    }
    
    @Override
    public List<String> getOutputs() {
        return outputs;
    }

    public static void main(String[] args) throws TavernaException {
        XMLBasedT2Flow me = new XMLBasedT2Flow("Echo.t2flow");
        Map<String, Integer> inputs = me.getInputs();
        System.out.println(inputs);
        List<String> outputs = me.getOutputs();
        System.out.println(outputs);
    }

}

