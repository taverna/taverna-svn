/*
 * PedroXMLWrapper.java
 *
 * Created on 27 June 2004, 17:00
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

/**
 * 
 * @author alperp
 */
public class PedroXMLWrapper implements IServiceModelFiller {

	private String operationName;

	private String serviceName;

	private String descriptionLocation;

	private String operationDescriptionText;

	private String serviceDescriptionText;

	private String serviceInterfaceLocation;

	private String locationURL;

	private String[] parameters;

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

		// tokenize location, service name and operation name
		String[] tokens = operID.split("\\$");
		if (tokens.length != 3) {
			Exception e = new Exception();
			throw e;
		}

		this.operationName = tokens[2];
		this.serviceName = tokens[1];
		this.descriptionLocation = tokens[0];

		try {

			Document document;
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(descriptionLocation);

			NodeList serviceNameList = document
					.getElementsByTagName(FetaModelXSD.SERVICE_NAME);

			boolean found = false;
			for (int j = 0; j < serviceNameList.getLength(); j++) {

				if (serviceName
						.equalsIgnoreCase(getElementValue((Element) serviceNameList
								.item(j)))) {

					Element serviceElement = (Element) serviceNameList.item(j)
							.getParentNode();

					NodeList operationNameList = serviceElement
							.getElementsByTagName(FetaModelXSD.OPERATION_NAME);

					for (int k = 0; k < operationNameList.getLength(); k++) {

						if (operationName
								.equalsIgnoreCase(getElementValue((Element) operationNameList
										.item(k)))) {
							found = true;
							Element operationElement = (Element) operationNameList
									.item(k).getParentNode();
							this.operationDescriptionText = getFirstSubElementValue(
									operationElement,
									FetaModelXSD.OPER_DESC_TEXT);
							this.serviceDescriptionText = getFirstSubElementValue(
									serviceElement, FetaModelXSD.SERV_DESC_TEXT);
							this.organisationName = getFirstSubElementValue(
									serviceElement,
									FetaModelXSD.ORGANISATION_NAME);
							this.locationURL = getFirstSubElementValue(
									serviceElement, FetaModelXSD.LOCATION_URL);
							this.serviceInterfaceLocation = getFirstSubElementValue(
									serviceElement, FetaModelXSD.INTERFACE_WSDL);
							String serviceTypeStr = getFirstSubElementValue(
									serviceElement, FetaModelXSD.SERVICE_TYPE);

							// not really we should not do this
							if (serviceTypeStr == null) {
								this.serviceType = ServiceType.WSDL;
							} else {
								this.serviceType = ServiceType
										.getTypeForString(serviceTypeStr);
							}

							this.operationApplication = getFirstSubElementValue(
									operationElement, FetaModelXSD.OPER_APP);
							this.operationMethod = getFirstSubElementValue(
									operationElement, FetaModelXSD.OPER_METHOD);
							this.operationTask = getFirstSubElementValue(
									operationElement, FetaModelXSD.OPER_TASK);
							this.operationResource = getFirstSubElementValue(
									operationElement,
									FetaModelXSD.OPER_RESOURCE);
							this.operationResourceContent = getFirstSubElementValue(
									operationElement,
									FetaModelXSD.OPER_RESOURCE_CONTENT);
							this.operationSpec = getFirstSubElementValue(
									operationElement,
									FetaModelXSD.OPERATION_SPEC);

						}
					}
					if (found)
						break;
				}// if
			}// for

		}// try
		catch (Exception e) {

			throw e;
		}

	}

	private String getElementValue(Element element) {

		Node elementTextNode = (Node) element.getFirstChild();
		return elementTextNode.getNodeValue();

	}

	private String getFirstSubElementValue(Element element,
			String childElementName) {
		String value;
		NodeList childList = element.getElementsByTagName(childElementName);
		if (childList.getLength() > 0) {
			Element childElement = (Element) childList.item(0);
			value = getElementValue(childElement);
		} else {
			value = null;
		}
		return value;

	}

	/* Operation Related */
	public String getOperationName() {
		return this.operationName;
	}

	public String getOperationDescriptionText() {
		return this.operationDescriptionText;
	}

	/* Operation Annotation Related */
	public String getOperationMethod() {
		return this.operationMethod;
	}

	public String getOperationTask() {
		return this.operationTask;
	}

	public String getOperationApplication() {
		return this.operationApplication;
	}

	public String getOperationResource() {
		return this.operationResource;
	}

	public String getOperationResourceContent() {
		return this.operationResourceContent;
	}

	public String getOperationSpec() {
		return this.operationSpec;
	}

	/* Service Related */
	public String getServiceName() {
		return this.serviceName;
	}

	public String getDescriptionLocation() {
		return this.descriptionLocation;
	}

	public String getServiceDescriptionText() {
		return this.serviceDescriptionText;
	}

	public ServiceType getServiceType() {
		return this.serviceType;
	}

	public String getServiceInterfaceLocation() {
		return this.serviceInterfaceLocation;
	}

	public String getLocationURL() {
		return this.locationURL;
	}

	public String getOrganisationName() {
		return this.organisationName;
	}

	/* We do not have any setter methods for now! */
}
