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
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-10-18 18:07:31 $
 *               by   $Author: sowen70 $
 * Created on 07-Apr-2006
 *****************************************************************/
package net.sf.taverna.t2.activities.wsdl.soap;

import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.activities.wsdl.parser.UnknownOperationException;
import net.sf.taverna.t2.activities.wsdl.parser.WSDLParser;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFOperation_ApacheAxis;
import org.apache.wsif.providers.soap.apacheaxis.WSIFPort_ApacheAxis;

/**
 * Invokes SOAP based webservices
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class WSDLSOAPInvoker {

	private static Logger logger = Logger.getLogger(WSDLSOAPInvoker.class);

	private WSDLParser parser;
	private String operationName;

	public WSDLSOAPInvoker(WSDLParser parser, String operationName, List<String> outputNames) {
		this.parser=parser;
		this.operationName=operationName;
	}

	/**
	 * Invokes the webservice with the supplied input Map, and returns a Map
	 * containing the outputs, mapped against their output names.
	 * 
	 * @param inputMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> invoke(Map inputMap) throws Exception {
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
	public Map invoke(Map inputMap, EngineConfiguration config)
			throws Exception {

		Call call = getCall();
		if (config != null) {
			call.setClientHandlers(config.getGlobalRequest(), config
					.getGlobalResponse());
		}
		call.setTimeout(getTimeout());
		
		BodyBuilder builder = BodyBuilderFactory.instance().create(parser,operationName,parser.getOperationInputParameters(operationName));
		SOAPBodyElement body = builder.build(inputMap);

		SOAPEnvelope requestEnv = new SOAPEnvelope();

		requestEnv.addBodyElement(body);

		SOAPEnvelope responseEnv = call.invoke(requestEnv);

		List response = responseEnv.getBodyElements();

		logger.info("SOAP response was:" + response);

		SOAPResponseParser responseParser = SOAPResponseParserFactory.instance()
				.create(response, getUse(), getStyle(),
						this.parser.getOperationOutputParameters(operationName));
		Map result = responseParser.parse(response);

		//FIXME: need to handle attachments
		//result.put("attachmentList", extractAttachmentsDataThing(call));

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
		return parser.getStyle();
	}

	private String getUse() throws UnknownOperationException {
		return parser.getUse(operationName);
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

		String use = parser.getUse(operationName);
		Call result = (((WSIFPort_ApacheAxis) ((WSIFOperation_ApacheAxis) getWSIFOperation()).getWSIFPort()).getCall());
		result.setUseSOAPAction(true);
		result.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		result.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		result
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		if (use.equalsIgnoreCase("literal")) {
			result.setSOAPActionURI(parser.getSOAPActionURI(
					operationName));
			result.setEncodingStyle(null);
		}

		return result;
	}
	
	private WSIFOperation getWSIFOperation() throws WSIFException {
		Definition def = parser.getDefinition();
		Service s = (Service) def.getServices().values().toArray()[0];
		WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
		PortType portType = parser.getPortType(operationName);
		WSIFService service = factory.getService(def, s, portType);
		WSIFPort port = service.getPort();
		WSIFOperation op = port.createOperation(operationName);		
		return op;
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
//	private DataThing extractAttachmentsDataThing(Call axisCall)
//			throws SOAPException, IOException {
//		List attachmentList = new ArrayList();
//		for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
//				.hasNext();) {
//			AttachmentPart ap = (AttachmentPart) i.next();
//			logger.debug("Found attachment filename : "
//					+ ap.getAttachmentFile());
//			DataHandler dh = ap.getDataHandler();
//			BufferedInputStream bis = new BufferedInputStream(dh
//					.getInputStream());
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			int c;
//			while ((c = bis.read()) != -1) {
//				bos.write(c);
//			}
//			bis.close();
//			bos.close();
//			String mimeType = dh.getContentType();
//			if (mimeType.matches(".*image.*") || mimeType.matches(".*octet.*")
//					|| mimeType.matches(".*audio.*")
//					|| mimeType.matches(".*application/zip.*")) {
//				attachmentList.add(bos.toByteArray());
//			} else {
//				attachmentList.add(new String(bos.toByteArray()));
//			}
//		}
//		DataThing attachmentThing = DataThingFactory.bake(attachmentList);
//		for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
//				.hasNext();) {
//			String mimeType = ((AttachmentPart) i.next()).getDataHandler()
//					.getContentType();
//			attachmentThing.getMetadata().addMIMEType(mimeType);
//		}
//		return attachmentThing;
//	}
}

	
	
