/*
 * LOCALFragment.java
 *
 * Created on January 14, 2005, 12:18 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * 
 * @author alperp
 */
public class LOCALFragment extends DnDFragment {

	public static final String HEADER_LOCAL_TAG = "local";

	private String localJavaObjectName;

	public LOCALFragment(String objectName) {

		super(HEADER_LOCAL_TAG);
		fragment = null;
		localJavaObjectName = objectName;

		try {

			Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);

			fragment = new Element(HEADER_LOCAL_TAG, ns);
			fragment.setText(localJavaObjectName);

		} catch (Exception exp) {
			System.out
					.println("Exception Occured during Taverna DnD element construction"
							+ exp.toString());
		}
	}

	public LOCALFragment(Element specElement) {
		super(HEADER_LOCAL_TAG);
		fragment = specElement;
		localJavaObjectName = specElement.getChildText(HEADER_LOCAL_TAG);
	}

	public String getLocalJavaObjectName() {

		return this.localJavaObjectName;
	}
}
