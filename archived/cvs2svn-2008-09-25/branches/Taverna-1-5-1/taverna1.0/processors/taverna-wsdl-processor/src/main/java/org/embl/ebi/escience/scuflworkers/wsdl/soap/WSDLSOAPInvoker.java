/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WSDLSOAPInvoker.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-15 10:49:52 $
 *               by   $Author: davidwithers $
 * Created on 07-Apr-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.providers.soap.apacheaxis.WSIFOperation_ApacheAxis;
import org.apache.wsif.providers.soap.apacheaxis.WSIFPort_ApacheAxis;
import org.embl.ebi.escience.baclava.Base64;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ArrayTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.BaseTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ComplexTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Invokes SOAP based webservices
 * 
 * @author sowen
 * 
 */

public class WSDLSOAPInvoker {

	private static Logger logger = Logger.getLogger(WSDLSOAPInvoker.class);

	private WSDLBasedProcessor processor = null;

	public WSDLSOAPInvoker(WSDLBasedProcessor processor) {
		this.processor = processor;
	}

	/**
	 * Invokes the webservice with the supplied input Map, and returns a Map
	 * containing the outputs, mapped against their output names.
	 * 
	 * @param inputMap
	 * @return
	 * @throws Exception
	 */
	public Map invoke(Map inputMap) throws Exception {
		return invoke(inputMap, null);
	}

	/**
	 * Invokes the webservice with the supplied input Map, and returns a Map
	 * containing the outputs, mapped against their output names.
	 * 
	 * @param inputMap
	 * @return
	 * @throws Exception
	 */
	public Map invoke(Map inputMap, EngineConfiguration config) throws Exception {

		Call call = getCall();
		if (config != null) {
			call.setClientHandlers(config.getGlobalRequest(), config
					.getGlobalResponse());
		}
		call.setTimeout(getTimeout());
		SOAPBodyElement body = buildBody(inputMap);

		List response = (List) call.invoke(new Object[] { body });

		logger.info("SOAP response was:" + response);

		SOAPResponseParser parser = SOAPResponseParserFactory.instance()
				.create(response, getUse(), getStyle(),
						getProcessor().getOutputPorts());
		Map result = parser.parse(response);

		result.put("attachmentList", extractAttachmentsDataThing(call));

		return result;
	}
	
	/**
	 * Reads the property taverna.wsdl.timeout, default to 5 minutes if missing.
	 * @return
	 */
	private Integer getTimeout() {
		int result=300000;
		String minutesStr=System.getProperty("taverna.wsdl.timeout");
		
		if (minutesStr==null) {
			logger.warn("Missing property for taverna.wsdl.timeout. Using default of 5 minutes");
			return result;
		}
		try {
			int minutes=Integer.parseInt(minutesStr.trim());
			result=minutes*1000*60;
		}
		catch(NumberFormatException e) {
			logger.error("Error with number format for timeout setting taverna.wsdl.timeout",e);
			return result;
		}		
		logger.info("Using a timout of "+result+"ms");
		return result;
	}

	private String getStyle() {
		return getProcessor().getParser().getStyle();
	}

	private String getUse() throws UnknownOperationException {
		return getProcessor().getParser().getUse(
				getProcessor().getOperationName());
	}

	private WSDLBasedProcessor getProcessor() {
		return processor;
	}

	/**
	 * Returns an axis based Call, initialised for the operation that needs to
	 * be invoked
	 * 
	 * @return
	 * @throws ServiceException
	 * @throws UnknownOperationException
	 * @throws WSDLException
	 * @throws WSIFException
	 */
	private Call getCall() throws ServiceException, UnknownOperationException,
			WSDLException, WSIFException {

		WSDLBasedProcessor processor = getProcessor();
		String operationName = processor.getOperationName();
		String use = processor.getParser().getUse(operationName);
		Call result = (((WSIFPort_ApacheAxis) ((WSIFOperation_ApacheAxis) processor
				.getWSIFOperation()).getWSIFPort()).getCall());		
		result.setUseSOAPAction(true);
		result.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		result.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		result
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		if (use.equalsIgnoreCase("literal")) {
			result.setSOAPActionURI(processor.getParser().getSOAPActionURI(
					operationName));
			result.setEncodingStyle(null);
		}

		return result;
	}

	/**
	 * 
	 * @return the namespace for the operation
	 */
	private String getOperationNamespace() throws UnknownOperationException {
		return getProcessor().getParser().getOperationNamespaceURI(
				getProcessor().getOperationName());
	}

