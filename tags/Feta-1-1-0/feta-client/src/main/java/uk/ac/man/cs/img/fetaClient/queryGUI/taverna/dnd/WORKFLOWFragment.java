/*
 * WORKFLOWFragment.java
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

public class WORKFLOWFragment extends DnDFragment {

	public static final String HEADER_WORKFLOW_TAG = "workflow";

	public static final String WORKFLOW_LOCATION_TAG = "xscufllocation";

	private String workflowLoc;

	public WORKFLOWFragment(String scuflScriptLocation) {
		super(HEADER_WORKFLOW_TAG);
		fragment = null;
		workflowLoc = scuflScriptLocation;

		try {

			Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);

			fragment = new Element(HEADER_WORKFLOW_TAG, ns);

			Element scriptLocElement = new Element(WORKFLOW_LOCATION_TAG, ns);
			scriptLocElement.setText(workflowLoc);

			fragment.addContent((Content) scriptLocElement);

		} catch (Exception exp) {
			System.out
					.println("Exception Occured during Taverna DnD element construction"
							+ exp.toString());
		}

	}

	public WORKFLOWFragment(Element specElement) {
		super(HEADER_WORKFLOW_TAG);
		fragment = specElement;
		Namespace ns = Namespace.getNamespace("s", SCHEMA_LOCATION);
		workflowLoc = specElement.getChildText(WORKFLOW_LOCATION_TAG, ns);

	}

	public String getWorkflowLoc() {
		return this.workflowLoc;
	}
}
