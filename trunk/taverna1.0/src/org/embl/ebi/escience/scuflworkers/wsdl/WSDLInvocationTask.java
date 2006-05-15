/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.util.Map;

import org.apache.axis.AxisFault;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.embl.ebi.escience.scuflworkers.wsdl.soap.WSDLSOAPInvoker;
import org.w3c.dom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * The task required to invoke an arbitrary web service.
 * 
 * @author Tom Oinn
 */
public class WSDLInvocationTask implements ProcessorTaskWorker {

	private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);

	private WSDLBasedProcessor processor;
	
	public WSDLInvocationTask(Processor p) {
		this.processor = (WSDLBasedProcessor) p;
	}

	/**
	 * Executes the service identified by the ProcessorTask, and returns a Map of the output DataThing's
	 * mapped against the output name.
	 */
	public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {
		Map result = null;
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);
		try {
			result = invoker.invoke(inputMap);
		} catch (AxisFault af) {
			logger.error("Axis fault invoking wsdl based service: " + af.getMessage());
			Element[] details = af.getFaultDetails();
			if (details != null) {
				for (int i = 0; i < details.length; i++) {
					Element detailElement = details[i];
					logger.error("Fault Detail:" + XMLUtils.ElementToString(detailElement));
				}
			}
			TaskExecutionException te = new TaskExecutionException("Fault returned invoking webservice: "
					+ af.getFaultReason());
			te.initCause(af);
			throw te;

		} catch (TaskExecutionException e) {
			logger.error("Error occurred invoking wsdl based service: " + e.getMessage());
			throw (TaskExecutionException) e;
		} catch (Exception e) {
			logger.error("Error occurred invoking wsdl based service: " + e.getMessage());
			TaskExecutionException te = new TaskExecutionException("Error occured during invocation " + e.getMessage());
			te.initCause(e);
			throw te;
		}
		return result;
	}
	
}
