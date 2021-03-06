package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ArrayTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.BaseTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class EncodedBodyBuilder extends AbstractBodyBuilder {
	public EncodedBodyBuilder(String style, WSDLBasedProcessor processor) {
		super(style, processor);
	}

	@Override
	protected Use getUse() {
		return Use.ENCODED;
	}

	@Override
	public SOAPBodyElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		SOAPBodyElement result = super.build(inputMap);
		for (Iterator iterator = namespaceMappings.keySet().iterator(); iterator
				.hasNext();) {
			String namespaceURI = (String) iterator.next();
			String ns = (String) namespaceMappings.get(namespaceURI);
			result.addNamespaceDeclaration(ns, namespaceURI);
		}
		result.setAttribute("soapenv:encodingStyle",
				"http://schemas.xmlsoap.org/soap/encoding/");
		return result;
	}

	protected Element createSkeletonElementForSingleItem(
			Map<String, String> namespaceMappings, TypeDescriptor descriptor,
			String inputName, String typeName) {
		Element el = XMLUtils.StringToElement("", inputName, "");

		String ns = namespaceMappings.get(descriptor.getNamespaceURI());
		if (ns != null) {
			el.setAttribute("xsi:type", ns + ":" + descriptor.getType());
		}
		return el;
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

		el = XMLUtils.StringToElement("", inputName, "");

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

		String ns = namespaceMappings.get(elementType.getNamespaceURI());
		if (ns != null) {
			String elementNS = ns + ":" + elementType.getType() + "[" + size
					+ "]";
			el.setAttribute("soapenc:arrayType", elementNS);
			el.setAttribute("xmlns:soapenc",
					"http://schemas.xmlsoap.org/soap/encoding/");
		}

		el.setAttribute("xsi:type", "soapenc:Array");

		return el;
	}

	@Override
	protected SOAPBodyElement addElementToBody(String operationNamespace, SOAPBodyElement body, Element el) throws SOAPException {
		body.addChildElement(new SOAPBodyElement(el));
		return body;
	}

	
}
