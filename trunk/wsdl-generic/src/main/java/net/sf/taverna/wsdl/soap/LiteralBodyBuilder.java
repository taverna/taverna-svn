package net.sf.taverna.wsdl.soap;

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

import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
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
@SuppressWarnings("unchecked")
public class LiteralBodyBuilder extends AbstractBodyBuilder {

	public LiteralBodyBuilder(String style, WSDLParser parser, String operationName, List<TypeDescriptor> inputDescriptors) {
		super(style, parser, operationName,inputDescriptors);
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

	@Override
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
			Object dataValue, TypeDescriptor descriptor, String mimeType,
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

		if (dataValue instanceof List) {
			List dataValues = (List) dataValue;
			size = dataValues.size();
			populateElementWithList(mimeType, el, dataValues, elementType);
		} else {
			
			// if mime type is text/xml then the data is an array in xml form,
			// else its just a single primitive element
			if (mimeType.equals("'text/xml'")) {

				Document doc = docBuilder.parse(new ByteArrayInputStream(
						dataValue.toString().getBytes()));
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
				populateElementWithObjectData(mimeType, item, dataValue);
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
