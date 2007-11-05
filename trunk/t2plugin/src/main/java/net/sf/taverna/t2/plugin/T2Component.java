package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cyclone.WorkflowModelTranslator;
import net.sf.taverna.t2.cyclone.WorkflowTranslationException;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class T2Component extends JPanel implements WorkflowModelViewSPI {

	private static final Logger logger = Logger.getLogger(T2Component.class);

	private Dataflow dataflow;
	
	private JButton runButton;
	
	private JButton stopButton;
	
	private JTextArea translationStatus;
	
	private ResultComponent resultComponent = (ResultComponent) new ResultComponentFactory().getComponent();
	
	private int results = 0;
	
	public T2Component() {
		setLayout(new BorderLayout());
		
		runButton = createRunButton();
		stopButton = new JButton("Stop");
		
		translationStatus = new JTextArea();
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(runButton);
		buttonPanel.add(stopButton);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(translationStatus, BorderLayout.NORTH);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(topPanel, BorderLayout.NORTH);
		add(resultComponent, BorderLayout.CENTER);
	}

	private JButton createRunButton() {
		final JButton runButton = new JButton("Run");
		runButton.setEnabled(false);
		add(runButton);
		runButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				ContextManager.baseManager = new InMemoryDataManager("namespace", Collections.EMPTY_SET);

				runButton.setEnabled(false);
				try {
					
					resultComponent.register(dataflow, new ResultListener() {

						public void resultTokenProduced(EntityIdentifier token,
								int[] index, String portName) {
							logger.info("Result for " + portName);
							if (index.length == 0) {
								results++;
								if (results == dataflow.getOutputPorts().size()) {
									runButton.setEnabled(true);
									results = 0;
								}
							}
						}
						
					});
					for (Processor processor : dataflow.getProcessors()) {
						if (processor.getInputPorts().size() == 0) {
							logger.debug("Firing processor : " + processor.getLocalName());
							processor.fire(dataflow.getLocalName());
						}
					}
				} catch (EditException e) {
					logger.error(e);
					showErrorDialog("Unable to translate workflow", e);
					runButton.setEnabled(true);
				}

			}

		});
		
		return runButton;
	}
	
	private Dataflow translateWorkflow(ScuflModel model) {
		Dataflow dataflow = null;
		try {
			dataflow = WorkflowModelTranslator.doTranslation(model);
			DataflowValidationReport report = dataflow.checkValidity();
			if (report.isValid()) {
				translationStatus.setText("Workflow translated OK");
				runButton.setEnabled(true);
			} else {
				translationStatus.setText("Unable to validate translated workflow");				
			}
			
		} catch (WorkflowTranslationException e) {
			logger.error(e);
			translationStatus.setText("Unable to translate workflow\n" + e.getMessage());
			runButton.setEnabled(false);
		}
		return dataflow;
	}
	
	private void showErrorDialog(String title, Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void attachToModel(ScuflModel model) {
		this.dataflow = translateWorkflow(model);
	}

	public void detachFromModel() {

	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDisplay() {

	}

	public void onDispose() {

	}

}
