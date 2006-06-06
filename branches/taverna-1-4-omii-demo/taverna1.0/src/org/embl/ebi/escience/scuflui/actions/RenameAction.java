package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflIcons;

public class RenameAction extends ModelObjectAction {

	public RenameAction(ScuflModel model, Object modelObject) {
		super(model, modelObject);
		putValue(SMALL_ICON, ScuflIcons.renameIcon);
		putValue(NAME, "Rename");
	}

	public void actionPerformed(ActionEvent e) {
		rename(modelObject);
	}

	private void rename(Object object) {
		String name;
		String description;
		// FIXME: Have some interface Nameable?
		if (object instanceof Processor) {
			name = ((Processor) object).getName();
			description = "New name for the processor?";
		} else if (object instanceof Port) {
			Port port = (Port) object;
			if (!port.isNameEditable()) {
				JOptionPane.showMessageDialog(null,
						"Only input and output ports can be renamed.",
						"Can't rename port", JOptionPane.ERROR_MESSAGE);
				return;
			}
			name = port.getName();
			description = "New name for the port?";
		} else {
			throw new IllegalArgumentException("Unknown modelObject type");
		}

		String new_name = (String) JOptionPane.showInputDialog(null,
				description, "Name required", JOptionPane.QUESTION_MESSAGE,
				null, null, name);
		if (new_name == null || new_name.equals(name)) {
			return;
		}		
		if (object instanceof Processor) {
			Processor processor = (Processor)object;
			processor.setName(new_name);
			name = processor.getName();
		} else if (object instanceof Port) {
			Port port = (Port) object;
			port.setName(new_name);
			name = port.getName();
		} else {
			throw new IllegalArgumentException("Unknown modelObject type");
		}
		if (! name.equals(new_name)) {
			JOptionPane.showMessageDialog(null,
					"Invalid name '" + new_name +"', name reset to '" + name + "'.",
					"Invalid name", JOptionPane.ERROR_MESSAGE);
		}
	}

}
