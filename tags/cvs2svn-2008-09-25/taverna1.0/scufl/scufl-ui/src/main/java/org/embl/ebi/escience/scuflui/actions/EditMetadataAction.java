package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflSemanticMarkupEditor;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

@SuppressWarnings("serial")
public class EditMetadataAction extends ModelObjectAction {

	public EditMetadataAction(ScuflModel model, Port modelObject) {
		super(model, modelObject);		
		putValue(SMALL_ICON, TavernaIcons.editIcon);
		putValue(NAME, "Edit metadata...");		
	}

	public void actionPerformed(ActionEvent e) {
		Port port = (Port) modelObject;  
		UIUtils.createFrame(model, new ScuflSemanticMarkupEditor(
				port.getMetadata()), 100, 100, 400, 600);
	}

}
