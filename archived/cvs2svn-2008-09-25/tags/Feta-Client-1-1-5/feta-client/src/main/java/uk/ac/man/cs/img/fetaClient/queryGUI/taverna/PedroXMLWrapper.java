/*
 * PedroXMLWrapper.java
 *
 * Created on 27 June 2004, 17:00
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

/**
 * @author alperp
 */
public class PedroXMLWrapper implements IServiceModelFiller {

	private static Logger logger = Logger.getLogger(PedroXMLWrapper.class);

	private String operationName;

	private String serviceName;

	private String descriptionLocation;

	private String operationDescriptionText;

	private String serviceDescriptionText;

	private String serviceInterfaceLocation;

	private String locationURL;

	private LinkedHashMap<String, String> inputParameters;

	private LinkedHashMap<String, String> outputParameters;
	
	private ServiceType serviceType;

	private String organisationName;

	private String operationTask;

	private String operationMethod;

	private String operationApplication;

	private String operationResource;

	private String operationResourceContent;

	private String operationSpec;

	/** Creates a new instance of PedroXMLWrapper */
	public PedroXMLWrapper(String operID) throws Exception {
		logger.debug("Finding pedro service description " + operID);
		// tokenize location, service name and operation name
		String[] tokens = operID.split("\\$");
		if (tokens.length != 3) {
			throw new IllegalArgumentException("Could not parse operation ID " + operID);
		}
		descriptionLocation = tokens[0];
		serviceName = tokens[1];
		operationName = tokens[2];

		
		DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(descriptionLocation);

		NodeList serviceNameList =
			document.getElementsByTagName(FetaModelXSD.SERVICE_NAME);

		for (int j = 0; j < serviceNameList.getLength(); j++) {
			if (! serviceName.equalsIgnoreCase(getElementValue((Element) serviceNameList.item(j)))) {
				continue;
			}
			Element serviceElement =
				(Element) serviceNameList.item(j).getParentNode();

			NodeList operationNameList =
				serviceElement.getElementsByTagName(FetaModelXSD.OPERATION_NAME);

			for (int k = 0; k < operationNameList.getLength(); k++) {
				if (! operationName.equalsIgnoreCase(getElementValue((Element) operationNameList.item(k)))) {
					continue;
				}
				Element operationElement =
					(Element) operationNameList.item(k).getParentNode();
				operationDescriptionText =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPER_DESC_TEXT);
				serviceDescriptionText =
					getFirstSubElementValue(serviceElement,
						FetaModelXSD.SERV_DESC_TEXT);
				organisationName =
					getFirstSubElementValue(serviceElement,
						FetaModelXSD.ORGANISATION_NAME);
				locationURL =
					getFirstSubElementValue(serviceElement,
						FetaModelXSD.LOCATION_URL);
				
				serviceInterfaceLocation =
					getFirstSubElementValue(serviceElement,
						FetaModelXSD.INTERFACE_WSDL);
				String serviceTypeStr =
					getFirstSubElementValue(serviceElement,
						FetaModelXSD.SERVICE_TYPE);

				// not really we should not do this
				if (serviceTypeStr == null) {
					serviceType = ServiceType.WSDL;
				} else {
					serviceType =
						ServiceType.getTypeForString(serviceTypeStr);
				}

				operationApplication =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPER_APP);
				operationMethod =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPER_METHOD);
				operationTask =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPER_TASK);
				operationResource =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPER_RESOURCE);
				operationResourceContent =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPER_RESOURCE_CONTENT);
				operationSpec =
					getFirstSubElementValue(operationElement,
						FetaModelXSD.OPERATION_SPEC);
				
				// Find parameters name and descriptions
				inputParameters = findParameters(operationElement, FetaModelXSD.OPERATION_INPUTS);
				outputParameters = findParameters(operationElement, FetaModelXSD.OPERATION_OUTPUTS);
				return; // found
			}
		}
	}

	private static LinkedHashMap<String, String> findParameters(Element operationElement, String tagName) {
		LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
		Element operationInputs = (Element) operationElement.getElementsByTagName(tagName).item(0);
		if (operationInputs == null) {
			return parameters; // empty
		}
		NodeList inputs = operationInputs.getElementsByTagName(FetaModelXSD.PARAMETER);
		for (int i = 0; i<inputs.getLength(); i++) {
			Element parameter = (Element) inputs.item(i);
			String name = getFirstSubElementValue(parameter, FetaModelXSD.PARAMETER_NAME);
			String desc = getFirstSubElementValue(parameter, FetaModelXSD.PARAMETER_DESC);
			if (name == null) {
				logger.warn("Unknown parameter name for " + parameter);
				continue;
			}
			if (desc == null) { 
				desc = "";
			}
			parameters.put(name, desc);
		}
		return parameters;
	}

	private static String getElementValue(Element element) {
		Node elementTextNode = element.getFirstChild();
		return elementTextNode.getNodeValue();

	}

	private static String getFirstSubElementValue(Element element,
		String childElementName) {
		NodeList childList = element.getElementsByTagName(childElementName);
		Element childElement = (Element) childList.item(0);
		if (childElement == null) {
			return null;
		}
		return getElementValue(childElement);
	}

	/* Operation Related */
	public String getOperationName() {
		return operationName;
	}

	public String getOperationDescriptionText() {
		return operationDescriptionText;
	}

	/* Operation Annotation Related */
	public String getOperationMethod() {
		return operationMethod;
	}

	public String getOperationTask() {
		return operationTask;
	}

	public String getOperationApplication() {
		return operationApplication;
	}

	public String getOperationResource() {
		return operationResource;
	}

	public String getOperationResourceContent() {
		return operationResourceContent;
	}

	public String getOperationSpec() {
		return operationSpec;
	}

	/* Service Related */
	public String getServiceName() {
		return serviceName;
	}

	public String getDescriptionLocation() {
		return descriptionLocation;
	}

	public String getServiceDescriptionText() {
		return serviceDescriptionText;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public String getServiceInterfaceLocation() {
		return serviceInterfaceLocation;
	}

	public String getLocationURL() {
		return locationURL;
	}

	public String getOrganisationName() {
		return organisationName;
	}
	
	public LinkedHashMap<String, String> getInputParameters() {
		// Assumes our caller don't modify the hashmap..
		return inputParameters;
	}
	
	public LinkedHashMap<String, String> getOutputParameters() {
		// Assumes our caller don't modify the hashmap..
		return outputParameters;
	}

	/* We do not have any setter methods for now! */
}
