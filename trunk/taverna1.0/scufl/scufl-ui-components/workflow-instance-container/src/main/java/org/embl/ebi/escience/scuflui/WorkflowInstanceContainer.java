package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowInstanceSetViewSPI;

/**
 * Holds a set of named workflow instances pushed into it from the implementation
 * of the UIUtils model listener. Constructs a new EnactorInvocation object for
 * each model it receives, adding a toolbar to allow the disposal of the invocation
 * object.
 * @author Tom Oinn
 */
@SuppressWarnings("serial")
public class WorkflowInstanceContainer extends JPanel implements WorkflowInstanceSetViewSPI {

	private JTabbedPane tabs = new JTabbedPane();
	private Map<String, EnactorInvocation> invocations = 
		new HashMap<String, EnactorInvocation>();	
	
	public WorkflowInstanceContainer() {
		super(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
	}
	
	public void removeWorkflowInstance(String modelName) {
		EnactorInvocation invocation=invocations.get(modelName);
		if (invocation!=null) {
			tabs.remove(invocation);
			invocations.remove(modelName);
			invocation.onDispose();
		}
	}

	public void newWorkflowInstance(String modelName, WorkflowInstance instance) {
		try {
			EnactorInvocation enactorInvocationPanel = new EnactorInvocation(instance);
			invocations.put(modelName, enactorInvocationPanel);
			String title = instance.getWorkflowModel().getDescription().getTitle();			
			tabs.addTab(title, TavernaIcons.windowRun,enactorInvocationPanel);				
			tabs.setSelectedComponent(enactorInvocationPanel);						
			
		} catch (WorkflowSubmissionException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return "Workflow instances";
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Call the onDispose method of any UIComponents within the tabbed
	 * pane, this should enforce unbinding and release of all the workflow
	 * resources. This should rarely happen though as we're almost certainly
	 * going to want to keep this component in the named component map as
	 * we don't want workflow instances to be cancelled just because the user
	 * switched to a different perspective.
	 */
	public void onDispose() {
		for (Component c : tabs.getComponents()) {
			if (c instanceof UIComponentSPI) {
				UIComponentSPI uic = (UIComponentSPI)c;
				uic.onDispose();
			}
		}
	}		
}
