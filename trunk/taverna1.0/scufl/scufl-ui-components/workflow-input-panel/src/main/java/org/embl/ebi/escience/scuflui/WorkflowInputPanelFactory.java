package org.embl.ebi.escience.scuflui;

import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class WorkflowInputPanelFactory implements UIComponentFactorySPI {

	private static Logger logger = Logger.getLogger(WorkflowInputPanelFactory.class);	
	
	public String getName() {
		return "Workflow input panel";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowRun;
	}

	@SuppressWarnings("serial")
	public UIComponentSPI getComponent() {
		return new WorkflowInputMapBuilder() {

			@Override
			public void launchEnactorDisplay(Map inputObject) {
				EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
				ScuflModel workflowModel = 
					(ScuflModel)ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_WORKFLOW);
				try {
					WorkflowInstance instance = enactor.compileWorkflow(workflowModel, inputObject, EnactorInvocation.USERCONTEXT);
					logger.debug("Compiled workflow " + instance);
					ModelMap.getInstance().setModel(instance.getID(), instance);					
					logger.debug("Running the workflow " + instance);
				} catch (WorkflowSubmissionException e) {
					logger.error("Could not submit workflow", e);
				}	
			}
		};
	}

}
