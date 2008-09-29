/*
 * WSDLFragment.java
 *
 * Created on January 14, 2005, 12:13 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd;

/**
 * 
 * @author alperp
 */

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

public class WSDLFragment extends DnDFragment {

	public static final String HEADER_WSDL_TAG = "arbitrarywsdl";

	public static final String WSDL_TAG = "wsdl";

	public static final String OPERATION_TAG = "operation";

	private String wsdlLoc;

	private String operName;

	public WSDLFragment(String WSDLLocationValue, String WSDLOperationNameValue) {
		super(HEADER_WSDL_TAG);
		fragment = null;
		wsdlLoc = WSDLLocationValue;
		operName = WSDLOperationNameValue;

		try {

			Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
			fragment = new Element(HEADER_WSDL_TAG, ns);

			Element wsdlElement = new Element(WSDL_TAG, ns);
			wsdlElement.setText(wsdlLoc);

			Element operationElement = new Element(OPERATION_TAG, ns);
			operationElement.setText(operName);

			fragment.addContent((Content) wsdlElement);
			fragment.addContent((Content) operationElement);

		} catch (Exception exp) {
			System.out
					.println("Exception Occured during Taverna DnD element construction"
							+ exp.toString());
		}

	}

	public WSDLFragment(org.jdom.Element specElement) {
		super(HEADER_WSDL_TAG);
		fragment = specElement;
		Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
		wsdlLoc = specElement.getChildText(WSDL_TAG, ns);
		operName = specElement.getChildText(OPERATION_TAG, ns);
	}

	public String getWsdlLoc() {
		return wsdlLoc;
	}

	public String getOperName() {
		return operName;
	}
}
