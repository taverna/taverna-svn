/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.ClasspathUtils;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.embl.ebi.escience.scuflworkers.wsdl.soap.WSDLSOAPInvoker;
import org.w3c.dom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * The task required to invoke an arbitrary web service.
 * 
 * @author Tom Oinn
 */
public class WSDLInvocationTask implements ProcessorTaskWorker {

	private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);

	private WSDLBasedProcessor processor;

	private static final List<String> secureServiceList = new ArrayList<String>();

	private static EngineConfiguration securityConfiguration = null;

	static {
		try {
			String omiiClientHome = System.getProperty("omii.client.home");
			if (omiiClientHome != null) {
				File omiiLibDir = new File(omiiClientHome, "lib");
				File omiiConfDir = new File(omiiClientHome, "conf");
				String omiiClasspath = ClasspathUtils.expandDirs(omiiLibDir
						.getCanonicalPath())
						+ File.pathSeparator + omiiConfDir.getCanonicalPath();
				logger.debug("Set omii classpath " + omiiClasspath);
				ClassLoader omiiClassLoader = ClassUtils.createClassLoader(
						omiiClasspath, ClassUtils.class.getClassLoader());
				ClassUtils.setDefaultClassLoader(omiiClassLoader);
				securityConfiguration = new FileProvider(omiiConfDir
						.getAbsolutePath(), "client-config.wsdd");
				securityConfiguration.configureEngine(new AxisClient());
			}
		} catch (Exception e) {
			logger.debug("Failed to load security configuration : "
					+ e.getMessage());
			securityConfiguration = null;
		}
	}

	public WSDLInvocationTask(Processor p) {
		this.processor = (WSDLBasedProcessor) p;
	}

	/**
	 * Executes the service identified by the ProcessorTask, and returns a Map
	 * of the output DataThing's mapped against the output name.
	 */
	public Map execute(Map inputMap, IProcessorTask parentTask)
			throws TaskExecutionException {
		Map result = null;
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);
		try {
			if (isSecureService()) {
				result = invoker.invoke(inputMap, securityConfiguration);
			} else {
				result = invoker.invoke(inputMap);
			}
		} catch (AxisFault af) {
			String reason = af.getFaultReason();
			if (reason != null) {
				reason = reason.toLowerCase();
				// Note: Check in lowercase in case they fix the
				// typo "required Security header". Don't check the classname
				// (WSDoAllReceiver) as it has a tendency to change with wss4j
				// versions. This is still very flaky, but there seems to be no
				// way of asking wss4j if a service is expecting security headers
				if (reason.contains("does not contain required security header") && 
					!isSecureService() && securityConfiguration != null) {
					// this looks like a secure service so set it as secure and
					// try again
					logger.debug("Retrying as secure service");
					setSecureService(true);
					return execute(inputMap, parentTask);
				}
			}
			logger.error("Axis fault invoking wsdl based service: "
					+ af.getMessage());
			Element[] details = af.getFaultDetails();
			if (details != null) {
				for (int i = 0; i < details.length; i++) {
					Element detailElement = details[i];
					logger.error("Fault Detail:"
							+ XMLUtils.ElementToString(detailElement));
				}
			}
			TaskExecutionException te = new TaskExecutionException(
					"Fault returned invoking webservice: "
							+ af.getFaultReason());
			te.initCause(af);
			throw te;

		} catch (TaskExecutionException e) {
			logger.error("Error occurred invoking wsdl based service: "
					+ e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error occurred invoking wsdl based service: "
					+ e.getMessage());
			TaskExecutionException te = new TaskExecutionException(
					"Error occured during invocation " + e.getMessage());
			te.initCause(e);
			throw te;
		}
		return result;
	}

	private boolean isSecureService() {
		return secureServiceList.contains(processor.getWSDLLocation());
	}

	private void setSecureService(boolean isSecure) {
		if (isSecure) {
			if (!isSecureService()) {
				secureServiceList.add(processor.getWSDLLocation());
			}
		} else {
			secureServiceList.remove(processor.getWSDLLocation());
		}
	}

}
