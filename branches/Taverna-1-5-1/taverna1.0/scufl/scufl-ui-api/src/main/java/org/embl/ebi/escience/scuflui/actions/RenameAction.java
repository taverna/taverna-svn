package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;

@SuppressWarnings("serial")
public class RenameAction extends ModelObjectAction {

	public RenameAction(ScuflModel model, Object modelObject) {
		super(model, modelObject);
		putValue(SMALL_ICON, TavernaIcons.renameIcon);
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
		} else if (object instanceof ScuflModel) {
			name = ((ScuflModel) object).getDescription().getTitle();
			description = "New workflow title";
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
		} else if (object instanceof ScuflModel) {
			ScuflModel model = (ScuflModel) object;
			new_name = new_name.trim();
			model.getDescription().setTitle(new_name);
			name = model.getDescription().getTitle();
			model.fireModelEvent(new org.embl.ebi.escience.scufl.ScuflModelEvent(this,
					"Title Changed"));					
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
