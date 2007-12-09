package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cyclone.WorkflowModelTranslator;
import net.sf.taverna.t2.cyclone.WorkflowTranslationException;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.plugin.input.InputComponent;
import net.sf.taverna.t2.plugin.input.InputComponent.InputComponentCallback;
import net.sf.taverna.t2.plugin.pretest.HealthCheckReportPanel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class T2Component extends JPanel implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 6964568042620234711L;

	private static final Logger logger = Logger.getLogger(T2Component.class);

	private ScuflModel model;

	private JButton runButton;
	
	private JButton testButton;

	private JButton stopButton;

	private JTextArea runStatus;
	
	private CardLayout cardLayout;
	
	private JPanel topPanel;
	
	private HealthCheckReportPanel reportPanel;

	private JScrollPane runStatusScrollPane;

	private ResultComponent resultComponent = (ResultComponent) new ResultComponentFactory()
			.getComponent();

	private WorkflowInstanceFacadeImpl facade;

	private int results = 0;

	public T2Component() {
		setLayout(new BorderLayout());
		testButton = createTestButton();
		runButton = createRunButton();
		stopButton = new JButton(new AbstractAction("Stop") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3675250815643062008L;

			public void actionPerformed(ActionEvent e) {
				// TODO: Actually stop the workflow
				runButton.setEnabled(true);
				testButton.setEnabled(true);
			}

		});

		runStatus = new JTextArea();
		runStatus.setSize(new Dimension(0, 200));
		
		cardLayout = new CardLayout();
		topPanel = new JPanel(cardLayout);

		JPanel runStatusPanel = new JPanel(new BorderLayout());
		runStatusPanel.add(runStatus, BorderLayout.CENTER);
		runStatusPanel.add(Box.createVerticalStrut(200), BorderLayout.EAST);

		runStatusScrollPane = new JScrollPane(runStatusPanel);
		topPanel.add(runStatusScrollPane, "run status");
		
		reportPanel = new HealthCheckReportPanel();
		topPanel.add(reportPanel, "health report");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(testButton);
		buttonPanel.add(runButton);
		buttonPanel.add(stopButton);

		JSplitPane midPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		midPanel.add(topPanel);
		midPanel.add(resultComponent);

		add(buttonPanel, BorderLayout.NORTH);
		add(midPanel, BorderLayout.CENTER);
	}

	private JButton createTestButton() {
		JButton button = new JButton("Test");
		
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				testButton.setEnabled(false);
				runButton.setEnabled(false);
				runHealthCheck();
			}
		}
		);
		return button;
	}

	protected void runHealthCheck() {
		if (model!=null) {
			reportPanel.setModel(model);
			
			cardLayout.show(topPanel, "health report");
			
			Runnable reportThread = new Runnable() {
				public void run() {
					reportPanel.start();
					testButton.setEnabled(true);
					runButton.setEnabled(true);
				}	
			};
			
			new Thread(reportThread).start();
			
		}
	}

	private JButton createRunButton() {
		JButton button = new JButton("Run");

		button.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent arg0) {
				if (model.getProcessors().length > 0
						&& model.getWorkflowSinkPorts().length > 0) {
					cardLayout.show(topPanel, "run status");
					runButton.setEnabled(false);
					testButton.setEnabled(false);
					runStatus.setText("");
					resultComponent.clear();

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
							final InvocationContext context = createContext();
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
													logger
															.info("Running workflow with "
																	+ entities);
													runWorkflow(dataflow,
															entities, context);
												} catch (EditException e) {
													logger.error(e);
													updateStatus("failed\n");
													showErrorDialog(
															"Unable to translate workflow",
															e.getMessage());
													runButton.setEnabled(true);
													testButton.setEnabled(true);
												}
											}

										}, context);
								dialog.add(inputComp);
								dialog.setSize(640, 480);
								dialog.setVisible(true);
								return;
							}
							runWorkflow(dataflow, null, context);
						} else {
							updateStatus("failed\n");
							showErrorDialog("Unable to translate workflow",
									"Workflow validation failed");
							runButton.setEnabled(true);
							testButton.setEnabled(true);
						}
					} catch (EditException e) {
						logger.error(e);
						updateStatus("failed\n");
						showErrorDialog("Unable to translate workflow", e
								.getMessage());
						runButton.setEnabled(true);
						testButton.setEnabled(true);
					} catch (WorkflowTranslationException e) {
						logger.error(e);
						updateStatus("failed\n");
						showErrorDialog("Unable to translate workflow", e
								.getMessage());
						runButton.setEnabled(true);
						testButton.setEnabled(true);
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

	protected InvocationContext createContext() {
		// final DataManager dataManager = new InMemoryDataManager("namespace",
		// Collections.EMPTY_SET);
		String tavHome = System.getProperty("taverna.home");
		File dataManagerDir = new File(tavHome, "t2-datamanager");
		final DataManager dataManager = new FileDataManager(UUID.randomUUID()
				.toString(), Collections.EMPTY_SET, dataManagerDir);

		InvocationContext context = new InvocationContext() {
			public DataManager getDataManager() {
				return dataManager;
			}
		};
		return context;
	}

	protected void runWorkflow(final Dataflow dataflow,
			Map<DataflowInputPort, EntityIdentifier> entities,
			InvocationContext context) throws EditException {

		facade = new WorkflowInstanceFacadeImpl(dataflow, context);
		facade.addResultListener(new ResultListener() {

			public void resultTokenProduced(WorkflowDataToken token,
					String portName, String owningProcess) {
				if (token.getIndex().length == 0) {
					results++;
					if (results == dataflow.getOutputPorts().size()) {
						resultComponent.deregister(facade);
						facade.removeResultListener(this);
						runButton.setEnabled(true);
						testButton.setEnabled(true);
						results = 0;
					}
				}
				updateStatus("Result " + indexString(token.getIndex())
						+ " for port " + portName + "\n");
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
