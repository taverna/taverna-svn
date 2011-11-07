/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.module;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.RunWireit;
import uk.ac.manchester.cs.wireit.event.OutputFirer;
import uk.ac.manchester.cs.wireit.event.OutputListener;
import uk.ac.manchester.cs.wireit.taverna.CommandLineRun;
import uk.ac.manchester.cs.wireit.taverna.CommandLineWrapper;
import uk.ac.manchester.cs.wireit.taverna.ProcessException;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;
import uk.ac.manchester.cs.wireit.taverna.TavernaInput;
import uk.ac.manchester.cs.wireit.taverna.baclava.DataThingBasedBaclava;
import uk.ac.manchester.cs.wireit.taverna.workflow.TavernaWorkflow;
import uk.ac.manchester.cs.wireit.taverna.workflow.XMLBasedT2Flow;

/**
 *
 * @author Christian
 */
public class TavernaModule extends Module{

    private CommandLineWrapper commandLine;
    private Map<String,ValueListener> inputPorts;
    private Map<String,OutputFirer> outputPorts;
    private Map<String,TavernaInput> tavernaInputs;
    private OutputFirer baclavaOutput;
    private String URLRoot;
    private URI baclavaInput;
    //private final String WORKING_PATH = "webapps/WireIt";
    private boolean alreadyRun = false;
    
        
    public static String WORKFLOWS_DIR = "Workflows";
    public static String INPUTS_DIR = "Input";
    public static String OUTPUTS_DIR = "Output";

    public TavernaModule(JSONObject json, StringBuffer URL) throws JSONException, TavernaException, IOException{
        super(json);
        commandLine = new CommandLineWrapper();
        //setTavernaHome(System.getenv("TAVERNA_HOME"));
        
        setTavernaHome(RunWireit.TAVERNA_CMD_HOME);
        System.out.println("Taverna Home: " + RunWireit.TAVERNA_CMD_HOME);
        
        //File outputDirectory = new File(RunWireit.ABSOLUTE_WORKING_PATH, "Output");
        File outputDirectory = new File(RunWireit.ABSOLUTE_ROOT_FILE_PATH, OUTPUTS_DIR);
        if (!outputDirectory.exists()){
            outputDirectory.mkdirs();
        }
        System.out.println("Taverna Output Dir: " + outputDirectory);
        commandLine.setOutputRootDirectory(outputDirectory);
        
        setWorkflow(json);  
        URLRoot = URL.substring(0, URL.lastIndexOf("/"));
    }
    
    public final void setTavernaHome(String tavernaHome) throws TavernaException, IOException {
        if (tavernaHome != null && !tavernaHome.isEmpty()){
            commandLine.setTavernaHome(new File(tavernaHome));        
        } 
    }
    
    private void setWorkflow(JSONObject json) throws JSONException, TavernaException, IOException{
        JSONObject config = json.getJSONObject("config");
        String fileSt = config.optString("wfURI");
        
        //Checks for security. Change as required
        if (fileSt.contains("..")){
            throw new TavernaException ("Security exception URIs cannot contain \"..\"");
        }
        //File workflowFile = new File("webapps/WireIt/Workflows/" + fileSt);
        File workflowsDirectory = new File(RunWireit.ABSOLUTE_ROOT_FILE_PATH, WORKFLOWS_DIR);
        File workflowFile = new File(workflowsDirectory, fileSt);
        commandLine.setWorkflowFile(workflowFile);
        
        TavernaWorkflow workflow = new XMLBasedT2Flow(workflowFile);
        setInputs(workflow);
        setOutputs(workflow);
    }
        
    private void setInputs(TavernaWorkflow workflow) throws TavernaException{
        //removeNullandEmptyValues();
        Map<String,Integer> inputs = workflow.getInputs();  
        inputPorts = new HashMap<String,ValueListener>();
        tavernaInputs = new HashMap<String,TavernaInput>();
        for (String key:inputs.keySet()){
            TavernaInput tavernaInput = new TavernaInput(key, inputs.get(key));
            tavernaInputs.put(key, tavernaInput);
            ValueListener port = new ValueListener(tavernaInput);
            inputPorts.put(key, port);
        }
        baclavaInput = null;
    }

