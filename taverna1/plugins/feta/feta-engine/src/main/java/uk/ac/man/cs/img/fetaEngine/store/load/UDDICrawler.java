/*
 *
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
 */
package uk.ac.man.cs.img.fetaEngine.store.load;

/**
 * @author alperp
 * 
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.OverviewDoc;
import org.uddi4j.datatype.binding.BindingTemplate;
import org.uddi4j.datatype.binding.BindingTemplates;
import org.uddi4j.datatype.binding.InstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceInfo;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.ServiceInfo;
import org.uddi4j.response.ServiceInfos;
import org.uddi4j.response.ServiceList;
import org.uddi4j.response.TModelInfo;
import org.uddi4j.response.TModelList;
import org.uddi4j.transport.TransportFactory;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.TModelBag;

public class UDDICrawler {

	public static void main(String args[]) {
		UDDICrawler app = new UDDICrawler();
		System.out.println("\n*********** Running UDDICrawler ***********");
		try {
			List resultList = app
					.getXMLURLs("http://fantasio.ecs.soton.ac.uk:8080/grimoires/services/inquire");
			// app.getXMLURLs("http://oescedge.oucs.ox.ac.uk/juddi/inquiry");
			for (int i = 0; i < resultList.size(); i++) {
				System.out.println(resultList.get(i).toString());
			}

		} catch (FetaLoadException fe) {
			fe.printStackTrace();
		}

		System.exit(0);
	}

	public List getXMLURLs(String inquiryURL) throws FetaLoadException {

		System.out.println("The inquiry URL is" + inquiryURL);

		List allPedroXMLURLS = new ArrayList();

		// Configure UDDI4J system properties. Normally set on commandline or
		// elsewhere
		// SOAP transport being used
		if (System.getProperty(TransportFactory.PROPERTY_NAME) == null) {
			System.setProperty(TransportFactory.PROPERTY_NAME,
					"org.uddi4j.transport.ApacheSOAPTransport");
		}
		// Logging
		if (System.getProperty("org.uddi4j.logEnabled") == null) {
			System.setProperty("org.uddi4j.logEnabled", "false");
		}

		// Configure JSSE support
		try {
			System.setProperty("java.protocol.handler.pkgs",
					"com.sun.net.ssl.internal.www.protocol");

			// Dynamically loads security provider based on properties.
			// Typically configured in JRE
			java.security.Security.addProvider((java.security.Provider) Class
					.forName("com.sun.net.ssl.internal.ssl.Provider")
					.newInstance());
		} catch (Exception e) {
			System.out
					.println("Error configuring JSSE provider. Make sure JSSE is in classpath.\n"
							+ e.getMessage());
		}

		// Construct a UDDIProxy object.
		UDDIProxy proxy = new UDDIProxy();

		try {
			// Select the desired UDDI server node
			proxy.setInquiryURL(inquiryURL);

			// Setting FindQualifiers to 'CASE SENSITIVE MATCH'
			FindQualifiers findQualifiers = new FindQualifiers();
			Vector qualifier = new Vector();
			qualifier.add(new FindQualifier("caseSensitiveMatch"));
			findQualifiers.setFindQualifierVector(qualifier);

			// Find the Feta tModel
			// And setting the maximum rows to be returned as 1.
			TModelList fetaList = proxy.find_tModel("TEsting", null, null,
					findQualifiers, 1);

			Vector feta_tModelInfoVector = fetaList.getTModelInfos()
					.getTModelInfoVector();
			int i = feta_tModelInfoVector.size();
			String fetaKEY = "947589347853";
			if (i > 0) {
				System.out.println("Feta TModel exists there are " + i
						+ " of them");

				TModelInfo feta_tModelInfo = (TModelInfo) feta_tModelInfoVector
						.elementAt(0);
				// Print name of feta tModel
				System.out.println("first one's name is"
						+ feta_tModelInfo.getNameString());
				System.out.println("first one's key is"
						+ feta_tModelInfo.getTModelKey());
				fetaKEY = feta_tModelInfo.getTModelKey();

				feta_tModelInfo = (TModelInfo) feta_tModelInfoVector
						.elementAt(1);
				System.out.println("first one's name is"
						+ feta_tModelInfo.getNameString());
				System.out.println("first one's key is"
						+ feta_tModelInfo.getTModelKey());

			}

			TModelBag fetaBag = new TModelBag();
			Vector fetaVect = new Vector();
			fetaVect.add(fetaKEY);
			fetaBag.setTModelKeyStrings(fetaVect);
			ServiceList sl = (ServiceList) proxy.find_service(null, null, null,
					fetaBag, null, 0);
			ServiceInfos myServInf = new ServiceInfos();
			myServInf = sl.getServiceInfos();
			Vector serviceInfoVector = myServInf.getServiceInfoVector();
			int length = serviceInfoVector.size();

			for (int j = 0; j < length; j++) {
				ServiceInfo sinfo = (ServiceInfo) serviceInfoVector
						.elementAt(j);
				String serviceKey = sinfo.getServiceKey();

				System.out.println("Service key is" + serviceKey);
				System.out.println("Service name is" + sinfo.getDefaultName());
				ServiceDetail sd = proxy.get_serviceDetail(serviceKey);
				Vector businessServices = sd.getBusinessServiceVector();
				if (businessServices.size() == 0)
					continue;
				BindingTemplates templates = ((BusinessService) businessServices
						.elementAt(0)).getBindingTemplates();
				Vector bindingTemplates = templates.getBindingTemplateVector();
				if (bindingTemplates.size() == 0)
					continue;
				BindingTemplate template = (BindingTemplate) bindingTemplates
						.elementAt(0);
				TModelInstanceDetails details = template
						.getTModelInstanceDetails();
				Vector detVector = details.getTModelInstanceInfoVector();
				TModelInstanceInfo tinfo = (TModelInstanceInfo) detVector
						.elementAt(0);
				InstanceDetails idetails = tinfo.getInstanceDetails();
				OverviewDoc odoc = idetails.getOverviewDoc();
				System.out.print(odoc.getOverviewURLString());
				allPedroXMLURLS.add(odoc.getOverviewURLString());
			}// for

			// Handle possible errors
		} catch (UDDIException e) {
			DispositionReport dr = e.getDispositionReport();
			if (dr != null) {
				System.out.println("UDDIException faultCode:"
						+ e.getFaultCode() + "\n operator:" + dr.getOperator()
						+ "\n generic:" + dr.getGeneric() + "\n errno:"
						+ dr.getErrno() + "\n errCode:" + dr.getErrCode()
						+ "\n errInfoText:" + dr.getErrInfoText());
			}
			e.printStackTrace();
		} catch (Exception e) {
			throw new FetaLoadException("Exception occured during Feta Load"
					+ e.getMessage());
		}
		return allPedroXMLURLS;
	}

}
