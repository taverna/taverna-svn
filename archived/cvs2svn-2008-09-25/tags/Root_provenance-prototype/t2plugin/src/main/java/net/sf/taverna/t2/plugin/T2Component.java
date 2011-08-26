package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

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
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.impl.MonitorImpl;
import net.sf.taverna.t2.plugin.input.InputComponent;
import net.sf.taverna.t2.plugin.input.InputComponent.InputComponentCallback;
import net.sf.taverna.t2.plugin.pretest.HealthCheckReportPanel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class T2Component extends JPanel implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 6964568042620234711L;

	private static final Logger logger = Logger.getLogger(T2Component.class);

	static final File defaultDataManagerDir = new File(System
			.getProperty("taverna.home"), "t2-datamanager");

	public static final String DATA_STORE_PROPERTY = "dataManagerDir";

	private static Preferences userPrefs = Preferences
			.userNodeForPackage(T2Component.class);

	private ScuflModel model;

	private JButton runButton;

	private JButton testButton;

	private JButton stopButton;

	private JButton preferencesButton;

	private JLabel runStatus;

	private CardLayout cardLayout;

	private JPanel topPanel;

	private JTree monitorTree;

	private HealthCheckReportPanel reportPanel;

	private ResultComponent resultComponent = (ResultComponent) new ResultComponentFactory()
			.getComponent();

	private PreferencesFrame preferencesFrame = new PreferencesFrame();

	private WorkflowInstanceFacadeImpl facade;

	private int results = 0;

	public T2Component() {
		setLayout(new BorderLayout());
		testButton = createTestButton();
		runButton = createRunButton();
		stopButton = new JButton(new AbstractAction("Reset") {

			private static final long serialVersionUID = -3675250815643062008L;

			public void actionPerformed(ActionEvent e) {
				// TODO: Actually stop the workflow
				enableRun(true);
			}

		});
		stopButton.setEnabled(false);

		preferencesButton = new JButton("Preferences");
		preferencesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				preferencesFrame.pack();
				preferencesFrame.setLocationRelativeTo(T2Component.this);
				preferencesFrame.setVisible(true);
			}

		});

		runStatus = new JLabel(" ");
		runStatus.setBorder(new EmptyBorder(5, 5, 5, 5));

		cardLayout = new CardLayout();
		topPanel = new JPanel(cardLayout);

		monitorTree = MonitorImpl.getJTree();
		monitorTree.setRootVisible(false);
		MonitorImpl.enableMonitoring(true);

		JPanel runStatusPanel = new JPanel(new BorderLayout());
		runStatusPanel.add(runStatus, BorderLayout.NORTH);
		runStatusPanel.add(new JScrollPane(monitorTree), BorderLayout.CENTER);

		topPanel.add(runStatusPanel, "run status");

		reportPanel = new HealthCheckReportPanel();
		topPanel.add(reportPanel, "health report");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(testButton);
		buttonPanel.add(runButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(preferencesButton);

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
				enableRun(false);
				runHealthCheck();
			}
		});
		return button;
	}

	protected void runHealthCheck() {
		if (model != null) {
			reportPanel.setModel(model);

			cardLayout.show(topPanel, "health report");

			Runnable reportThread = new Runnable() {
				public void run() {
					reportPanel.start();
					enableRun(true);
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
					clearMonitorTree();
					cardLayout.show(topPanel, "run status");
					enableRun(false);
					updateStatus("");
					resultComponent.clear();

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
													enableRun(true);
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
							enableRun(true);
						}
					} catch (EditException e) {
						logger.error(e);
						updateStatus("Running workflow...failed");
						showErrorDialog("Unable to run workflow", e
								.getMessage());
						enableRun(true);
					} catch (WorkflowTranslationException e) {
						logger.error(e);
						updateStatus("Translating workflow...failed");
						showErrorDialog("Unable to translate workflow", e
								.getMessage());
						enableRun(true);
					}

				}
			}

		});

		return button;
	}

	protected void clearMonitorTree() {
		Object root = monitorTree.getModel().getRoot();
		if (root instanceof TreeNode) {
			TreeNode rootTreeNode = (TreeNode) root;
			Enumeration children = rootTreeNode.children();
			while (children.hasMoreElements()) {
				Object child = children.nextElement();
				if (child instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode childTreeNode = (DefaultMutableTreeNode) child;
					if (childTreeNode.getUserObject() instanceof MonitorNode) {
						MonitorNode monitorNode = (MonitorNode) childTreeNode
								.getUserObject();
						MonitorImpl.getMonitor().deregisterNode(
								monitorNode.getOwningProcess());
					}
				}
			}
		}
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

		String dataManagerStore = userPrefs.get(DATA_STORE_PROPERTY,
				defaultDataManagerDir.getAbsolutePath());
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
						enableRun(true);
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
		stopButton.setEnabled(!enabled);
	}

}
