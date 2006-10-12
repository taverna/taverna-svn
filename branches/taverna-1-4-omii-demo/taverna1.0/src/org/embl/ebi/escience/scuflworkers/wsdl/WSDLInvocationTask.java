/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.FileProvider;
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
	
	private static final List secureServiceList = new ArrayList();

	private static EngineConfiguration securityConfiguration = null;
	
	static {
		try {
			securityConfiguration = new FileProvider("client-config.wsdd");
			securityConfiguration.configureEngine(new AxisClient());
		} catch (Exception e) {
			logger.debug("Failed to load security configuration : " + e.getMessage());
			securityConfiguration = null;
		}
	}

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
			if (isSecureService()) {
				result = invoker.invoke(inputMap, securityConfiguration);
			} else {
				result = invoker.invoke(inputMap);
			}
		} catch (AxisFault af) {
			if ("SecurityContextInitHandler: Request does not contain required Security header"
					.equals(af.getMessage())) {
				if (!isSecureService() && securityConfiguration != null) {
					// this looks like a secure service so set it as secure and try again
					setSecureService(true);
					return execute(inputMap, parentTask);
				}
			}
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

	public boolean isSecureService() {
		return secureServiceList.contains(processor.getWSDLLocation());
	}
	
	public void setSecureService(boolean isSecure) {
		if (isSecure) {
			if (!isSecureService()) {
				secureServiceList.add(processor.getWSDLLocation());
			}
		} else  {
			secureServiceList.remove(processor.getWSDLLocation());
		}
	}
		
}
