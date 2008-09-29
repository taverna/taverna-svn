package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ArrayTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.BaseTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * An implementation of BodyBuilder that supports creating the SOAP body for
 * Webservices based upon a WSDL with Literal style.
 * 
 * @author Stuart Owen
 * 
 */
public class LiteralBodyBuilder extends AbstractBodyBuilder {

	private static Logger logger = Logger.getLogger(LiteralBodyBuilder.class);

	public LiteralBodyBuilder(String style, WSDLBasedProcessor processor) {
		super(style, processor);
	}

	@Override
	protected Use getUse() {
		return Use.LITERAL;
	}

	@Override
	public SOAPBodyElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		SOAPBodyElement body = super.build(inputMap);

		if (getStyle() == Style.DOCUMENT) {
			stripTypeAttributes(body);
		}

		return body;
	}

	protected Element createSkeletonElementForSingleItem(
			Map<String, String> namespaceMappings, TypeDescriptor descriptor,
			String inputName, String typeName) {
		if (getStyle()==Style.DOCUMENT) {
			return XMLUtils.StringToElement("", descriptor.getQname().getLocalPart(), "");
		}
		else {
			return XMLUtils.StringToElement("", inputName, "");
		}
	}

	private void stripTypeAttributes(Node parent) {
		if (parent.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element) parent;
			if (parent.hasAttributes()) {
				NamedNodeMap map = parent.getAttributes();
				List<Node> attributeNodesForRemoval = new ArrayList<Node>();
				for (int i = 0; i < map.getLength(); i++) {
					Node node = map.item(i);

					if ((node.getLocalName() != null && node.getLocalName()
							.equals("type"))
							|| (node.getPrefix() != null && node.getPrefix()
									.equals("xmlns"))) {
						attributeNodesForRemoval.add(node);
					}
				}

				for (Node node : attributeNodesForRemoval) {
					if (logger.isDebugEnabled())
						logger.debug("Removing attribute from body: {"
								+ el.getNamespaceURI() + "}"
								+ el.getLocalName());
					el.removeAttributeNS(node.getNamespaceURI(), node
							.getLocalName());
				}
			}
		}

		if (parent.hasChildNodes()) {
			for (int i = 0; i < parent.getChildNodes().getLength(); i++)
				stripTypeAttributes(parent.getChildNodes().item(i));
		}
	}

	@Override
	protected Element createElementForArrayType(
			Map<String, String> namespaceMappings, String inputName,
			DataThing thing, TypeDescriptor descriptor, String mimeType,
			String typeName) throws ParserConfigurationException, SAXException,
			IOException, UnknownOperationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();

		Element el;
		ArrayTypeDescriptor arrayDescriptor = (ArrayTypeDescriptor) descriptor;
		TypeDescriptor elementType = arrayDescriptor.getElementType();
		int size = 0;

		el = XMLUtils.StringToElement("", typeName, "");

		if (thing.getDataObject() instanceof List) {
			List dataValues = (List) thing.getDataObject();
			size = dataValues.size();
			populateElementWithList(mimeType, el, dataValues, elementType);
		} else {
			Object dataItem = thing.getDataObject();
			// if mime type is text/xml then the data is an array in xml form,
			// else its just a single primitive element
			if (mimeType.equals("'text/xml'")) {

				Document doc = docBuilder.parse(new ByteArrayInputStream(
						dataItem.toString().getBytes()));
				Node child = doc.getDocumentElement().getFirstChild();

				while (child != null) {
					size++;
					el.appendChild(el.getOwnerDocument()
							.importNode(child, true));
					child = child.getNextSibling();
				}
			} else {
				String tag = "item";
				if (elementType instanceof BaseTypeDescriptor) {
					tag = elementType.getType();
				} else {
					tag = elementType.getName();
				}
				Element item = el.getOwnerDocument().createElement(tag);
				populateElementWithObjectData(mimeType, item, dataItem);
				el.appendChild(item);
			}

		}

		return el;
	}

	@Override
	protected SOAPBodyElement addElementToBody(String operationNamespace, SOAPBodyElement body, Element el) throws SOAPException {
		if (getStyle()==Style.DOCUMENT) {
			body = new SOAPBodyElement(el);
			body.setNamespaceURI(operationNamespace);
		}
		else {
			body.addChildElement(new SOAPBodyElement(el));
		}
		return body;
	}
	
	

}
