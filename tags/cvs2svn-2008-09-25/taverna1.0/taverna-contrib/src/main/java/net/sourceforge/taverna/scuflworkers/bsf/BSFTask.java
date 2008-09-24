/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.ExtendedBSFManager;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A task to invoke a BSFProcessor
 * 
 * Last edited by: $Author: sowen70 $
 * 
 * @author mfortner
 * @author Stian Soiland
 */
public class BSFTask implements ProcessorTaskWorker {

	// Magic output port, if this has been added, the script will be
	// run as an evaluation, and the result put as "result"
	private static final String RESULT = "result";

	private static Logger logger = Logger.getLogger(BSFTask.class);

	private Processor proc;

	private BSFEngine engine;

	private ExtendedBSFManager mgr = new ExtendedBSFManager();

	public BSFTask(Processor p) {
		this.proc = p;
	}

	public BSFTask() {

	}

	public Map execute(Map workflowInputMap, IProcessorTask parentTask) throws TaskExecutionException {
		try {
			BSFProcessor theProcessor = (BSFProcessor) proc;
			String script = theProcessor.getScript();
			String language = theProcessor.getLanguage();
			Map<String, DataThing> outputMap = new HashMap<String, DataThing>();
			List<OutputPort> outputPorts = Arrays.asList(theProcessor.getOutputPorts());
			// mgr.initBSFDebugManager();
			engine = mgr.loadScriptingEngine(language);

			synchronized (engine) {
				// engine.initialize(mgr,theProcessor.getLanguage(), null);

				// set inputs
				Iterator iinput = workflowInputMap.keySet().iterator();
				while (iinput.hasNext()) {
					String inputname = (String) iinput.next();
					DataThing inputdt = (DataThing) workflowInputMap.get(inputname);
					Object dataObj = inputdt.getDataObject();
					// ExtendedBSFDeclaredBean bean = new
					// ExtendedBSFDeclaredBean(inputname, dataObj,
					// dataObj.getClass());
					// inputBeanMap.put(inputname, bean);
					// beanVector.add(bean);
					// engine.declareBean(bean);

					mgr.declareBean(inputname, dataObj, dataObj.getClass());

					// interpreter.set(inputname, inputdt.getDataObject());
				}

				// set outputs
				for (OutputPort output : outputPorts) {
					String outputname = output.getName();
					// DataFlavor flavor =
					// outputPorts[ioutput].getTransferDataFlavors()[0];
					// Object outObj =
					// outputPorts[ioutput].getTransferData(flavor);
					// ExtendedBSFDeclaredBean outBean = new
					// ExtendedBSFDeclaredBean(outputname,new String(),
					// String.class );
					// this.outputBeanMap.put(outputname, outBean);
					// beanVector.add(outBean);
					// engine.declareBean(outBean);
					// DataThing outObj = new DataThing("");

					mgr.declareBean(outputname, new String(), String.class);
					// interpreter.unset(outputPorts[ioutput].getName());
				}

				// execute the script
				// engine.initialize(mgr, language,beanVector);
				
				// FIXME: Avoid flow control through exceptions
				Object result = null;
				try {
					theProcessor.locatePort(RESULT);
					// Found "magic" port, we'll also capture the result of evaluating
					// the script. Note that some languages (Python) don't allow
					// assignments (ie. other outputs) in evaluations
					logger.debug("Running script as evaluation");
					result = mgr.eval(language, "testScript", 0, 0, script);
				} catch (UnknownPortException ex) {
					logger.debug("Running script as execution");
					mgr.exec(language, "testScript", 0, 0, script);
				}

				// convert outputs to DataThings.
				for (OutputPort port : outputPorts) {
					String outputname = port.getName();
					Object outBean = mgr.lookupBean(outputname);
					if (outBean != null) {
						outputMap.put(outputname, new DataThing(outBean));
					}
				}
				
				if (result != null) {
					// Include evaluation result
					outputMap.put(RESULT, new DataThing(result));
				}

				/*
				 * // clear inputs iinput =
				 * workflowInputMap.keySet().iterator(); while
				 * (iinput.hasNext()) { String inputname = (String)
				 * iinput.next(); Object inObj =
				 * workflowInputMap.get(inputname); engine.undeclareBean(new
				 * ExtendedBSFDeclaredBean(inputname, inObj, inObj.getClass()));
				 * //interpreter.unset(inputname); }
				 */

			}
			return outputMap;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TaskExecutionException("Error running bsf script: " + ex);
		}
	}

}
