/*
 * Created on Jan 26, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.3 $
 */
public class AddDataConstraintAction extends ScuflModelAction {
	private Port source;

	private Port target;

	/**
	 * @param model
	 */
	public AddDataConstraintAction(ScuflModel model, Port start, Port end) {
		super(model);
		if (end instanceof InputPort) {
			this.source = start;
			this.target = end;
			putValue(SMALL_ICON, TavernaIcons.inputIcon);
		} else {
			this.source = end;
			this.target = start;
			putValue(SMALL_ICON, TavernaIcons.outputIcon);
		}
		putValue(NAME, end.getName());
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			model.addDataConstraint(new DataConstraint(model, source, target));
		} catch (DataConstraintCreationException e1) {
			// TODO Handle DataConstraintCreationException
			e1.printStackTrace();
		}
	}
}
