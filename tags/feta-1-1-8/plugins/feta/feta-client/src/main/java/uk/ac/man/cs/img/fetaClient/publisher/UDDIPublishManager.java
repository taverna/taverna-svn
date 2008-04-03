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

package uk.ac.man.cs.img.fetaClient.publisher;

import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.OverviewDoc;
import org.uddi4j.datatype.binding.BindingTemplate;
import org.uddi4j.datatype.binding.BindingTemplates;
import org.uddi4j.datatype.binding.InstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceInfo;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.RegisteredInfo;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.TModelDetail;
import org.uddi4j.response.TModelInfo;
import org.uddi4j.response.TModelList;
import org.uddi4j.transport.TransportFactory;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 * 
 * @author alperp
 */
public class UDDIPublishManager {

	private String inquiryURL;

	private String publishURL;

	private String userName;

	private String passWord;

	/** Creates a new instance of UDDIPublishManager */
	public UDDIPublishManager(String inqURL, String pubURL, String usrName,
			String passWd) {

		inquiryURL = inqURL;
		publishURL = pubURL;
		userName = usrName;
		passWord = passWd;
		setSysProps();
	}

	public String registerBusiness(String businessName) {
		// Construct a UDDIProxy object
		UDDIProxy proxy = new UDDIProxy();

		try {
			// Select the desired UDDI server node
			proxy.setPublishURL(publishURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			Vector entities = new Vector();
			BusinessEntity be = new BusinessEntity("", businessName);
			entities.addElement(be);

			// Save business
			BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),
					entities);

			// Process returned BusinessDetail object to get the
			// busines key.
			Vector businessEntities = bd.getBusinessEntityVector();
			BusinessEntity returnedBusinessEntity = (BusinessEntity) (businessEntities
					.elementAt(0));
			String businessKey = returnedBusinessEntity.getBusinessKey();

			System.out.println("Business registered with key" + businessKey);
			return businessKey;
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
			return null;
			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}

	}

	public String registerTModel(String tModelName) {
		UDDIProxy proxy = new UDDIProxy();

		try {

			proxy.setPublishURL(publishURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			Vector tModels = new Vector();
			TModel tModel = new TModel("", tModelName);
			tModels.add(tModel);

			// **** Save a TModel
			TModelDetail tModelDetail = proxy.save_tModel(token
					.getAuthInfoString(), tModels);

			// Processing return type
			Vector tModelVector = tModelDetail.getTModelVector();
			TModel tModelReturned = (TModel) (tModelVector.elementAt(0));

			System.out.println("TModel registered with key"
					+ tModelReturned.getTModelKey());
			return tModelReturned.getTModelKey();

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
			return null;
			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String registerFetaService(String fetaTModelName,
			String descriptionLocation) {

		UDDIProxy proxy = new UDDIProxy();

		try {
			String businessKey = null;
			// Select the desired UDDI server node
			proxy.setPublishURL(publishURL);
			proxy.setInquiryURL(inquiryURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			System.out.println("Returned authToken:"
					+ token.getAuthInfoString());

			// similarly for the Feta tModel key
			String fetaTModelKey = findTModelByName(fetaTModelName);
			if (fetaTModelKey == null)
				fetaTModelKey = this.registerTModel(fetaTModelName);

			RegisteredInfo regInfo = proxy.get_registeredInfo(token
					.getAuthInfoString());

			Vector businessInfoVector = regInfo.getBusinessInfos()
					.getBusinessInfoVector();
			int i = businessInfoVector.size();
			if (i > 0) {
				// get the first one for now
				BusinessInfo bInfo = (BusinessInfo) businessInfoVector
						.elementAt(0);
				businessKey = bInfo.getBusinessKey();

			} else {
				businessKey = this.registerBusiness("MYGRID-FETA");
			}

			String serviceKey = registerFetaService(businessKey,
					fetaTModelName, descriptionLocation);

			if (serviceKey == null)
				return "Error has occured during UDDI publishing";
			else
				return "Service has been registered with key: " + serviceKey;

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
			return "Error has occured during UDDI publishing. "
					+ dr.getErrInfoText();

			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();
			return "Error has occured during UDDI publishing. "
					+ e.getMessage();
		}

	}

	public String registerFetaService(String businessKey,
			String fetaTModelName, String descriptionLocation) {
		// Construct a UDDIProxy object
		UDDIProxy proxy = new UDDIProxy();

		String serviceFileName = new String();
		String serviceName = new String();
		String[] parts = descriptionLocation.split("/");

		if (parts != null) {
			if (parts.length > 0) {
				serviceFileName = parts[parts.length - 1];
				serviceName = serviceFileName.substring(0, serviceFileName
						.length() - 4);
			}
		}

		try {
			// Select the desired UDDI server node
			proxy.setPublishURL(publishURL);
			proxy.setInquiryURL(inquiryURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			System.out.println("Returned authToken:"
					+ token.getAuthInfoString());

			// For saving a Business Service we need a Business Entity .
			// Hence create a new business entity . if the key for oone is not
			// provided

			if (businessKey == null) {
				return null;
				// businessKey = this.registerBusiness("MYGRID-FETA");
			}

			// similarly for the Feta tModel key
			String fetaTModelKey = findTModelByName(fetaTModelName);
			if (fetaTModelKey == null)
				fetaTModelKey = this.registerTModel(fetaTModelName);

			Vector fetaBindingTemplates = new Vector();

			// Create a new business service

			BindingTemplate fetaBindTemp = new BindingTemplate();

			TModelInstanceDetails details = new TModelInstanceDetails();
			fetaBindTemp.setTModelInstanceDetails(details);

			OverviewDoc odoc = new OverviewDoc();
			odoc.setOverviewURL(descriptionLocation);

			InstanceDetails idetails = new InstanceDetails();
			idetails.setOverviewDoc(odoc);

			TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo(
					fetaTModelKey);
			tModelInstanceInfo.setInstanceDetails(idetails);

			Vector tModelInstanceInfoVector = new Vector();
			tModelInstanceInfoVector.addElement(tModelInstanceInfo);

			details.setTModelInstanceInfoVector(tModelInstanceInfoVector);
			fetaBindTemp.setTModelInstanceDetails(details);

			fetaBindingTemplates.addElement(fetaBindTemp);

			BindingTemplates bindingTemplates = new BindingTemplates();
			bindingTemplates.setBindingTemplateVector(fetaBindingTemplates);

			BusinessService businessService = new BusinessService("", "",
					bindingTemplates);
			businessService.setDefaultNameString("Feta Service " + serviceName,
					"en");
			// , "en-US");

			businessService.setBusinessKey(businessKey);

			Vector services = new Vector();
			services.addElement(businessService);

			// **** Save the Business Servic
			ServiceDetail serviceDetail = proxy.save_service(token
					.getAuthInfoString(), services);

			// Process returned ServiceDetail object to get the key

			Vector businessServices = serviceDetail.getBusinessServiceVector();
			BusinessService businessServiceReturned = (BusinessService) (businessServices
					.elementAt(0));
			System.out.println("Service registered with key"
					+ businessServiceReturned.getServiceKey());
			return businessServiceReturned.getServiceKey();

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

			return null;

			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}

	}

	public String findTModelByName(String tModelName) {
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
			TModelList fetaList = proxy.find_tModel("FetaModel", null, null,
					findQualifiers, 1);

			Vector feta_tModelInfoVector = fetaList.getTModelInfos()
					.getTModelInfoVector();
			int i = feta_tModelInfoVector.size();

			if (i > 0) {

				TModelInfo feta_tModelInfo = (TModelInfo) feta_tModelInfoVector
						.elementAt(0);
				// Print name of feta tModel

				return feta_tModelInfo.getTModelKey();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void deleteService(String serviceKey) {

		// Construct a UDDIProxy object
		UDDIProxy proxy = new UDDIProxy();

		try {
			// Select the desired UDDI server node
			proxy.setPublishURL(publishURL);
			proxy.setInquiryURL(inquiryURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			// **** Having service key, delete using the authToken
			DispositionReport dr = proxy.delete_service(token
					.getAuthInfoString(), serviceKey);

			if (dr.success()) {
				System.out.println("Service successfully deleted");
			} else {
				System.out
						.println("Errno:" + dr.getErrno() + "\n ErrCode:"
								+ dr.getErrCode() + "\n ErrText:"
								+ dr.getErrInfoText());
			}

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

			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void deleteBusiness(String businessKey) {
		// Construct a UDDIProxy object
		UDDIProxy proxy = new UDDIProxy();

		try {
			// Select the desired UDDI server node
			proxy.setPublishURL(publishURL);
			proxy.setInquiryURL(inquiryURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			// delete using the authToken and businessKey
			DispositionReport dr1 = proxy.delete_business(token
					.getAuthInfoString(), businessKey);

			if (dr1.success()) {
				System.out.println("Registry successfully cleaned");
			} else {
				System.out.println("Errno:" + dr1.getErrno() + "\n ErrCode:"
						+ dr1.getErrCode() + "\n ErrText:"
						+ dr1.getErrInfoText());
			}
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

			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void deleteTModel(String tModelKey) {
		// Construct a UDDIProxy object
		UDDIProxy proxy = new UDDIProxy();

		try {
			// Select the desired UDDI server node
			proxy.setPublishURL(publishURL);
			proxy.setInquiryURL(inquiryURL);

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(userName, passWord);

			// delete using the authToken and tModelKey
			DispositionReport dr1 = proxy.delete_tModel(token
					.getAuthInfoString(), tModelKey);

			if (dr1.success()) {
				System.out.println("TModel successfully deleted"
						+ dr1.getResultVector().toString());
			} else {
				System.out.println("Errno:" + dr1.getErrno() + "\n ErrCode:"
						+ dr1.getErrCode() + "\n ErrText:"
						+ dr1.getErrInfoText());
			}
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

			// Catch any other exception that may occur
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setSysProps() {

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

	}

	public static void main(String args[]) {
		try {
			if (args.length < 2) {
				System.out.println("Insufficient arguments");
				System.exit(0);
			}

			UDDIPublishManager man = new UDDIPublishManager(
					"http://oescedge.oucs.ox.ac.uk/juddi/inquiry",
					"http://oescedge.oucs.ox.ac.uk/juddi/publish", "pinar", "");

			// UDDIPublishManager man = new
			// UDDIPublishManager("http://fantasio.ecs.soton.ac.uk:8080/grimoires/services/inquire",
			// "http://fantasio.ecs.soton.ac.uk:8080/grimoires/services/publish",
			// "",
			// "");
			if (args[0].equalsIgnoreCase("registerBusiness")) {

				String businessKey = man.registerBusiness(args[1]);
			} else if (args[0].equalsIgnoreCase("registerService")) {
				String serviceKey = man.registerFetaService(args[1],
						"FetaModel",
						"http://www.cs.man.ac.uk/~penpecip/feta/data");
			} else if (args[0].equalsIgnoreCase("registerTModel")) {
				man.registerTModel(args[1]);
			} else if (args[0].equalsIgnoreCase("deleteService")) {
				man.deleteService(args[1]);
			} else if (args[0].equalsIgnoreCase("deleteBusiness")) {
				man.deleteBusiness(args[1]);
			} else if (args[0].equalsIgnoreCase("deleteTModel")) {
				man.deleteTModel(args[1]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}
}
