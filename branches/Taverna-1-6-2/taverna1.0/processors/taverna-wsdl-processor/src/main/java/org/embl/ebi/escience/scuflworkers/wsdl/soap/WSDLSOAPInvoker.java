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
 * Revision           $Revision: 1.14.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-11-09 15:44:23 $
 *               by   $Author: sowen70 $
 * Created on 07-Apr-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.wsdl.WSDLException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.providers.soap.apacheaxis.WSIFOperation_ApacheAxis;
import org.apache.wsif.providers.soap.apacheaxis.WSIFPort_ApacheAxis;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;

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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public Map invoke(Map inputMap, EngineConfiguration config)
			throws Exception {

		Call call = getCall();
		if (config != null) {
			call.setClientHandlers(config.getGlobalRequest(), config
					.getGlobalResponse());
		}
		call.setTimeout(getTimeout());

		BodyBuilder builder = BodyBuilderFactory.instance().create(
				getProcessor());
		SOAPBodyElement body = builder.build(inputMap);

		SOAPEnvelope requestEnv = new SOAPEnvelope();

		requestEnv.addBodyElement(body);

		SOAPEnvelope responseEnv = call.invoke(requestEnv);
		
		Map result;
		if (responseEnv == null) {
			if (processor.getOutputPorts().length == 1
					&& processor.getOutputPorts()[0].getName().equals(
							"attachmentList")) {
				// Could be axis 2 service with no output (TAV-617)
				logger.info("No output from WSDL-processor: " + processor);
				result = new HashMap();
			} else {
				throw new IllegalStateException(
						"Missing expected outputs from service");
			}
		} else {
			List response = responseEnv.getBodyElements();
			logger.info("SOAP response was:" + response);
			SOAPResponseParser parser = SOAPResponseParserFactory.instance()
					.create(response, getUse(), getStyle(),
							getProcessor().getOutputPorts());
			result = parser.parse(response);
		}
		
		result.put("attachmentList", extractAttachmentsDataThing(call));

		return result;
	}

	/**
	 * Reads the property taverna.wsdl.timeout, default to 5 minutes if missing.
	 * 
	 * @return
	 */
	private Integer getTimeout() {
		int result = 300000;
		String minutesStr = System.getProperty("taverna.wsdl.timeout");

		if (minutesStr == null) {
			logger
					.warn("Missing property for taverna.wsdl.timeout. Using default of 5 minutes");
			return result;
		}
		try {
			int minutes = Integer.parseInt(minutesStr.trim());
			result = minutes * 1000 * 60;
		} catch (NumberFormatException e) {
			logger
					.error(
							"Error with number format for timeout setting taverna.wsdl.timeout",
							e);
			return result;
		}
		logger.info("Using a timout of " + result + "ms");
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
	 * Exctracts any attachments that result from invoking the service, and
	 * returns them as a List wrapped within a DataThing
	 * 
	 * @param axisCall
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private DataThing extractAttachmentsDataThing(Call axisCall)
			throws SOAPException, IOException {
		List attachmentList = new ArrayList();
		if (axisCall.getResponseMessage()!=null && axisCall.getResponseMessage().getAttachments()!=null) {
			for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
					.hasNext();) {
				AttachmentPart ap = (AttachmentPart) i.next();
				logger.debug("Found attachment filename : "
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
		}
		
		DataThing attachmentThing = DataThingFactory.bake(attachmentList);
		if (axisCall.getResponseMessage()!=null && axisCall.getResponseMessage().getAttachments()!=null) {
			for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
					.hasNext();) {
				String mimeType = ((AttachmentPart) i.next()).getDataHandler()
						.getContentType();
				attachmentThing.getMetadata().addMIMEType(mimeType);
			}
		}
		
		return attachmentThing;
	}
	
}
