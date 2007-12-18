/*
 * SOAPLABFragment.java
 *
 * Created on January 14, 2005, 12:16 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd;

/**
 * 
 * @author alperp
 */

import org.jdom.Element;
import org.jdom.Namespace;

public class SOAPLABFragment extends DnDFragment {

	public static final String HEADER_SOAPLAB_TAG = "soaplabwsdl";

	private String soaplabServiceLocation;

	public SOAPLABFragment(String serviceLoc) {
		super(HEADER_SOAPLAB_TAG);

		fragment = null;
		soaplabServiceLocation = serviceLoc;

		try {

			Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);

			fragment = new Element(HEADER_SOAPLAB_TAG, ns);
			fragment.setText(soaplabServiceLocation);

		} catch (Exception exp) {
			System.out
					.println("Exception Occured during Taverna DnD element construction"
							+ exp.toString());
		}

	}

	public SOAPLABFragment(Element specElement) {
		super(HEADER_SOAPLAB_TAG);
		fragment = specElement;
		Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
		soaplabServiceLocation = specElement.getText();
		System.out.println("Service Location is-->" + soaplabServiceLocation);
	}

	public String getSoaplabServiceLoc() {
		return soaplabServiceLocation;
	}
}
