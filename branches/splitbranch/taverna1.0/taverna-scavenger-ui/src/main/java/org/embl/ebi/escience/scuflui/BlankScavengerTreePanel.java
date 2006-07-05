/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;


/**
 * A trivial subclass of the scavenger panel which starts off blank
 */
public class BlankScavengerTreePanel extends ScavengerTreePanel {
		
	private static final long serialVersionUID = 8333709449450772744L;

	public String getName() {
		return "Available services (no defaults)";
	}
	
	protected boolean populate()
	{
		return false;
	}
	
	

}