	/**
	 * Create and populates a SOAP body for the operation being called, and for
	 * the given input map. The body is correctly structered according to the
	 * operation style, and the inputs provided.
	 * 
	 * @param inputMap
	 * @return a populated SOAPBodyElement
	 * @throws WSDLException
	 * @throws ParserConfigurationException
	 * @throws SOAPException
	 * @throws IOException
	 * @throws SAXException
	 * @throws UnknownOperationException
	 */
	private SOAPBodyElement buildBody(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		String operationName = getProcessor().getOperationName();

		List inputs = getProcessor().getParser().getOperationInputParameters(
				operationName);

		Map<String, String> namespaceMappings = generateNamespaceMappings(inputs);
		String operationNamespace = getOperationNamespace();

		SOAPBodyElement body = new SOAPBodyElement(XMLUtils.StringToElement(
				operationNamespace, operationName, ""));

		// its important to preserve the order of the inputs!
		for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
			TypeDescriptor descriptor = (TypeDescriptor) iterator.next();
			String inputName = descriptor.getName();
			DataThing thing = (DataThing) inputMap.get(inputName);

			if (thing == null)
				logger.warn("No input named: '" + inputName
						+ "' provided to invoke service: '" + operationName);
			else {
				String mimeType = getMimeTypeForInputName(inputName);
				String typeName = descriptor.getType();

				Element el = null;

				if (descriptor instanceof ArrayTypeDescriptor) {
					el = createElementForArrayType(namespaceMappings,
							inputName, thing, descriptor, mimeType, typeName);

				} else {
					Object dataValue = thing.getDataObject();
					if (getUse().equals("literal"))
						el = XMLUtils.StringToElement("", typeName, "");
					else
						el = XMLUtils.StringToElement("", inputName, "");

					String ns = namespaceMappings.get(descriptor
							.getNamespaceURI());
					if (ns != null) {
						el.setAttribute("xsi:type", ns + ":"
								+ descriptor.getType());
					}

					populateElementWithObjectData(mimeType, el, dataValue);
				}

				if (getUse().equals("literal")) {
					body = new SOAPBodyElement(el);
					body.setNamespaceURI(operationNamespace);
				} else {
					body.addChildElement(new SOAPBodyElement(el));
				}
			}
		}

		for (Iterator iterator = namespaceMappings.keySet().iterator(); iterator
				.hasNext();) {
			String namespaceURI = (String) iterator.next();
			String ns = (String) namespaceMappings.get(namespaceURI);
			if (!ns.equals("xsd") && !ns.equals("xsi")) {
				body.addNamespaceDeclaration(ns, namespaceURI);
			}
		}

		if (getProcessor().getParser().getUse(operationName).equals("encoded")) {
			body.setAttribute("soapenv:encodingStyle",
					"http://schemas.xmlsoap.org/soap/encoding/");
		}

		if (logger.isInfoEnabled()) {
			try {
				logger.info("Generated SOAP body:" + body.getAsString());
			} catch (Exception e) {
				logger.warn("Cant display soap body", e);
			}
		}

