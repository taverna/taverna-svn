/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;
import java.awt.event.ActionListener;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;




/**
 * Classes implementing this interface perform the role
 * of creating an actionlistener that creates the appropriate
 * scavenger structures when activated.
 * @author Tom Oinn
 */
public interface ScavengerHelper {

    /** 
     * Return an action listener to create the scavengers when
     * the item is clicked
     */
    public ActionListener getListener(ScavengerTree theScavenger);

    /**
     * Get the text for this scavenger creator, so something like
     * 'Create new WSDL scavenger' or similar
     */
    public String getScavengerDescription();

}
