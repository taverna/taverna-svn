/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.embl.ebi.escience.scuflui.actions;

import javax.swing.AbstractAction;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public abstract class ScuflModelAction extends AbstractAction
{
	protected ScuflModel model;
	
	protected ScuflModelAction(ScuflModel model)
	{
		this.model = model;
	}
}
