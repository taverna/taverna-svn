package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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

	private ScuflModel model;

	private JButton runButton;

	private JButton stopButton;

	private JTextArea runStatus;

	private JScrollPane runStatusPane;

	private ResultComponent resultComponent = (ResultComponent) new ResultComponentFactory()
			.getComponent();

	private int results = 0;

	public T2Component() {
		setLayout(new BorderLayout());

		runButton = createRunButton();
		stopButton = new JButton("Stop");

		runStatus = new JTextArea();
		runStatus.setSize(new Dimension(0, 200));
		
		runStatusPane = new JScrollPane(runStatus);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(runButton);
		buttonPanel.add(stopButton);

		JSplitPane midPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		midPanel.add(runStatusPane);
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
							resultComponent.register(dataflow,
									new ResultListener() {

										public void resultTokenProduced(
												EntityIdentifier token,
												int[] index, String portName) {
											logger.info("Result for "
													+ portName);
											if (index.length == 0) {
												results++;
												if (results == dataflow
														.getOutputPorts()
														.size()) {
													runButton.setEnabled(true);
													results = 0;
												}
											} else {
												updateStatus("Result "
														+ indexString(index)
														+ " for port "
														+ portName + "\n");
											}
										}

									});
							for (Processor processor : dataflow.getProcessors()) {
								if (processor.getInputPorts().size() == 0) {
									logger.debug("Firing processor : "
											+ processor.getLocalName());
									processor.fire(dataflow.getLocalName());
								}
							}
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
		JScrollBar scrollBar = runStatusPane.getVerticalScrollBar();
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

}
