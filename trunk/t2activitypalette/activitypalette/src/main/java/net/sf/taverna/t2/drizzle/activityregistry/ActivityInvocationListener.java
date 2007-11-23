/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessor;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessorFactory;
import org.embl.ebi.escience.scuflui.WorkflowInputPanelFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public class ActivityInvocationListener implements ActionListener {
	
	private ProcessorFactory pf;

	public ActivityInvocationListener(final ProcessorFactory pf) {
		this.pf = pf;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		try {
			final ScuflModel m;
			if (pf instanceof ScuflWorkflowProcessorFactory) {
				ScuflWorkflowProcessor wp = (ScuflWorkflowProcessor) pf.createProcessor("workflow",
						new ScuflModel());
				m = wp.getInternalModel();
			} else {
				m = new ScuflModel();
				m.getDescription().setTitle(pf.getName());								
				Processor p = pf.createProcessor("processor", m);
				// m.addProcessor(p);
				// Iterate over all inputs and create
				// workflow inputs, similarly for all
				// outputs
				InputPort[] ip = p.getInputPorts();
				for (int i = 0; i < ip.length; i++) {
					String portName = ip[i].getName();
					OutputPort port = new OutputPort(m.getWorkflowSourceProcessor(), portName);
					m.getWorkflowSourceProcessor().addPort(port);
					m.addDataConstraint(new DataConstraint(m, port, ip[i]));
				}
				OutputPort[] op = p.getOutputPorts();
				for (int i = 0; i < op.length; i++) {
					String portName = op[i].getName();
					InputPort port = new InputPort(m.getWorkflowSinkProcessor(), portName);
					m.getWorkflowSinkProcessor().addPort(port);
					m.addDataConstraint(new DataConstraint(m, op[i], port));
				}
				// Should have now created a trivial single
				// processor workflow or the directly loaded
				// more complex one.
			}
			
			WorkflowInputPanelFactory.invokeWorkflow(m);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Unable to run operation : \n" + ex.getMessage(),
					"Exception!", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

}
