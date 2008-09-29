/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

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

    private Interpreter interpreter = new Interpreter();

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
            synchronized (interpreter) {
                // set inputs
                Iterator iinput = workflowInputMap.keySet().iterator();
                while (iinput.hasNext()) {
                    String inputname = (String) iinput.next();
                    DataThing inputdt = (DataThing) workflowInputMap
                            .get(inputname);
                    interpreter.set(inputname, inputdt.getDataObject());
                }
                // run
                Object result = interpreter.eval(script);
                // get and clear outputs
                for (int ioutput = 0; ioutput < outputPorts.length; ioutput++) {
                    Object value = interpreter.get(outputPorts[ioutput]
                            .getName());
                    if (value != null) {
                        outputMap.put(outputPorts[ioutput].getName(),
                                new DataThing(value));
                        // syntactic type?
                    }
                    interpreter.unset(outputPorts[ioutput].getName());
                }
                // clear inputs
                iinput = workflowInputMap.keySet().iterator();
                while (iinput.hasNext()) {
                    String inputname = (String) iinput.next();
                    interpreter.unset(inputname);
                }
            }
            return outputMap;
        } catch (Exception ex) {
            throw new TaskExecutionException("Error running bsf script: "
                    + ex);
        }
    }




}