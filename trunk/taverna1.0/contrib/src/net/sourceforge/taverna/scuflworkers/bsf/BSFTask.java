/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.ExtendedBSFManager;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import bsh.Interpreter;

/**
 * A task to invoke a BSFProcessor
 * 
 * Last edited by: $Author: sowen70 $
 * @author mfortner
 */
public class BSFTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(BSFTask.class);

    private static final int INVOCATION_TIMEOUT = 0;

    private Processor proc;
    
    private BSFEngine engine;
    
    private Map inputBeanMap = new HashMap();
    private Map outputBeanMap = new HashMap();
    private Vector beanVector = new Vector();

    private Interpreter interpreter = new Interpreter();
    private ExtendedBSFManager mgr = new ExtendedBSFManager();

    public BSFTask(Processor p) {
        this.proc = p;
    }
    
    public BSFTask(){
        
    }

    public Map execute(java.util.Map workflowInputMap ,  IProcessorTask parentTask)
            throws TaskExecutionException {
        try {
            BSFProcessor theProcessor = (BSFProcessor) proc;
            String script = theProcessor.getScript();
            String language = theProcessor.getLanguage();
            Map outputMap = new HashMap();
            OutputPort outputPorts[] = theProcessor.getOutputPorts();
            //mgr.initBSFDebugManager();
            engine = mgr.loadScriptingEngine(language);
            
            
            synchronized (engine) {
            	//engine.initialize(mgr,theProcessor.getLanguage(), null);
            	
                // set inputs
                Iterator iinput = workflowInputMap.keySet().iterator();
                while (iinput.hasNext()) {
                    String inputname = (String) iinput.next();
                    DataThing inputdt = (DataThing) workflowInputMap
                            .get(inputname);
                    Object dataObj =inputdt.getDataObject(); 
                    //ExtendedBSFDeclaredBean bean = new ExtendedBSFDeclaredBean(inputname, dataObj, dataObj.getClass());
                    //inputBeanMap.put(inputname, bean); 
                    //beanVector.add(bean);
                    //engine.declareBean(bean);
                    
                    mgr.declareBean(inputname, dataObj,dataObj.getClass());
                    
                    //interpreter.set(inputname, inputdt.getDataObject());
                }
 
                
                
                // set outputs
                for (int ioutput = 0; ioutput < outputPorts.length; ioutput++) {
                	
                    String outputname = outputPorts[ioutput].getName();
                    //DataFlavor flavor = outputPorts[ioutput].getTransferDataFlavors()[0];
                    //Object outObj = outputPorts[ioutput].getTransferData(flavor);
                    //ExtendedBSFDeclaredBean outBean = new ExtendedBSFDeclaredBean(outputname,new String(), String.class );
                    //this.outputBeanMap.put(outputname, outBean);
                    //beanVector.add(outBean);
                    //engine.declareBean(outBean);
                    //DataThing outObj = new DataThing("");
                    
                    mgr.declareBean(outputname,new String(), String.class );
                    //interpreter.unset(outputPorts[ioutput].getName());
                }
                
                
                // execute the script
                //engine.initialize(mgr, language,beanVector);
                
                Object r = mgr.eval(language,"testScript", 0,0, script);
                
                
                // convert outputs to DataThings.
               for (int ioutput = 0; ioutput < outputPorts.length; ioutput++) {
                	
                    String outputname = outputPorts[ioutput].getName();
                    
                    Object outBean =  mgr.lookupBean(outputname);
                    
                    if (outBean != null){
                    	outputMap.put(outputname, new DataThing(outBean));
                    }
                    
                }
               outputMap.put("result",new DataThing(r));
                
                
                /*
                // clear inputs
                iinput = workflowInputMap.keySet().iterator();
                while (iinput.hasNext()) {
                    String inputname = (String) iinput.next();
                    Object inObj = workflowInputMap.get(inputname);
                    engine.undeclareBean(new ExtendedBSFDeclaredBean(inputname, inObj, inObj.getClass()));
                    //interpreter.unset(inputname);
                }*/
                
                
            }
            return outputMap;
        } catch (Exception ex) {
        	ex.printStackTrace();
            throw new TaskExecutionException("Error running bsf script: "
                    + ex);
        }
    }




}