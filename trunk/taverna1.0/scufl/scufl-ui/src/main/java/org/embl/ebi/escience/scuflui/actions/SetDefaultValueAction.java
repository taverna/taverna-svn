package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

public class SetDefaultValueAction extends ModelObjectAction {

	public SetDefaultValueAction(ScuflModel model, InputPort modelObject) {
		super(model, modelObject);
		putValue(SMALL_ICON, TavernaIcons.editIcon);
		putValue(NAME, "Set default value");
	}

	public void actionPerformed(ActionEvent e) {
		InputPort port = (InputPort) modelObject;
		String value = port.getDefaultValue();
		String new_value = (String) JOptionPane.showInputDialog(UIUtils.getActionEventParentWindow(e),
				"Default value for port " + port, "Default value", JOptionPane.QUESTION_MESSAGE,
				null, null, value);
		if (new_value == null || new_value.equals(value)) {
			return;
		}
		port.setDefaultValue(new_value);
	}

}
