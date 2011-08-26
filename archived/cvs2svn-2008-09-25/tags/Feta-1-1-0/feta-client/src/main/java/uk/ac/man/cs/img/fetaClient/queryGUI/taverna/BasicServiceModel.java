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

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.io.StringReader;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.BIOMOBYFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.DnDFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.LOCALFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.SEQHOUNDFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.SOAPLABFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.WORKFLOWFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.WSDLFragment;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

/**
 * @author alperp
 *
 */

public class BasicServiceModel extends AbstractMonitorableModel {

	private String serviceDescriptionLocation;

	private String serviceName;

	private String operationName;

	private String operationSpec;

	private String serviceDescriptionText;

	private String serviceLocation;

	private String serviceOrganisationName;

	private String serviceInterfaceLocation;

	private ServiceType serviceType;

	private DnDFragment dndFragment;

	private BasicOperationModel operation;

	public BasicServiceModel() {
		super();
		serviceType = ServiceType.UNDEFINED;
		operation = new BasicOperationModel();
	}

	public BasicServiceModel(IServiceModelFiller xmlWrapper) {

		super();

		org.jdom.Document doc;
		org.jdom.Element elm;

		serviceName = xmlWrapper.getServiceName();
		serviceDescriptionText = xmlWrapper.getServiceDescriptionText();
		serviceLocation = xmlWrapper.getLocationURL();
		serviceInterfaceLocation = xmlWrapper.getServiceInterfaceLocation();
		serviceDescriptionLocation = xmlWrapper.getDescriptionLocation();
		serviceType = xmlWrapper.getServiceType();
		serviceOrganisationName = xmlWrapper.getOrganisationName();
		operation = new BasicOperationModel(xmlWrapper);
		operationName = xmlWrapper.getOperationName();
		operationSpec = xmlWrapper.getOperationSpec();

		try {
			if (operationSpec == null) {
				if (this.serviceType == ServiceType.WSDL) {
					dndFragment = new WSDLFragment(serviceInterfaceLocation,
							operationName);
				} else if (this.serviceType == ServiceType.SOAPLAB) {
					dndFragment = new SOAPLABFragment(serviceLocation);
				} else if (this.serviceType == ServiceType.WORKFLOW) {
					dndFragment = new WORKFLOWFragment(serviceInterfaceLocation);
				} else if (this.serviceType == ServiceType.BIOMOBY) {
					dndFragment = new BIOMOBYFragment(serviceInterfaceLocation,
							operationName, serviceOrganisationName);
				} else if (this.serviceType == ServiceType.SEQHOUND) {
					dndFragment = new SEQHOUNDFragment(operationName,
							"seqhound.blueprint.org",
							"skinner.blueprint.org:8080", "/cgi-bin/seqrem",
							"/jseqhound/jseqrem");
				} else if (this.serviceType == ServiceType.LOCALOBJECT) {
					dndFragment = new LOCALFragment(operationName);
				} else {

					// there is no way we can introduce this resulting operation
					// to Taverna
					System.out.println("==============================================================");
					System.out.println("Problem creating a service model FOR service " + serviceName + " "+operationName);
					System.out.println("details - desc loc " + serviceDescriptionLocation);
					System.out.println("details - service Type " + serviceType);
					System.out.println("==============================================================");
				}
			} else {
				// it already has a Taverna compliant processor spec element
				// within its descriptions
				// so use it!

				try {
					doc = (new SAXBuilder()).build(new StringReader(
							operationSpec));

					elm = doc.getRootElement();

					// We do this because DnD would fail if the jdom Element we
					// submit to the
					// transferable has a PARENT
					org.jdom.Element specElement = (Element) elm.clone();

					dndFragment = new DnDFragment();
					dndFragment.setFragment(specElement);
					dndFragment.setHeaderTag(specElement.getName());

				} catch (Exception ex) {
					// the XML we read from the operationSpec field in the FEta
					// XML is invalid. Not nice!!
				}

			}// else

		}// try
		catch (Exception e) {
			e.printStackTrace();
		}// catch

	}

	/**
	 * @return
	 */
	public String getServiceLocation() {
		return this.serviceLocation;
	}

	/**
	 * @return
	 */
	public String getServiceName() {
		return this.serviceName;
	}

	/**
	 * @return
	 */
	public String getServiceOrganisationName() {
		return this.serviceOrganisationName;
	}

	/**
	 * @return
	 */
	public ServiceType getServiceType() {
		return this.serviceType;
	}

	/**
	 * @return
	 */
	public String getServiceDescriptionText() {
		return this.serviceDescriptionText;
	}

	public String getServiceDescriptionLocation() {
		return this.serviceDescriptionLocation;

	}

	public String getServiceInterfaceLocation() {
		return this.serviceInterfaceLocation;

	}

	public DnDFragment getDnDFragment() {
		return this.dndFragment;

	}

	public org.jdom.Element getTavernaProcessorSpecAsElement() {
		return dndFragment.getFragment();

	}

	public String getTavernaProcessorTag() {
		return dndFragment.getHeaderTag();

	}

	public String getTavernaProcessorSpecAsString() {
		return this.operationSpec;

	}

	public BasicOperationModel getOperationModel() {
		return this.operation;
	}

	public String toString() {
		if (this.serviceType == ServiceType.SOAPLAB)
			return this.serviceName;
		else
			return /* this.serviceName+" - "+ */this.operation
					.getOperationName();

	}

	public void setServiceDescriptionLocation(String loc) {
		this.serviceDescriptionLocation = loc;
		fireChange();

	}

	public void setOperationModel(BasicOperationModel operModel) {
		operation.copyFrom(operModel);
		fireChange();

	}

	public void setServiceInterfaceLocation(String loc) {
		this.serviceInterfaceLocation = loc;
		fireChange();
	}

	/**
	 * @param string
	 */
	public void setServiceLocation(String loc) {
		this.serviceLocation = loc;
		fireChange();

	}

	/**
	 * @param string
	 */
	public void setServiceName(String name) {
		this.serviceName = name;
		fireChange();

	}

	/**
	 * @param string
	 */
	public void setServiceOrganisationName(String name) {
		this.serviceOrganisationName = name;
		fireChange();

	}

	/**
	 * @param i
	 */

	public void setServiceType(ServiceType srvcType) {
		this.serviceType = srvcType;
		fireChange();

	}

	/**
	 * @param string
	 */
	public void setServiceDescriptionText(String desc) {
		this.serviceDescriptionText = desc;
		fireChange();

	}

	public void setDnDFragment(DnDFragment frag) {
		this.dndFragment = frag;
	}

	public void copyFrom(BasicServiceModel model) {
		setServiceName(model.getServiceName());
		setServiceDescriptionText(model.getServiceDescriptionText());
		setServiceOrganisationName(model.getServiceOrganisationName());
		setServiceLocation(model.getServiceLocation());
		setServiceDescriptionLocation(model.getServiceDescriptionLocation());
		setServiceInterfaceLocation(model.getServiceInterfaceLocation());
		setServiceType(model.getServiceType());
		setDnDFragment(model.getDnDFragment());
		setOperationModel(model.getOperationModel());

	}

}
