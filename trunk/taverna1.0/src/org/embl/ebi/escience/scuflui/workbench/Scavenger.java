/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.tree.*;

/**
 * A subclass of DefaultMutableTreeNode that should
 * be subclassed to create particular scavengers for
 * different types of processor
 * @author Tom Oinn
 */
public abstract class Scavenger extends DefaultMutableTreeNode {

    public Scavenger(Object userObject) {
	super(userObject);
    }

}
