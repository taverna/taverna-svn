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

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class DnDFragment {

	public static final String SCHEMA_LOCATION = "http://org.embl.ebi.escience/xscufl/0.1alpha";

	protected Element fragment;

	private String HEADER_TAG;

	public DnDFragment() {

	}

	public DnDFragment(String headerTag) {
		HEADER_TAG = headerTag;
	}

	public Element getFragment() {

		return fragment;
	}

	public String getFragmentAsString() {
		XMLOutputter xo = new XMLOutputter();
		return xo.outputString(fragment);

	}

	public String getHeaderTag() {
		return HEADER_TAG;
	}

	public void setFragment(Element frag) {

		fragment = frag;
	}

	public void setHeaderTag(String tag) {

		HEADER_TAG = tag;
	}

}
