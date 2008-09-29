package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.WorkflowInputPanelFactory;
import org.embl.ebi.escience.scuflui.shared.ModelMap;

public class RunWorkflowAction extends AbstractAction {

	private static ModelMap modelMap = ModelMap.getInstance();
	
	private Component parentComponent;
	
	public RunWorkflowAction(Component parentComponent) {
		this.parentComponent = parentComponent;
		putValue(SMALL_ICON, TavernaIcons.runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");
	}
	
	public void actionPerformed(ActionEvent e) {
		WorkflowInputPanelFactory.invokeWorkflow(
				(ScuflModel) modelMap.getNamedModel(ModelMap.CURRENT_WORKFLOW), parentComponent); 
	}

}