		return body;
	}

	/**
	 * generates an XML DOM Element for an array
	 * 
	 * @param namespaceMappings
	 * @param inputName
	 * @param thing
	 * @param descriptor
	 * @param mimeType
	 * @param typeName
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Element createElementForArrayType(
			Map<String, String> namespaceMappings, String inputName,
			DataThing thing, TypeDescriptor descriptor, String mimeType,
			String typeName) throws ParserConfigurationException, SAXException,
			IOException {
		Element el;
		ArrayTypeDescriptor arrayDescriptor = (ArrayTypeDescriptor) descriptor;
		TypeDescriptor elementType = arrayDescriptor.getElementType();
		int size = 0;

		if (getStyle().equals("document"))
			el = XMLUtils.StringToElement("", typeName, "");
		else
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
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(new ByteArrayInputStream(dataItem
						.toString().getBytes()));
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

	/**
	 * Populates a DOM XML Element with the contents of a List of dataValues
	 * 
	 * @param mimeType -
	 *            the mime type of the data
	 * @param element -
	 *            the Element to be populated
	 * @param dataValues -
	 *            the List of Objects containing the data
	 * @param elementType -
	 *            the TypeDescriptor for the element being populated
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void populateElementWithList(String mimeType, Element element,
			List dataValues, TypeDescriptor elementType)
			throws ParserConfigurationException, SAXException, IOException {
		for (Iterator dataIterator = dataValues.iterator(); dataIterator
				.hasNext();) {
			Object dataItem = dataIterator.next();
			String tag;
			if (elementType instanceof BaseTypeDescriptor) {
				tag = elementType.getType();
			} else {
				tag = elementType.getName();
			}

			Element item = element.getOwnerDocument().createElement(tag);
			populateElementWithObjectData(mimeType, item, dataItem);
			element.appendChild(item);
		}
	}

	/**
	 * Populates a DOM XML Element with dataValue according to its mimetype
	 * 
	 * @param mimeType
	 * @param element
	 * @param dataValue
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void populateElementWithObjectData(String mimeType,
			Element element, Object dataValue)
			throws ParserConfigurationException, SAXException, IOException {
		if (mimeType.equals("'text/xml'")) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(dataValue
					.toString().getBytes()));
			Node child = doc.getDocumentElement().getFirstChild();

			while (child != null) {
				element.appendChild(element.getOwnerDocument().importNode(
						child, true));
				child = child.getNextSibling();
			}
		} else if (mimeType.equals("'application/octet-stream'")
				&& dataValue instanceof byte[]) {
			String encoded = Base64.encodeBytes((byte[]) dataValue);
			element.appendChild(element.getOwnerDocument().createTextNode(
					encoded));
		} else {
			element.appendChild(element.getOwnerDocument().createTextNode(
					dataValue.toString()));
		}
	}

	/**
	 * Provides the mime type for a given input
	 * 
	 * @param inputName
	 * @return
	 */
	private String getMimeTypeForInputName(String inputName) {
		InputPort[] inputPorts = getProcessor().getInputPorts();
		for (int i = 0; i < inputPorts.length; i++) {
			if (inputPorts[i].getName().equals(inputName)) {
				return inputPorts[i].getSyntacticType();
			}
		}
		return "";
	}

	/**
	 * Generates a map of all the namespaces for the operation and all of the
	 * types required to call the operation. Namesspace prefixes (the key) start
	 * with ns1 representing the operation, and continue incrementally for all
	 * additional namespaces (ns2, ns3 ... etc).
	 * 
	 * @return
	 * @param inputs -
	 *            List of input TypeDescriptor's
	 * @throws UnknownOperationException
	 * @throws IOException
	 */
	private Map<String, String> generateNamespaceMappings(List inputs)
			throws UnknownOperationException, IOException {
		Map<String, String> result = new HashMap<String, String>();
		int nsCount = 2;

		result.put(getOperationNamespace(), "ns1");
		result.put("http://www.w3.org/2001/XMLSchema", "xsd");
		result.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

		for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
			TypeDescriptor descriptor = (TypeDescriptor) iterator.next();
			nsCount = mapNamespace(descriptor, result, nsCount);

		}

		return result;
	}

	/**
	 * creates a namespace prefix and adds the namespace to the namespaceMap for
	 * a TypeDescriptor. Further recursive calls are made if this type contains
	 * addition inner elements that are not already mapped.
	 * 
	 * @param descriptor
	 * @param namespaceMap
	 * @param nsCount
	 * @return
	 */
	private int mapNamespace(TypeDescriptor descriptor, Map namespaceMap,
			int nsCount) {
		String namespace = descriptor.getNamespaceURI();
		if (!namespaceMap.containsKey(namespace)) {
			namespaceMap.put(namespace, "ns" + nsCount);
			nsCount++;
		}

		if (descriptor instanceof ArrayTypeDescriptor) {
			nsCount = mapNamespace(((ArrayTypeDescriptor) descriptor)
					.getElementType(), namespaceMap, nsCount);
		} else if (descriptor instanceof ComplexTypeDescriptor) {
			List elements = ((ComplexTypeDescriptor) descriptor).getElements();
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				nsCount = mapNamespace((TypeDescriptor) iterator.next(),
						namespaceMap, nsCount);
			}
		}

		return nsCount;
	}

	/**
	 * Exctracts any attachments that result from invoking the service, and
	 * returns them as a List wrapped within a DataThing
	 * 
	 * @param axisCall
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	private DataThing extractAttachmentsDataThing(Call axisCall)
			throws SOAPException, IOException {
		List attachmentList = new ArrayList();
		for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
				.hasNext();) {
			AttachmentPart ap = (AttachmentPart) i.next();
			System.out.println("Found attachment filename : "
					+ ap.getAttachmentFile());
			DataHandler dh = ap.getDataHandler();
			BufferedInputStream bis = new BufferedInputStream(dh
					.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int c;
			while ((c = bis.read()) != -1) {
				bos.write(c);
			}
			bis.close();
			bos.close();
			String mimeType = dh.getContentType();
			if (mimeType.matches(".*image.*") || mimeType.matches(".*octet.*")
					|| mimeType.matches(".*audio.*")
					|| mimeType.matches(".*application/zip.*")) {
				attachmentList.add(bos.toByteArray());
			} else {
				attachmentList.add(new String(bos.toByteArray()));
			}
		}
		DataThing attachmentThing = DataThingFactory.bake(attachmentList);
		for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
				.hasNext();) {
			String mimeType = ((AttachmentPart) i.next()).getDataHandler()
					.getContentType();
			attachmentThing.getMetadata().addMIMEType(mimeType);
		}
		return attachmentThing;
	}

}
