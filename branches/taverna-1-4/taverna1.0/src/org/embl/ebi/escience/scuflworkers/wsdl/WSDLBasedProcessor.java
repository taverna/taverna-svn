/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
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
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.WSDLParser;

/**
 * A processor based on an operation defined within a WSDL file accessible to
 * the class at construction time.
 * 
 * @author Tom Oinn
 */

public class WSDLBasedProcessor extends Processor implements java.io.Serializable {

	private static final long serialVersionUID = 6669263809722072508L;

	public int getMaximumWorkers() {
		return 10;
	}

	private static Logger logger = Logger.getLogger(WSDLBasedProcessor.class);

	WSIFPort port = null;

	String operationName = null;

	String wsdlLocation = null;

	String[] inNames, outNames;

	Class[] inTypes, outTypes;

	WSDLParser parser = null;

	private static Map defMap = new HashMap();

	public WSDLBasedProcessor(ScuflModel model, String procName, String wsdlLocation, String operationName)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		this(model, procName, wsdlLocation, operationName, null);
	}

	/**
	 * Use a static synchronized cache to avoid re-loading and parsing WSDL
	 * files where possible within a single Taverna instance
	 * 
	 * @throws WSDLException
	 */
	public static Definition getDefinition(String wsdlLocation) throws WSDLException {
		if (defMap.containsKey(wsdlLocation)) {
			return (Definition) defMap.get(wsdlLocation);
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

	public WSDLBasedProcessor(ScuflModel model, String procName, String wsdlLocation, String operationName,
			QName portTypeName) throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, procName);

		this.wsdlLocation = wsdlLocation;
		this.operationName = operationName;
		if (this.isOffline()) {
			return;
		}

		try {
			parser = new WSDLParser(wsdlLocation);
		} catch (Exception e) {
			ProcessorCreationException pce = new ProcessorCreationException(procName + ": Unable to load wsdl at "
					+ wsdlLocation);
			pce.initCause(e);
			logger.error(pce);
			throw pce;
		}

		// Configure to use axis then read the WSDL
		WSIFPluggableProviders.overrideDefaultProvider("http://schemas.xmlsoap.org/wsdl/soap/",
				new WSIFDynamicProvider_ApacheAxis());
		Definition def = parser.getDefinition();

		try {
			List inputs = parser.getOperationInputParameters(operationName);
			List outputs = parser.getOperationOutputParameters(operationName);
			setDescription(parser.getOperationDocumentation(operationName));

			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			port = factory.getService(def).getPort();

			inNames = new String[inputs.size()];
			inTypes = new Class[inputs.size()];

			outNames = new String[outputs.size()];
			outTypes = new Class[outputs.size()];

			TypeDescriptor.retrieveSignature(inputs, inNames, inTypes);
			TypeDescriptor.retrieveSignature(outputs, outNames, outTypes);

			for (int i = 0; i < inNames.length; i++) {
				InputPort inputPort = new InputPort(this, inNames[i]);
				inputPort.setSyntacticType(TypeDescriptor.translateJavaType(inTypes[i]));
				addPort(inputPort);
			}

			// Add an attachment output part
			OutputPort attachments = new OutputPort(this, "attachmentList");
			attachments.setSyntacticType("l('')");
			addPort(attachments);

			for (int i = 0; i < outNames.length; i++) {
				OutputPort outputPort = new OutputPort(this, outNames[i]);
				outputPort.setSyntacticType(TypeDescriptor.translateJavaType(outTypes[i]));
				addPort(outputPort);
			}
		} catch (UnknownOperationException e) {
			throw new ProcessorCreationException(procName + ": Unable to locate operation " + operationName
					+ " in WSDL at " + wsdlLocation);
		} catch (Exception e) {
			ProcessorCreationException ex = new ProcessorCreationException("Unable to process operation "
					+ operationName + " in WSDL at " + wsdlLocation);
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * Build a single use WSIFOperation object. This should only be used for a
	 * single invocation of the target service!
	 */
	public WSIFOperation getWSIFOperation() throws WSIFException {
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
	 * Get the target endpoint for this processor
	 */
	public String getResourceHost() {
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

}
