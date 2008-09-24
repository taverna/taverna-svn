/*
 * BIOMOBYFragment.java
 *
 * Created on January 14, 2005, 12:17 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd;

/**
 * 
 * @author alperp
 */

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

public class BIOMOBYFragment extends DnDFragment {

	public static final String HEADER_BIOMOBY_TAG = "biomobywsdl";

	public static final String BIOMOBY_ENDPOINT_TAG = "mobyEndpoint";

	public static final String BIOMOBY_SERVICE_NAME_TAG = "serviceName";

	public static final String BIOMOBY_AUTHORITY_TAG = "authorityName";

	private String mobyCentralLoc;

	private String mobyServiceName;

	private String providerAuthorityName;

	public BIOMOBYFragment(String mobyCentralLocation, String serviceName,
			String authorityName) {
		super(HEADER_BIOMOBY_TAG);

		fragment = null;
		mobyCentralLoc = mobyCentralLocation;
		mobyServiceName = serviceName;
		providerAuthorityName = authorityName;

		try {

			Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
			fragment = new Element(HEADER_BIOMOBY_TAG, ns);

			Element mobyCentralElement = new Element(BIOMOBY_ENDPOINT_TAG, ns);
			mobyCentralElement.setText(mobyCentralLoc);

			Element serviceNameElement = new Element(BIOMOBY_SERVICE_NAME_TAG,
					ns);
			serviceNameElement.setText(mobyServiceName);

			Element authorityNameElement = new Element(BIOMOBY_AUTHORITY_TAG,
					ns);
			authorityNameElement.setText(providerAuthorityName);

			fragment.addContent((Content) mobyCentralElement);
			fragment.addContent((Content) serviceNameElement);
			fragment.addContent((Content) authorityNameElement);

		} catch (Exception exp) {
			System.out
					.println("Exception Occured during Taverna DnD element construction"
							+ exp.toString());
		}

	}

	public BIOMOBYFragment(Element specElement) {
		super(HEADER_BIOMOBY_TAG);
		fragment = specElement;
		Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
		mobyCentralLoc = specElement.getChildText(BIOMOBY_ENDPOINT_TAG, ns);
		mobyServiceName = specElement
				.getChildText(BIOMOBY_SERVICE_NAME_TAG, ns);
		providerAuthorityName = specElement.getChildText(BIOMOBY_AUTHORITY_TAG,
				ns);

	}

	public String getMobyCENTRALLoc() {

		return this.mobyCentralLoc;
	}

	public String getAuthorityName() {
		return this.providerAuthorityName;
	}

	public String getServiceName() {
		return this.mobyServiceName;
	}
}
