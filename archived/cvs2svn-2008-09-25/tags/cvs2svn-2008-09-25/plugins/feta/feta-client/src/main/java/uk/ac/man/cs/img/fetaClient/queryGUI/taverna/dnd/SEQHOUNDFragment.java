/*
 * SEQHOUNDFragment.java
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

public class SEQHOUNDFragment extends DnDFragment {

	public static final String HEADER_SEQHOUND_TAG = "seqhound";

	public static final String SEQHOUND_METHOD_TAG = "method";

	public static final String SEQHOUND_SERVER_TAG = "server";

	public static final String SEQHOUND_SEQREM_SERVER_TAG = "jseqremserver";

	public static final String SEQHOUND_PATH_TAG = "path";

	public static final String SEQHOUND_SEQREM_PATH_TAG = "jseqrempath";

	private String seqhoundMethod;

	private String seqhoundServer;

	private String seqhoundJSeqremServer;

	private String seqhoundPath;

	private String seqhoundJSeqremPath;

	public SEQHOUNDFragment(String method, String server, String seqremServer,
			String path, String seqremPath) {
		super(HEADER_SEQHOUND_TAG);
		fragment = null;
		seqhoundMethod = method;
		seqhoundServer = server;
		seqhoundJSeqremServer = seqremServer;
		seqhoundPath = path;
		seqhoundJSeqremPath = seqremPath;

		try {

			Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
			fragment = new Element(HEADER_SEQHOUND_TAG, ns);

			Element seqhoundMethodElement = new Element(SEQHOUND_METHOD_TAG, ns);
			seqhoundMethodElement.setText(seqhoundMethod);

			Element seqhoundServerElement = new Element(SEQHOUND_SERVER_TAG, ns);
			seqhoundServerElement.setText(seqhoundServer);

			Element seqhoundJSeqremServerElement = new Element(
					SEQHOUND_SEQREM_SERVER_TAG, ns);
			seqhoundJSeqremServerElement.setText(seqhoundJSeqremServer);

			Element seqhoundPathElement = new Element(SEQHOUND_PATH_TAG, ns);
			seqhoundPathElement.setText(seqhoundPath);

			Element seqhoundJSeqremPathElement = new Element(
					SEQHOUND_SEQREM_PATH_TAG, ns);
			seqhoundJSeqremPathElement.setText(seqhoundJSeqremPath);

			fragment.addContent((Content) seqhoundMethodElement);
			fragment.addContent((Content) seqhoundServerElement);
			fragment.addContent((Content) seqhoundJSeqremServerElement);
			fragment.addContent((Content) seqhoundPathElement);
			fragment.addContent((Content) seqhoundJSeqremPathElement);

			System.out.println(getFragmentAsString());

		} catch (Exception exp) {
			System.out
					.println("Exception Occured during Taverna DnD element construction"
							+ exp.toString());
		}

	}

	public SEQHOUNDFragment(Element specElement) {
		super(HEADER_SEQHOUND_TAG);
		fragment = specElement;
		seqhoundMethod = specElement.getChildText(SEQHOUND_METHOD_TAG);
		seqhoundServer = specElement.getChildText(SEQHOUND_SERVER_TAG);
		seqhoundJSeqremServer = specElement
				.getChildText(SEQHOUND_SEQREM_SERVER_TAG);
		seqhoundPath = specElement.getChildText(SEQHOUND_PATH_TAG);
		seqhoundJSeqremPath = specElement
				.getChildText(SEQHOUND_SEQREM_PATH_TAG);

	}

	public String getMethodName() {

		return this.seqhoundMethod;
	}

}
