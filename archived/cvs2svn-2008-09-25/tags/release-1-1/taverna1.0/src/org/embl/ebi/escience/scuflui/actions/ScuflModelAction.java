/*
 * Created on Jan 12, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import javax.swing.AbstractAction;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.2 $
 */
public abstract class ScuflModelAction extends AbstractAction
{
	protected ScuflModel model;
	
	protected ScuflModelAction(ScuflModel model)
	{
		this.model = model;
	}
}
