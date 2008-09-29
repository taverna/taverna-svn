/*
 * Created on Jan 12, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * Action for removing an object from the Scufl Model. The object can be for
 * instance a Processor, Port, DataConstraint, or ConcurrencyConstraint.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1.2.1 $
 */
public class RemoveAction extends ModelObjectAction {

	public RemoveAction(ScuflModel model, Object modelObject) {
		super(model, modelObject);		
		putValue(SMALL_ICON, TavernaIcons.deleteIcon);
		putValue(NAME, "Remove from model");
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
		// 0));
	}

	public void actionPerformed(ActionEvent e) {
		remove(modelObject);
	}

	private void remove(Object object) {
		Class clazz = object.getClass();
		if (clazz.isArray()) {
			Object[] array = (Object[]) object;
			for (int index = 0; index < array.length; index++) {
				remove(array[index]);
			}
		} else {
			if (object instanceof Collection) {
				Iterator iterator = ((Collection) object).iterator();
				while (iterator.hasNext()) {
					remove(iterator.next());
				}
			} else if (object instanceof Processor) {
				model.destroyProcessor((Processor) object);
			} else if (object instanceof Port) {
				Port port = (Port) object;
				port.getProcessor().removePort(port);
			} else if (object instanceof DataConstraint) {
				model.destroyDataConstraint((DataConstraint) object);
			} else if (object instanceof ConcurrencyConstraint) {
				model
						.destroyConcurrencyConstraint((ConcurrencyConstraint) object);
			}
		}
	}
}