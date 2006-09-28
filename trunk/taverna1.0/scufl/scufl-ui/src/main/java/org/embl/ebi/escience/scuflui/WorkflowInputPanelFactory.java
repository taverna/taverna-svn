package org.embl.ebi.escience.scuflui;

import java.util.Map;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class WorkflowInputPanelFactory implements UIComponentFactorySPI {

	private static int count = 0;
	
	public String getName() {
		return "Workflow input panel";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowRun;
	}

	public UIComponentSPI getComponent() {
		return new WorkflowInputMapBuilder() {

			@Override
			public void launchEnactorDisplay(Map inputObject) {
				EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
				ScuflModel workflowModel = 
					(ScuflModel)UIUtils.getNamedModel("activeWorkflow");
				try {
					WorkflowInstance instance = enactor.compileWorkflow(workflowModel, inputObject, EnactorInvocation.USERCONTEXT);
					UIUtils.setModel("workflowInstance"+(count++), instance);
				} catch (WorkflowSubmissionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
			
		};
	}

}
