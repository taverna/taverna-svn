package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cyclone.WorkflowModelTranslator;
import net.sf.taverna.t2.cyclone.WorkflowTranslationException;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.plugin.input.InputComponent;
import net.sf.taverna.t2.plugin.input.InputComponent.InputComponentCallback;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class T2Component extends JPanel implements WorkflowModelViewSPI {

	private static final Logger logger = Logger.getLogger(T2Component.class);

	private ScuflModel model;

	private JButton runButton;

	private JButton stopButton;

	private JTextArea runStatus;

	private JScrollPane runStatusScrollPane;

	private ResultComponent resultComponent = (ResultComponent) new ResultComponentFactory()
			.getComponent();

	private WorkflowInstanceFacadeImpl facade;

	private int results = 0;

	public T2Component() {
		setLayout(new BorderLayout());

		runButton = createRunButton();
		stopButton = new JButton(new AbstractAction("Stop") {

			public void actionPerformed(ActionEvent e) {
				// TODO: Actually stop the workflow
				runButton.setEnabled(true);
			}
			
		});

		runStatus = new JTextArea();
		runStatus.setSize(new Dimension(0, 200));

		JPanel runStatusPanel = new JPanel(new BorderLayout());
		runStatusPanel.add(runStatus, BorderLayout.CENTER);
		runStatusPanel.add(Box.createVerticalStrut(200), BorderLayout.EAST);

		runStatusScrollPane = new JScrollPane(runStatusPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(runButton);
		buttonPanel.add(stopButton);

		JSplitPane midPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		midPanel.add(runStatusScrollPane);
		midPanel.add(resultComponent);

		add(buttonPanel, BorderLayout.NORTH);
		add(midPanel, BorderLayout.CENTER);
	}

	private JButton createRunButton() {
		JButton button = new JButton("Run");

		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (model.getProcessors().length > 0
						&& model.getWorkflowSinkPorts().length > 0) {
					runButton.setEnabled(false);
					runStatus.setText("");
					resultComponent.clear();

					ContextManager.baseManager = new InMemoryDataManager(
							"namespace", Collections.EMPTY_SET);
					try {
						updateStatus("Translating workflow...");
						final Dataflow dataflow = WorkflowModelTranslator
								.doTranslation(model);
						updateStatus("done\n");
						updateStatus("Validating workflow...");
						DataflowValidationReport report = dataflow
								.checkValidity();
						if (report.isValid()) {
							updateStatus("done\n");

							List<? extends DataflowInputPort> inputPorts = dataflow
									.getInputPorts();
							if (!inputPorts.isEmpty()) {
								JDialog dialog = new JDialog();
								InputComponent inputComp = new InputComponent(
										inputPorts,
										new InputComponentCallback<DataflowInputPort>() {

											public String getButtonText() {
												return "Run workflow";
											}

											public void invoke(
													Map<DataflowInputPort, EntityIdentifier> entities) {
												try {
													System.out
															.println("Running with "
																	+ entities);
													runWorkflow(dataflow,
															entities);
												} catch (EditException e) {
													logger.error(e);
													updateStatus("failed\n");
													showErrorDialog(
															"Unable to translate workflow",
															e.getMessage());
													runButton.setEnabled(true);
												}
											}

										});
								dialog.add(inputComp);
								dialog.setSize(640,480);
								dialog.setVisible(true);
								return;
							}
							runWorkflow(dataflow, null);
						} else {
							updateStatus("failed\n");
							showErrorDialog("Unable to translate workflow",
									"Workflow validation failed");
							runButton.setEnabled(true);
						}
					} catch (EditException e) {
						logger.error(e);
						updateStatus("failed\n");
						showErrorDialog("Unable to translate workflow", e
								.getMessage());
						runButton.setEnabled(true);
					} catch (WorkflowTranslationException e) {
						logger.error(e);
						updateStatus("failed\n");
						showErrorDialog("Unable to translate workflow", e
								.getMessage());
						runButton.setEnabled(true);
					}

				}
			}

		});

		return button;
	}

	private void showErrorDialog(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	private String indexString(int[] index) {
		StringBuffer result = new StringBuffer();
		result.append('[');
		for (int i = 0; i < index.length; i++) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(index[i]);
		}
		result.append(']');
		return result.toString();
	}

	private void updateStatus(String status) {
		runStatus.append(status);
		JScrollBar scrollBar = runStatusScrollPane.getVerticalScrollBar();
		if (scrollBar != null) {
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}

	public void attachToModel(ScuflModel model) {
		this.model = model;
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

	protected void runWorkflow(final Dataflow dataflow,
			Map<DataflowInputPort, EntityIdentifier> entities)
			throws EditException {
		facade = new WorkflowInstanceFacadeImpl(dataflow);
		facade.addResultListener(new ResultListener() {

			public void resultTokenProduced(EntityIdentifier token,
					int[] index, String portName, String owningProcess) {
				if (index.length == 0) {
					results++;
					if (results == dataflow.getOutputPorts().size()) {
						resultComponent.deregister(facade);
						facade.removeResultListener(this);
						runButton.setEnabled(true);
						results = 0;
					}
				}
				updateStatus("Result " + indexString(index) + " for port "
						+ portName + "\n");
			}

		});
		resultComponent.register(facade);
		facade.fire();
		if (entities != null) {
			for (Entry<DataflowInputPort, EntityIdentifier> entry : entities
					.entrySet()) {
				DataflowInputPort inputPort = entry.getKey();
				EntityIdentifier identifier = entry.getValue();
				int[] index = new int[] {};
				try {
					facade.pushData(identifier, index, inputPort.getName());
				} catch (TokenOrderException e) {
					e.printStackTrace();
					updateStatus("Could not submit data for port " + inputPort);
				}
			}
		}

	}

}
