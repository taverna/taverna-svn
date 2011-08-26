/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.*;
import java.util.*;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.log4j.Logger;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.providers.soap.apacheaxis.WSIFOperation_ApacheAxis;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * The task required to invoke an arbitrary web service.
 * 
 * @author Tom Oinn
 */
public class WSDLInvocationTask implements ProcessorTaskWorker {

	private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);

	private static final int INVOCATION_TIMEOUT = 0;

	// private static Service service = new org.apache.axis.client.Service();
	private WSDLBasedProcessor processor;

	public WSDLInvocationTask(Processor p) {
		this.processor = (WSDLBasedProcessor) p;
	}

	public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {
		try {
			// Obtain an instance of the WSIFOperation from the parent processor
			WSIFOperation operation = processor.getWSIFOperation();
			WSIFMessage input = operation.createInputMessage();
			WSIFMessage output = operation.createOutputMessage();
			WSIFMessage fault = operation.createFaultMessage();
			// Iterate over the inputs...
			for (int i = 0; i < processor.inNames.length; i++) {
				Object value = null;
				Class c = processor.inTypes[i];
				String argName = processor.inNames[i];
				DataThing inputObject = (DataThing) inputMap.get(argName);
				
				if (inputObject == null) {
					throw new TaskExecutionException("Input to web service '" + argName
							+ "' was defined but not provided.");
				}
				// Check whether the input port has been flagged as text/xml
				// and create a DOM Node if so
				if (c.equals(org.w3c.dom.Element.class)) {
					try {
						System.out.println("Trying to create dom...");
						// create a new Document
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						String dataObject = (String) inputObject.getDataObject();
						Document doc = builder.parse(new ByteArrayInputStream(dataObject.getBytes()));
						value = doc.getDocumentElement();
					} catch (Exception ex) {
						throw new TaskExecutionException(
								"Operation requires an XML complex type but\n invalid XML was supplied : "
										+ ex.getMessage());
					}					
				} else if (c.equals(String[].class)) {
					// Should have a collection of String here
					System.out.println("Building string[]");
					List l = (List) inputObject.getDataObject();
					value = (String[]) l.toArray(new String[0]);
				} else {
					// If the datathing contains a string and the service
					// wants something else...
					if (inputObject.getDataObject() instanceof String) {
						String argString = (String) inputObject.getDataObject();
						if (c.equals(Double.TYPE)) {
							value = new Double(argString);
						} else if (c.equals(Float.TYPE)) {
							value = new Float(argString);
						} else if (c.equals(Integer.TYPE)) {
							value = new Integer(argString);
						} else if (c.equals(Boolean.TYPE)) {
							value = new Boolean(argString);
						}
					}
				}
				if (value == null) {
					value = inputObject.getDataObject();
				}
				
				input.setObjectPart(processor.inNames[i], value);
			}

			boolean respOK = operation.executeRequestResponseOperation(input, output, fault);

			if (respOK) {
				// if (true){
				// Operation succeeded - extract output parts
				Map resultMap = new HashMap();
				for (int i = 0; i < processor.outNames.length; i++) {
					String outputName = processor.outNames[i];
					Object resultObject = output.getObjectPart(outputName);
					if (resultObject instanceof Node) {
						// If the output is an instance of Node then
						// convert it to a text/xml form.
						Node node = (Node) resultObject;
						TransformerFactory tFactory = TransformerFactory.newInstance();
						Transformer transformer = tFactory.newTransformer();
						DOMSource source = new DOMSource(node.getOwnerDocument());
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						transformer.transform(source, new StreamResult(baos));
						resultObject = baos.toString();
					} else if (resultObject instanceof Boolean || resultObject instanceof Number) {
						resultObject = resultObject.toString();
					}
					resultMap.put(outputName, new DataThing(resultObject));
				}
				// Can we extract attachments here?
				org.apache.axis.client.Call axisCall = ((WSIFOperation_ApacheAxis) operation).getDynamicWSIFPort()
						.getCall();
				List attachmentList = new ArrayList();
				for (Iterator i = axisCall.getResponseMessage().getAttachments(); i.hasNext();) {
					AttachmentPart ap = (AttachmentPart) i.next();
					System.out.println("Found attachment filename : " + ap.getAttachmentFile());
					DataHandler dh = ap.getDataHandler();
					BufferedInputStream bis = new BufferedInputStream(dh.getInputStream());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					int c;
					while ((c = bis.read()) != -1) {
						bos.write(c);
					}
					bis.close();
					bos.close();
					// Get the MIME type
					String mimeType = dh.getContentType();
					// if textual (i.e text/foo) then write to a string. Default
					// to
					// a string unless the type is an image or explicit octet
					// stream.
					if (mimeType.matches(".*image.*") || mimeType.matches(".*octet.*") || mimeType.matches(".*audio.*")
							|| mimeType.matches(".*application/zip.*")) {
						attachmentList.add(bos.toByteArray());
					} else {
						attachmentList.add(new String(bos.toByteArray()));
					}
				}
				DataThing attachmentThing = new DataThing(attachmentList);
				for (Iterator i = axisCall.getResponseMessage().getAttachments(); i.hasNext();) {
					String mimeType = ((AttachmentPart) i.next()).getDataHandler().getContentType();
					attachmentThing.getMetadata().addMIMEType(mimeType);
				}
				resultMap.put("attachmentList", attachmentThing);

				return resultMap;
			} else {
				// Operation failed - extract failure parts
				StringBuffer errorMessage = new StringBuffer();
				errorMessage.append("Error from WSIF based invocation :\n");
				for (Iterator i = fault.getPartNames(); i.hasNext();) {
					errorMessage.append("*");
					String name = (String) i.next();
					errorMessage.append(fault.getObjectPart(name).toString());
					if (i.hasNext()) {
						errorMessage.append("\n");
					}
				}
				throw new TaskExecutionException(errorMessage.toString());
			}
		} catch (Exception ex) {
			if (ex instanceof TaskExecutionException) {
				throw (TaskExecutionException) ex;
			}
			ex.printStackTrace();
			TaskExecutionException te = new TaskExecutionException("Error occured during invocation " + ex.getMessage());
			te.initCause(ex);
			throw te;
		}
	}

}
