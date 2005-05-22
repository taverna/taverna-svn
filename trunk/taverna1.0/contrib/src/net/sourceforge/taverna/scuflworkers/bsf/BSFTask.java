/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.ExtendedBSFManager;
import org.apache.bsf.ExtendedBSFDeclaredBean;
import org.apache.bsf.util.CodeBuffer;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import bsh.Interpreter;

/**
 * A task to invoke a BSFProcessor
 * 
 * Last edited by: $Author: phidias $
 * @author mfortner
 */
public class BSFTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(BSFTask.class);

    private static final int INVOCATION_TIMEOUT = 0;

    private Processor proc;
    
    private BSFEngine engine;
    
    private Map inputBeanMap = new HashMap();
    private Map outputBeanMap = new HashMap();

    private Interpreter interpreter = new Interpreter();
    private ExtendedBSFManager mgr = new ExtendedBSFManager();

    public BSFTask(Processor p) {
        this.proc = p;
    }
    
    public BSFTask(){
        
    }

    public Map execute(java.util.Map workflowInputMap ,  ProcessorTask parentTask)
            throws TaskExecutionException {
        try {
            BSFProcessor theProcessor = (BSFProcessor) proc;
            String script = theProcessor.getScript();
            Map outputMap = new HashMap();
            OutputPort outputPorts[] = theProcessor.getOutputPorts();
           
            engine = mgr.loadScriptingEngine(theProcessor.getLanguage());
            
            
            synchronized (engine) {
            	engine.initialize(mgr,theProcessor.getLanguage(), null);
                // set inputs
                Iterator iinput = workflowInputMap.keySet().iterator();
                while (iinput.hasNext()) {
                    String inputname = (String) iinput.next();
                    DataThing inputdt = (DataThing) workflowInputMap
                            .get(inputname);
                    Object dataObj =inputdt.getDataObject(); 
                    ExtendedBSFDeclaredBean bean = new ExtendedBSFDeclaredBean(inputname, dataObj, dataObj.getClass());
                    inputBeanMap.put(inputname, bean); 
                    engine.declareBean(bean);
                    
                    //interpreter.set(inputname, inputdt.getDataObject());
                }
 
                
                
                // set outputs
                for (int ioutput = 0; ioutput < outputPorts.length; ioutput++) {
                	
                    String outputname = outputPorts[ioutput].getName();
                    
                    ExtendedBSFDeclaredBean outBean = new ExtendedBSFDeclaredBean(outputname,new String(), String.class );
                    this.outputBeanMap.put(outputname, outBean);
                    
                    engine.declareBean(outBean);
                    //interpreter.unset(outputPorts[ioutput].getName());
                }
                
                
                // execute the script
                engine.exec(null, 0,0, script);
                
                // convert outputs to DataThings.
                for (int ioutput = 0; ioutput < outputPorts.length; ioutput++) {
                	
                    String outputname = outputPorts[ioutput].getName();
                    
                    ExtendedBSFDeclaredBean outBean = (ExtendedBSFDeclaredBean)this.outputBeanMap.get(outputname);
                    
                    outputMap.put(outputname, new DataThing(outBean.bean));
                    
                }
                
                
                
                // clear inputs
                iinput = workflowInputMap.keySet().iterator();
                while (iinput.hasNext()) {
                    String inputname = (String) iinput.next();
                    Object inObj = workflowInputMap.get(inputname);
                    engine.undeclareBean(new ExtendedBSFDeclaredBean(inputname, inObj, inObj.getClass()));
                    //interpreter.unset(inputname);
                }
                
                
            }
            return outputMap;
        } catch (Exception ex) {
            throw new TaskExecutionException("Error running bsf script: "
                    + ex);
        }
    }




}