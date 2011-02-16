package uk.ac.manchester.cs.img.esc.ui.serviceprovider;

import java.net.URL;

import net.sf.taverna.t2.lang.beans.*;

public class EscServiceProviderConfig extends PropertyAnnotated {
	
	private String serviceProviderURLString = "http://www.esciencecentral.co.uk/APIServer";

	@PropertyAnnotation(preferred=true, displayName="URL")
	public String getServiceProviderURLString() {
		return serviceProviderURLString;
	}

	public void setServiceProviderURLString(String serviceProviderURL) {
		this.serviceProviderURLString = serviceProviderURL;
	}

	}