    private void setOutputs(TavernaWorkflow workflow){
        outputPorts = new HashMap<String,OutputFirer>();
        List<String> outputs = workflow.getOutputs();
        for (String output: outputs){
            outputPorts.put(output, new OutputFirer());
        }
        baclavaOutput = new OutputFirer();
    }
    
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        //Just in case their are no inputs are all set as values.
        if (!alreadyRun) { 
            runIfReady(outputBuilder);
        }
    }
    
    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (inputPorts.containsKey(terminal)){
            return inputPorts.get(terminal);
        } else if (terminal.startsWith("in_") && inputPorts.containsKey(terminal.substring(3))) {
            return inputPorts.get(terminal.substring(3));            
        } else if (terminal.equals("Baclava Input")){
            return new BaclavaListener();
        } else {
            String portNames = "";
            for (String key:inputPorts.keySet()){
                portNames = portNames + key + ", ";
            }
            throw new JSONException("No input Port found with name " + terminal + " Ports are: " + portNames);
        }
    }

    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        if (outputPorts.containsKey(terminal)){
            outputPorts.get(terminal).addOutputListener(listener);
        } else if (terminal.equals("Baclava Output")){
            baclavaOutput.addOutputListener(listener);
        } else if (terminal.startsWith("out_") && outputPorts.containsKey(terminal.substring(4))) {
            outputPorts.get(terminal.substring(4)).addOutputListener(listener);           
        } else {
            String portNames = "";
            for (String key:outputPorts.keySet()){
                portNames = portNames + key + ", ";
            }
            throw new JSONException("No output Port found with name " + terminal + " Ports are: " + portNames);
        }
    }
    
    private boolean allValuesSet(){        //System.out.println("in allValuesSet" + values.size());
        for (String key:tavernaInputs.keySet()){
            if (!tavernaInputs.get(key).hasValue()){
                return false;
            }
        }
        return true;
    }
     
    File runWorkflowWithInputs() throws TavernaException, ProcessException{
        System.out.println("Workflow ready based on inputs!");
        TavernaInput[] inputArray = new TavernaInput[0];
        inputArray = tavernaInputs.values().toArray(inputArray);
        commandLine.setInputs(inputArray);
        System.out.println("ready to run");
        CommandLineRun run = commandLine.runWorkFlow();
        System.out.println("ready started");
        File output = run.getOutputFile();
        System.out.println("Workflow ran");
        return output;
    }

    File runWorkflowWithBaclava() throws TavernaException, ProcessException{
        System.out.println("Workflow ready based on Baclava!");
        commandLine.setInputsURI(baclavaInput.toString());        
        CommandLineRun run = commandLine.runWorkFlow();
        File output = run.getOutputFile();
        System.out.println("Workflow ran");
        return output;
    }

    private void runIfReady(StringBuilder outputBuilder) throws WireItRunException {
        File output;
        if (allValuesSet()){
            try {
                output = runWorkflowWithInputs();             
            } catch (Exception ex) {
                 throw new WireItRunException("Error running workflow: " + name + "  " + ex.getMessage(), ex);
            } 
            processRun(output, outputBuilder);
        } else if (baclavaInput != null) {
            try {
                output = runWorkflowWithBaclava();             
            } catch (Exception ex) {
                 throw new WireItRunException("Error running workflow: " + name + "  " + ex.getMessage(), ex);
            } 
            processRun(output, outputBuilder);
        }
    }
    
    private void processRun(File output, StringBuilder outputBuilder) throws WireItRunException {
        alreadyRun = true;
        DataThingBasedBaclava baclava;
        outputBuilder.append("Workflow ");
        outputBuilder.append(name);
        outputBuilder.append(" ran successfully.\n");
        try {
            baclava = new DataThingBasedBaclava(output);
        } catch (TavernaException ex) {
            throw new WireItRunException ("Unable to read Baclava file from " + name, ex);
        }
        for (String key:outputPorts.keySet()){
            //ystem.out.print (key + ": ");
            Object value;
            try {
                value = baclava.getValue(key);
            } catch (TavernaException ex) {
                throw new WireItRunException ("Unable to read value " + key + " from Baclava form " + name, ex);
            }
            //ystem.out.println(value);
            outputPorts.get(key).fireOutputReady(value, outputBuilder);
        }
        String baclavaPath = output.getPath().replace("\\","/");
        File baclavaFile = new File (baclavaPath);
        //ystem.out.println(bavalaPath);
        //String baclavaURI = URLRoot + baclavaPath.substring(RunWireit.ABSOLUTE_ROOT_PATH.length());   
        String baclavaURI = RunWireit.ABSOLUTE_ROOT_URL + "/" + OUTPUTS_DIR + "/" + baclavaFile.getParentFile().getName() + "/" + baclavaFile.getName();
                
        //baclavaPath.substring(RunWireit.ABSOLUTE_ROOT_FILE_PATH.length());   
        
        URI uri;
        try {
            uri = new URI(baclavaURI);
        } catch (URISyntaxException ex) {
            throw new WireItRunException ("Unable to set Baclava URI for " + name, ex);
        }
        baclavaOutput.fireOutputReady(uri, outputBuilder);
    }
    
    private class ValueListener implements OutputListener{

        private TavernaInput myInput;
        
        private ValueListener(TavernaInput input){
            myInput = input;
        }
        
        @Override
        public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
             try {
                if (output instanceof String){
                    System.out.println("Setting string");
                    myInput.setStringInput(output.toString());
                } else if (output instanceof byte[]){
                    //This is a hack. 
                    //A nicer way would be to save to file and then pass as file.
                    byte[] array = (byte[])output;
                    String asString = new String(array);
                    myInput.setStringInput(asString);
                } else if (output instanceof String[]){
                    //TavernaInputs will throw an exception is depth is not 1
                    System.out.println("Setting string array");
                    myInput.setStringsInput((String[])output);
                } else if (output instanceof URI){
                    System.out.println("setting URI");
                    myInput.setSingleURIInput(output.toString());                    
                } else if (output instanceof DelimiterURI){
                    //TavernaInputs will throw an exception is depth is not 1
                    DelimiterURI delimiterURI = (DelimiterURI)output;
                    myInput.setListURIInput(delimiterURI.getURI().toString(), delimiterURI.getDelimiter());
               } else {
                    //I could have done output.toString() but for now want to check every type is handled correctly.
                     throw new WireItRunException ("Unknown input type " + output.getClass() + " in " + name);
                }
            } catch (TavernaException ex) {
                throw new WireItRunException ("Error setting Taverna input for " + name + ex.getMessage(), ex);
            }
            runIfReady(outputBuilder);
        }
    }

    private class BaclavaListener implements OutputListener{
       
        private BaclavaListener(){
        }
        
        @Override
        public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
            if (output instanceof URI){
                baclavaInput = (URI)output;
                runIfReady(outputBuilder);
            } else {
                 throw new WireItRunException ("Unknown inpiut type " + output.getClass() + " in " + name);
            }
            runIfReady(outputBuilder);
        }
    }

}