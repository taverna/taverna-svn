/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import bsh.Interpreter;


/**
 * A task to invoke a BeanShellProcessor
 * @author Chris Greenhalgh
 */
public class BeanshellTask implements ProcessorTaskWorker 
{
    
    //private static Logger logger = Logger.getLogger(BeanshellTask.class);    
    
    private Processor proc;
	private Interpreter interpreter = new Interpreter();

    public BeanshellTask(Processor p) 
	{
	this.proc = p;
    }
    
    public Map execute(java.util.Map workflowInputMap, IProcessorTask parentTask) throws TaskExecutionException {
		try
		{
			BeanshellProcessor theProcessor = (BeanshellProcessor)proc;
			String script = theProcessor.getScript();
			Map outputMap = new HashMap();
			OutputPort outputPorts[] = theProcessor.getOutputPorts();
			synchronized(interpreter) 
			{
				// set inputs
				Iterator iinput = workflowInputMap.keySet().iterator();
				while(iinput.hasNext()) 
				{
					String inputname = (String)iinput.next();
					DataThing inputdt = (DataThing)workflowInputMap.get(inputname);
					interpreter.set(inputname, inputdt.getDataObject());
				}
				// run
				Object result = interpreter.eval(script);
				// get and clear outputs
				for (int ioutput=0; ioutput<outputPorts.length; ioutput++)
				{
					Object value = interpreter.get(outputPorts[ioutput].getName());
					if (value!=null) 
					{
						outputMap.put(outputPorts[ioutput].getName(),new DataThing(value));
						// syntactic type?
					}
					interpreter.unset(outputPorts[ioutput].getName());
				}
				// clear inputs
				iinput = workflowInputMap.keySet().iterator();
				while(iinput.hasNext()) 
				{
					String inputname = (String)iinput.next();
					interpreter.unset(inputname);
				}
			}
			return outputMap;
		}
		catch(Exception ex) 
		{
			throw new TaskExecutionException("Error running beanshell script: "+ex);
		}	
    }
}
