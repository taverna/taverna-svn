package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

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
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.plugin.input.InputComponent;
import net.sf.taverna.t2.plugin.input.InputComponent.InputComponentCallback;
import net.sf.taverna.t2.plugin.pretest.HealthCheckReportPanel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;

public class RunComponent extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RunComponent.class);

	private T2Component t2Component;

	private ScuflModel model;

	private JButton runButton;

	private JButton testButton;

	private JLabel runStatus;

	private JPanel runStatusPanel;
	
	private Date runDate; 
	
	private SVGDiagram svgDiagram = new SVGDiagram();

	private JSVGCanvas svgCanvas = svgDiagram.getSvgCanvas();

	private SVGDiagramMonitor svgDiagramMonitor = new SVGDiagramMonitor();

	private HealthCheckReportPanel reportPanel;

	private JSplitPane resultPanel;
	
	private JSplitPane midPanel;

	private RenderedResultComponent renderedResultComponent = new RenderedResultComponent(); 

	private ResultComponent resultComponent = new ResultComponent(renderedResultComponent);

	private WorkflowInstanceFacadeImpl facade;

	private int results = 0;

	public RunComponent(T2Component t2Component) {
		this.t2Component = t2Component;
		setLayout(new BorderLayout());
		testButton = createTestButton();
		runButton = createRunButton();

		Action resetDiagramAction = svgCanvas.new ResetTransformAction();
		resetDiagramAction.putValue(Action.NAME, "Reset Diagram");
		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		zoomInAction.putValue(Action.NAME, "Zoom In");
		Action zoomOutAction = svgCanvas.new ZoomAction(1/1.2);
		zoomOutAction.putValue(Action.NAME, "Zoom Out");
		
		runStatus = new JLabel(" ");
		runStatus.setBorder(new EmptyBorder(5, 5, 5, 5));

		runStatusPanel = new JPanel(new BorderLayout());
		runStatusPanel.add(runStatus, BorderLayout.NORTH);

		runStatusPanel.add(svgDiagram, BorderLayout.CENTER);
		svgDiagramMonitor.setDiagram(svgDiagram);

		reportPanel = new HealthCheckReportPanel();

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(testButton);
		buttonPanel.add(runButton);
		buttonPanel.add(new JButton(resetDiagramAction));
		buttonPanel.add(new JButton(zoomInAction));
		buttonPanel.add(new JButton(zoomOutAction));

		midPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		midPanel.add(runStatusPanel);
		
		resultPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		resultPanel.setTopComponent(resultComponent);
		resultPanel.setBottomComponent(renderedResultComponent);
		
		add(buttonPanel, BorderLayout.NORTH);
		add(midPanel, BorderLayout.CENTER);
	}

	public void setModel(ScuflModel model) {
		this.model = model;
		svgDiagram.setModel(model);
	}
	
	private JButton createTestButton() {
		JButton button = new JButton("Test");

		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				enableRun(false);
				runHealthCheck();
			}
		});
		return button;
	}

	protected void runHealthCheck() {
		if (model != null) {
			reportPanel.setModel(model);

			midPanel.setBottomComponent(reportPanel);
			midPanel.setDividerLocation(0.7);

			Runnable reportThread = new Runnable() {
				public void run() {
					reportPanel.start();
					enableRun(true);
				}
			};

			new Thread(reportThread).start();

		}
	}
	
	public void hideHealthCheck() {
		midPanel.setBottomComponent(null);
	}

	public String toString() {
		if (runDate != null) {
			return model.getDescription().getTitle() +  " " + DateFormat.getTimeInstance().format(runDate);
		} else {
			return "Current workflow";
		}
	}
	
	private JButton createRunButton() {
		JButton button = new JButton("Run");

		button.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent arg0) {
				
				if (model.getProcessors().length > 0
						&& model.getWorkflowSinkPorts().length > 0) {
					runDate = new Date();
					t2Component.addRun(RunComponent.this);
					enableRun(false);
					updateStatus("");
					midPanel.setDividerLocation(0.7);
					midPanel.setBottomComponent(resultPanel);
//					resultPanel.setDividerLocation(0.5);
					resultPanel.setDividerLocation(500);

					try {
						updateStatus("Translating workflow...");
						final Dataflow dataflow = WorkflowModelTranslator
								.doTranslation(model);
						updateStatus("Translating workflow...done");
						updateStatus("Validating workflow...");
						DataflowValidationReport report = dataflow
								.checkValidity();
						if (report.isValid()) {
							updateStatus("Validating workflow...done");

							List<? extends DataflowInputPort> inputPorts = dataflow
									.getInputPorts();
							final InvocationContext context = createContext();
							if (!inputPorts.isEmpty()) {
								final JDialog dialog = new JDialog();
								InputComponent inputComp = new InputComponent(
										inputPorts,
										new InputComponentCallback<DataflowInputPort>() {

											public String getButtonText() {
												return "Run workflow";
											}

											public void invoke(
													Map<DataflowInputPort, EntityIdentifier> entities) {
												try {
													dialog.setVisible(false);
													logger
															.info("Running workflow with "
																	+ entities);
													updateStatus("Running workflow...");
													runWorkflow(dataflow,
															entities, context);
												} catch (EditException e) {
													logger.error(e);
													updateStatus("Running workflow...failed");
													showErrorDialog(
															"Unable to run workflow",
															e.getMessage());
													MonitorManager.getInstance().removeObserver(svgDiagramMonitor);
//													enableRun(true);
												}
											}

										}, context);
								dialog.add(inputComp);
								dialog.setSize(640, 480);
								dialog.setVisible(true);
								return;
							}
							updateStatus("Running workflow...");
							runWorkflow(dataflow, null, context);
						} else {
							updateStatus("Validating workflow...failed");
							showErrorDialog("Unable to validate workflow",
									"Workflow validation failed");
							MonitorManager.getInstance().removeObserver(svgDiagramMonitor);
//							enableRun(true);
						}
					} catch (EditException e) {
						logger.error(e);
						updateStatus("Running workflow...failed");
						showErrorDialog("Unable to run workflow", e
								.getMessage());
						MonitorManager.getInstance().removeObserver(svgDiagramMonitor);
//						enableRun(true);
					} catch (WorkflowTranslationException e) {
						logger.error(e);
						updateStatus("Translating workflow...failed");
						showErrorDialog("Unable to translate workflow", e
								.getMessage());
						MonitorManager.getInstance().removeObserver(svgDiagramMonitor);
//						enableRun(true);
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
		runStatus.setText(status);
	}


	protected InvocationContext createContext() {
		// final DataManager dataManager = new InMemoryDataManager("namespace",
		// Collections.EMPTY_SET);

		String dataManagerStore = T2Component.userPrefs.get(T2Component.DATA_STORE_PROPERTY,
				T2Component.defaultDataManagerDir.getAbsolutePath());
		File dataManagerDir = new File(dataManagerStore);

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
		resultComponent.setContext(context);
		MonitorManager.getInstance().addObserver(svgDiagramMonitor);
		// Use the empty context by default to root this facade on the monitor
		// tree
		facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
		facade.addResultListener(new ResultListener() {

			public void resultTokenProduced(WorkflowDataToken token,
					String portName) {
				if (token.getIndex().length == 0) {
					results++;
					if (results == dataflow.getOutputPorts().size()) {
						resultComponent.deregister(facade);
						facade.removeResultListener(this);
						updateStatus("Workflow complete");
						MonitorManager.getInstance().removeObserver(svgDiagramMonitor);
//						enableRun(true);
						results = 0;
					}
				}
				// updateStatus("Result " + indexString(token.getIndex())
				// + " for port " + portName);
			}

		});
		determineOutputMimeTypes();
		resultComponent.register(facade);
		facade.fire();
		if (entities != null) {
			for (Entry<DataflowInputPort, EntityIdentifier> entry : entities
					.entrySet()) {
				DataflowInputPort inputPort = entry.getKey();
				EntityIdentifier identifier = entry.getValue();
				int[] index = new int[] {};
				try {
					facade.pushData(new WorkflowDataToken("", index,
							identifier, context), inputPort.getName());
				} catch (TokenOrderException e) {
					e.printStackTrace();
					updateStatus("Could not submit data for port " + inputPort);
				}
			}
		}

	}

	private void determineOutputMimeTypes() {
		// FIXME get mime types from annotations on DataflowOutputPorts
		Map<String, List<String>> mimeTypeMap = new HashMap<String, List<String>>();
		for (Port port : this.model.getWorkflowSinkPorts()) {
			String name2 = port.getName();
			String syntacticType = port.getSyntacticType();
			List<String> typeList = port.getMetadata().getMIMETypeList();

			if (!typeList.contains(syntacticType)) {
				typeList.add(syntacticType);
			}
			mimeTypeMap.put(name2, typeList);
		}
		this.resultComponent.setOutputMimeTypes(mimeTypeMap);
	}

	private void enableRun(boolean enabled) {
		runButton.setEnabled(enabled);
		testButton.setEnabled(enabled);
	}

}
