package org.embl.ebi.escience.scuflworkers.web;


import java.util.Set;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;

/**
 * An interface to specifically identify the WebScavengerHelper.
 * This is need to distinguish it from the other scavenger helpers so that is placement
 * in the menu can be controlled, and because WebScavenger requires ScavengerTree in its constructor
 * meaning that it needs this information passing to getDefaults.
 * 
 * @author Stuart Owen
 *
 */

public interface WebScavengerHelper {
	
	/**
	 * The same as getDefaults in ScavengerHelper, except that ScavengerTree is required
	 * to construct the WebScavengers.
	 * 
	 * @param tree
	 * @return
	 */
	public Set<Scavenger> getDefaults(ScavengerTree tree);

}
