package org.embl.ebi.escience.scuflui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sf.taverna.perspectives.EnactPerspective;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

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
		return new WorkflowInputMapBuilder();
	}
	
	/**
	 * Invoke workflow. Pop up a WorkflowInputPanel for the user to specify the
	 * inputs if needed, otherwise the execution will start immediately.
	 * 
	 * @see executeWorkflow(ScuflModel workflow, Map<String, DataThing> inputs)
	 * @param workflow Workflow to run
 	 * @return Recently opened WorkflowInputPanel instance, or 
 	 * null if no inputs were required
	 */
	public static WorkflowInputPanel invokeWorkflow(ScuflModel workflow) {
		return invokeWorkflow(workflow, null);
	}
	
	/**
	 * Invoke workflow. Pop up a WorkflowInputPanel for the user to specify the
	 * inputs if needed, with defaults loaded from the provided <code>inputs</code>
	 * map. 
	 * 
	 * @see executeWorkflow(ScuflModel workflow, Map<String, DataThing> inputs)
	 * @param workflow Workflow to run
	 * @param inputs Map of input values to populate the input panel with
	 * @return Recently opened WorkflowInputPanel instance, or null if no 
	 * inputs were required
	 */
	public static WorkflowInputPanel invokeWorkflow(ScuflModel workflow, Map<String, DataThing> inputs) {
		if (workflow.getWorkflowSourcePorts().length == 0) {
			// Execute with empty inputs, without popping up any dialogue box
			executeWorkflow(workflow, new HashMap<String, DataThing>());
			return null;
		} 
		WorkflowInputPanel panel = new WorkflowInputPanel(workflow);
		if (inputs != null && inputs.size() > 0) {
			try {
                panel.setInputs(inputs);
            } catch (InputsNotMatchingException e) {
                Object[] options = { "Ok" };
                JOptionPane
                        .showOptionDialog(
                                null,
                                "Inputs do not match.",
                                "Warning", JOptionPane.NO_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, options,
                                options[0]);
                logger.warn("Inputs for workflow " + workflow + " don't match", e);
            }			
		}
		UIUtils.createFrame(panel, 50, 50, 600, 600);
		return panel;
	}

	/**
	 * Immediately execute workflow. Workflow is submitted to the enactor with the
	 * given input values, and invocation is initiated. Switches to the 
	 * @link{EnactPerspective}.
	 * 
	 * @param workflow Workflow to run
	 * @param inputs Map of input values to run the workflow with
	 * @throws  
	 */
	public static void executeWorkflow(ScuflModel workflow, Map<String, DataThing> inputs) {
		EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
		WorkflowInstance instance;
		try {
			instance = enactor.compileWorkflow(workflow, inputs, 
					EnactorInvocation.USERCONTEXT);
		} catch (WorkflowSubmissionException e) {
			logger.error("Could not compile workflow " + workflow, e);
			return;
		}
		logger.debug("Compiled workflow " + instance);
		ModelMap.getInstance().setModel(ModelMap.CURRENT_PERSPECTIVE, 
				new EnactPerspective());
		ModelMap.getInstance().setModel(instance.getID(), instance);					
		logger.debug("Running the workflow " + instance);
		try {
			instance.run();
		} catch (InvalidInputException e) {
			logger.warn("Could not run workflow " + instance, e);
		}
		
	}
	

}
