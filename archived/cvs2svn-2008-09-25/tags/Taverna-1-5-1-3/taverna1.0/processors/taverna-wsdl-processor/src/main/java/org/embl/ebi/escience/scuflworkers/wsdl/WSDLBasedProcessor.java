/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.providers.soap.apacheaxis.WSIFPort_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFUtils;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.WorkflowSummaryAsHTML;
import org.embl.ebi.escience.scuflworkers.HTMLSummarisableProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLSplittableInputPort;
import org.embl.ebi.escience.scuflworkers.java.XMLSplittableOutputPort;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.WSDLParser;

/**
 * A processor based on an operation defined within a WSDL file accessible to
 * the class at construction time.
 * 
 * @author Tom Oinn
 */

public class WSDLBasedProcessor extends Processor implements Serializable,
		HTMLSummarisableProcessor {

	private static final long serialVersionUID = 6669263809722072508L;

	public int getMaximumWorkers() {
		return 100;
	}

	private static Logger logger = Logger.getLogger(WSDLBasedProcessor.class);

	String operationName = null;

	String wsdlLocation = null;

	String[] inNames, outNames;

	Class[] inTypes, outTypes;

	WSDLParser parser = null;
	
	WSIFService service; 

	private static Map<String, Definition> defMap = new HashMap<String, Definition>();

	public WSDLBasedProcessor(ScuflModel model, String procName,
			String wsdlLocation, String operationName)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		this(model, procName, wsdlLocation, operationName, null);
	}

	/**
	 * Use a static synchronized cache to avoid re-loading and parsing WSDL
	 * files where possible within a single Taverna instance
	 * 
	 * @throws WSDLException
	 */
	public static Definition getDefinition(String wsdlLocation)
			throws WSDLException {
		if (defMap.containsKey(wsdlLocation)) {
			return defMap.get(wsdlLocation);
		} else {
			Definition def = WSIFUtils.readWSDL(null, wsdlLocation);
			defMap.put(wsdlLocation, def);
			return def;
		}
	}

	/**
	 * Provides the javax.wsdl.Definition for WSDL this Processor is associated
	 * with.
	 * 
	 * @return Definition
	 * @throws WSDLException
	 */
	public Definition getDefinition() throws WSDLException {
		return WSDLBasedProcessor.getDefinition(wsdlLocation);
	}

	/**
	 * Construct a new processor from the given WSDL definition and operation
	 * name, delegates to superclass then instantiates ports based on WSDL
	 * inspection.
	 */

	public WSDLBasedProcessor(ScuflModel model, String procName,
			String wsdlLocation, String operationName, QName portTypeName)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, procName);

		this.wsdlLocation = wsdlLocation;
		this.operationName = operationName;
		if (this.isOffline()) {
			return;
		}

		try {
			parser = new WSDLParser(wsdlLocation);
		} catch (Exception e) {
			ProcessorCreationException pce = new ProcessorCreationException(
					procName + ": Unable to load wsdl at " + wsdlLocation);
			pce.initCause(e);
			logger.error(pce);
			throw pce;
		}

		// Configure to use axis then read the WSDL
		WSIFPluggableProviders.overrideDefaultProvider(
				"http://schemas.xmlsoap.org/wsdl/soap/",
				new WSIFDynamicProvider_ApacheAxis());
		Definition def = parser.getDefinition();

		try {
			List inputs = parser.getOperationInputParameters(operationName);
			List outputs = parser.getOperationOutputParameters(operationName);
			setDescription(parser.getOperationDocumentation(operationName));

			// TODO handle more than 1 service block
			if (def.getServices().size() > 1)
				logger
						.warn("WSDL "
								+ getWSDLLocation()
								+ " contains more than one service block, Taverna will only use the first service block");

			Service s = (Service) def.getServices().values().toArray()[0];

			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			PortType portType = parser.getPortType(operationName);
			service = factory.getService(def, s, portType);
						
			inNames = new String[inputs.size()];
			inTypes = new Class[inputs.size()];

			outNames = new String[outputs.size()];
			outTypes = new Class[outputs.size()];

			TypeDescriptor.retrieveSignature(inputs, inNames, inTypes);
			TypeDescriptor.retrieveSignature(outputs, outNames, outTypes);

			for (int i = 0; i < inNames.length; i++) {
				InputPort inputPort = new XMLSplittableInputPort(this,
						inNames[i]);
				inputPort.setSyntacticType(TypeDescriptor
						.translateJavaType(inTypes[i]));
				addPort(inputPort);
			}

			// Add an attachment output part
			OutputPort attachments = new XMLSplittableOutputPort(this,
					"attachmentList");
			attachments.setSyntacticType("l('')");
			addPort(attachments);

			for (int i = 0; i < outNames.length; i++) {
				OutputPort outputPort = new XMLSplittableOutputPort(this,
						outNames[i]);
				outputPort.setSyntacticType(TypeDescriptor
						.translateJavaType(outTypes[i]));
				addPort(outputPort);
			}
		} catch (UnknownOperationException e) {
			throw new ProcessorCreationException(procName
					+ ": Unable to locate operation " + operationName
					+ " in WSDL at " + wsdlLocation);
		} catch (Exception e) {
			ProcessorCreationException ex = new ProcessorCreationException(
					"Unable to process operation " + operationName
							+ " in WSDL at " + wsdlLocation);
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * Build a single use WSIFOperation object. This should only be used for a
	 * single invocation of the target service!
	 */
	public WSIFOperation getWSIFOperation() throws WSIFException {
		WSIFPort port = getPort();
		synchronized (port) {
			WSIFOperation op = port.createOperation(operationName);
			logger.debug("Created operation : " + op.toString());			
			return op;
		}
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("wsdlLocation", getWSDLLocation());
		props.put("operation", getOperationName());
		return props;
	}

	/**
	 * Provides access to the WSDLParser that represents the WSDL of the service
	 * this processor acts upon
	 * 
	 * @return WSDLParser
	 */
	public WSDLParser getParser() {
		return parser;
	}

	/**
	 * Get the WSDL location for this processor
	 */
	public String getWSDLLocation() {
		return this.wsdlLocation;
	}
	
	/**
     * Get a port. Note that if you keep references to this port, it will
     * also mean keeping referencing to the last call, and thereby
     * to the last data in that call.
     */
	WSIFPort getPort() {
		try {
			return service.getPort();
		} catch (WSIFException e) {
			logger.warn("Could not get WSIFPort", e);
			return null;
		}
	}

	/**
	 * Get the target endpoint for this processor
	 */
	public String getResourceHost() {
		WSIFPort port = getPort();
		if (port instanceof WSIFPort_ApacheAxis) {
			URL endpoint = ((WSIFPort_ApacheAxis) port).getEndPoint();
			return endpoint.getHost();
		} else {
			return "Unknown";
		}
	}

	/**
	 * 
	 * @return a String representation of the URL for the endpoint of the
	 *         service associated with this processor.
	 */
	String getTargetEndpoint() {
		WSIFPort port = getPort();
		if (port instanceof WSIFPort_ApacheAxis) {
			return ((WSIFPort_ApacheAxis) port).getEndPoint().toString();
		} else {
			return "Unknown";
		}
	}

	/**
	 * Get the operation name for this processor
	 */
	public String getOperationName() {
		return this.operationName;
	}

	public String getHTMLSummary(List<HTMLSummarisableProcessor> processors,
			Map<String, Processor> names) {
		Map<String, Map<String, Set<String>>> wsLocations = new HashMap<String, Map<String, Set<String>>>();
		StringBuffer sb = new StringBuffer();

		for (HTMLSummarisableProcessor proc : processors) {
			WSDLBasedProcessor wsdlProcessor = (WSDLBasedProcessor) proc;
			String wsdlLocation = "";
			try {
				URL wsdlURL = new URL(wsdlProcessor.getWSDLLocation());
				wsdlLocation = wsdlURL.getFile();
			} catch (MalformedURLException mue) {
				logger.warn("Error with wsdl url: "
						+ wsdlProcessor.getWSDLLocation(), mue);
			}
			if (wsLocations.containsKey(wsdlLocation) == false) {
				wsLocations.put(wsdlLocation,
						new HashMap<String, Set<String>>());
			}
			Map<String, Set<String>> operationToProcessorName = wsLocations
					.get(wsdlLocation);
			String operationName = wsdlProcessor.getOperationName();
			if (operationToProcessorName.containsKey(operationName) == false) {
				operationToProcessorName.put(operationName,
						new HashSet<String>());
			}
			Set<String> processorNames = operationToProcessorName
					.get(operationName);
			processorNames.add(WorkflowSummaryAsHTML.nameFor(names,
					wsdlProcessor));
		}
		for (Iterator j = wsLocations.keySet().iterator(); j.hasNext();) {
			// Top level iterator over all service locations.
			String location = (String) j.next();
			Map operationToProcessorName = (Map) wsLocations.get(location);
			int rows = 2 + operationToProcessorName.size();
			sb.append("<tr>");
			sb.append("<td width=\"80\" valign=\"top\" rowspan=\"" + rows
					+ "\" bgcolor=\"#a3cd5a\">Web&nbsp;service</td>");
			sb
					.append("<td colspan=\"2\" bgcolor=\"#a3cd5a\">WSDL Defined at <em>"
							+ location + "</em></td>");
			sb.append("</tr>");
			sb
					.append("<tr><td bgcolor=\"#eeeedd\">Operation name</td><td bgcolor=\"#eeeedd\">Processors</td></tr>");
			for (Iterator k = operationToProcessorName.keySet().iterator(); k
					.hasNext();) {
				String operationName = (String) k.next();
				Set processorNames = (Set) operationToProcessorName
						.get(operationName);
				sb.append("<tr>");
				sb.append("<td><font color=\"purple\">" + operationName
						+ "</font></td>");
				sb.append("<td>");
				for (Iterator l = processorNames.iterator(); l.hasNext();) {
					sb.append((String) l.next());
					if (l.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append("</td></tr>");
			}
		}
		return sb.toString();
	}

	public int htmlTablePlacement() {
		return 1;
	}

}
