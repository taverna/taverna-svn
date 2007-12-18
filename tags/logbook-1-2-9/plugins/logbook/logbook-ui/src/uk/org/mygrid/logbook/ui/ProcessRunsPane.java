package uk.org.mygrid.logbook.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ResultItemPanel;
import org.embl.ebi.escience.scuflui.ScuflDiagramPanel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;
import uk.org.mygrid.logbook.util.Utils;

public class ProcessRunsPane extends JTabbedPane implements UIComponentSPI {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(ProcessRunsPane.class);

	public static final String RESULTS_TAB_NAME = "Results";

	public static final String STATUS_TAB_NAME = "Status";

	public static final String DIAGRAM_TAB_NAME = "Diagram";

	public static final String INPUTS_TAB_NAME = "Inputs";

	public static final String DESCRIPTION_TAB_NAME = "Description";

	private ProcessRunsTreeTableModel processRunsTreeTableModel;

	private ProcessRunsTreeTable processorRunsTreeTable;

	private int previousRow = -1;

	IntermediateResultsPane intermediateResults;

	private JTabbedPane workflowResults;

	private JTabbedPane workflowInputs;

	private ScuflModel workflowModel = new ScuflModel();

	private ScuflDiagramPanel workflowDiagram;

	private final LogBookUIModel logBookUIModel;

	private JTextArea descriptionArea;

	public ProcessRunsPane(LogBookUIModel model) {

		this.logBookUIModel = model;
		final JPanel processorListPanel = new JPanel();
		processorListPanel.setLayout(new BoxLayout(processorListPanel,
				BoxLayout.PAGE_AXIS));
		processorListPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Processor statii"));
		processorListPanel.setPreferredSize(new Dimension(500, 120));
		processRunsTreeTableModel = new ProcessRunsTreeTableModel("");

		processorRunsTreeTable = new ProcessRunsTreeTable(
				processRunsTreeTableModel, logBookUIModel, this);
		processorRunsTreeTable.getTree().setCellRenderer(
				new ProcessRunsTreeTableRenderer());

		intermediateResults = new IntermediateResultsPane();

		add(processorListPanel);

		this.setVisible(true);
		JScrollPane scrollPane = new JScrollPane(processorRunsTreeTable);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		scrollPane.setMinimumSize(new Dimension(500, 120));
		scrollPane.getViewport().setBackground(java.awt.Color.WHITE);
		JSplitPane statusSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				scrollPane, intermediateResults);

		statusSplitPane.setDividerSize(7);
		statusSplitPane.setOneTouchExpandable(true);
		processorListPanel.add(statusSplitPane);

		// Mouse Listener the update the intermediate inputs/outputs based on
		// the processor selected
		// in the table. The Data store is accessed here.

		processorRunsTreeTable
				.addMouseListener(createProcessRunsTreeTableMouseListener());

		workflowDiagram = new ScuflDiagramPanel();
		workflowDiagram.attachToModel(workflowModel);

		add(processorListPanel, STATUS_TAB_NAME);
		workflowResults = new JTabbedPane();
		workflowInputs = new JTabbedPane();

		JPanel descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.setPreferredSize(new Dimension(400, 400));
		descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane descriptionPane = new JScrollPane(descriptionArea);
		descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

		add(workflowInputs, INPUTS_TAB_NAME);
		add(workflowResults, RESULTS_TAB_NAME);
		add(workflowDiagram, DIAGRAM_TAB_NAME);
		add(descriptionPane, DESCRIPTION_TAB_NAME);
		setEnabledAt(indexOfTab(INPUTS_TAB_NAME), false);
		setEnabledAt(indexOfTab(RESULTS_TAB_NAME), false);
		setEnabledAt(indexOfTab(DIAGRAM_TAB_NAME), false);
		setEnabledAt(indexOfTab(DESCRIPTION_TAB_NAME), false);

	}

	private MouseAdapter createProcessRunsTreeTableMouseListener() {
		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				int row = processorRunsTreeTable.getSelectedRow();

				if (row > 0 && row != previousRow) {
					previousRow = row;

					TreePath path = processorRunsTreeTable.getTree()
							.getSelectionPath();
					if (path != null) {
						Object userObject = ((DefaultMutableTreeNode) path
								.getLastPathComponent()).getUserObject();

						if (userObject instanceof ProcessRun) {
							ProcessRun processRun = (ProcessRun) userObject;

							// intermediateResults.update(processRun);

							for (int i = intermediateResults.getTabCount() - 1; i > 0; i--) {

								if (intermediateResults.getTitleAt(i).equals(
										"Failer Details"))
									intermediateResults.remove(i);

							}

							if (processRun.isFailed()) {
								JTextArea reason = new JTextArea();
								reason.setText(processRun.getCause());
								reason.setEditable(false);
								intermediateResults.add("Failer Details",
										reason);

							}

							Map<String, DataThing> processInputs = logBookUIModel
									.getProcessInputs(processRun);
							Map<String, DataThing> processOutputs = logBookUIModel
									.getProcessOutputs(processRun);
							clearIntermediateResults();

							for (String name : processInputs.keySet()) {
								DataThing d = processInputs.get(name);
								intermediateResults.addInput(d, name);
							}

							for (String name : processOutputs.keySet()) {
								DataThing d = processOutputs.get(name);
								intermediateResults.addOutput(d, name);

							}

						}
					}
				}

			}
		};
	}

	public void setProcessData(WorkflowRun w, List<ProcessRun> data) {
		processRunsTreeTableModel.setData(w, data);
		processorRunsTreeTable.getColumnModel().getColumn(0).setMinWidth(150);
	}

	public void addResults(Map<String, DataThing> outputs) {
		for (String name : outputs.keySet()) {
			DataThing d = (DataThing) outputs.get(name);
			ResultItemPanel ripOutput = new ResultItemPanel(d);

			name = Utils.outputLocalName(name);
			workflowResults.add(name, ripOutput);
		}
		setEnabledAt(indexOfTab(RESULTS_TAB_NAME), true);
	}

	public void addInputs(Map<String, DataThing> inputs) {
		for (String name : inputs.keySet()) {
			DataThing d = (DataThing) inputs.get(name);
			ResultItemPanel ripInput = new ResultItemPanel(d);
			// name = Utilities.inputLocalName(name);
			workflowInputs.add(name, ripInput);
		}
		setEnabledAt(indexOfTab(INPUTS_TAB_NAME), true);
	}

	public void clearIntermediateResults() {

		intermediateResults.clear();

	}

	public void removeResults() {
		workflowResults.removeAll();
	}

	public void removeInputs() {
		workflowInputs.removeAll();
	}

	/**
	 * Updates the workflowModel with a new workflow
	 * 
	 * @param workflow
	 *            the new workflow
	 */
	public void updateWorkflowModel(Workflow workflow) {
		if (workflow != null) {
			String workflowLSID = null;

			workflowLSID = workflow.getLsid();
			setEnabledAt(indexOfTab(STATUS_TAB_NAME), false);
			setEnabledAt(indexOfTab(INPUTS_TAB_NAME), false);
			setEnabledAt(indexOfTab(RESULTS_TAB_NAME), false);
			String description = workflow.getDescription();
			if (description != null && !description.trim().equals("")) {
				setDescription(description);
				setEnabledAt(indexOfTab(DESCRIPTION_TAB_NAME), true);
			} else {
				setEnabledAt(indexOfTab(DESCRIPTION_TAB_NAME), false);
			}
			setSelectedIndex(indexOfTab(DIAGRAM_TAB_NAME));
			try {
				if (workflowLSID != null) {
					workflowModel = logBookUIModel
							.retrieveWorkflow(workflowLSID);
					workflowDiagram.detachFromModel();
					workflowDiagram.attachToModel(workflowModel);
					setEnabledAt(indexOfTab(DIAGRAM_TAB_NAME), true);
				}
			} catch (Exception e) {
				// int diagramIndex = indexOfTab(DIAGRAM_TAB_NAME);
				// setEnabledAt(diagramIndex, false);
				// if (getSelectedIndex() == diagramIndex) {
				// setSelectedIndex(indexOfTab(STATUS_TAB_NAME));
				// }
				logger.warn(e);
			}
		} else {
			int diagramIndex = indexOfTab(DIAGRAM_TAB_NAME);
			setEnabledAt(diagramIndex, false);
			if (getSelectedIndex() == diagramIndex) {
				setSelectedIndex(indexOfTab(STATUS_TAB_NAME));
			}
		}
	}

	/**
	 * Updates the workflowModel with a new workflow
	 * 
	 * @param workflow
	 *            the new workflow
	 */
	public void updateWorkflowModel(WorkflowRun workflowRun) {
		if (workflowRun != null) {
			String workflowId = workflowRun.getWorkflowId();
			setEnabledAt(indexOfTab(STATUS_TAB_NAME), true);
			setEnabledAt(indexOfTab(RESULTS_TAB_NAME), true);
			setSelectedIndex(indexOfTab(STATUS_TAB_NAME));
			try {
				if (workflowId != null) {
					workflowModel = logBookUIModel
							.retrieveWorkflow(workflowId);
					workflowDiagram.detachFromModel();
					workflowDiagram.attachToModel(workflowModel);
					setEnabledAt(indexOfTab(DIAGRAM_TAB_NAME), true);
				}
			} catch (Exception e) {
				// int diagramIndex = indexOfTab(DIAGRAM_TAB_NAME);
				// setEnabledAt(diagramIndex, false);
				// if (getSelectedIndex() == diagramIndex) {
				// setSelectedIndex(indexOfTab(STATUS_TAB_NAME));
				// }
				logger.warn(e);
			}
		} else {
			int diagramIndex = indexOfTab(DIAGRAM_TAB_NAME);
			setEnabledAt(diagramIndex, false);
			if (getSelectedIndex() == diagramIndex) {
				setSelectedIndex(indexOfTab(STATUS_TAB_NAME));
			}
		}
	}

	private void setDescription(String description) {
		descriptionArea.setText(description);
	}

	public void attachToModel(ScuflModel model) {
		// TODO Auto-generated method stub

	}

	public void detachFromModel() {
		// TODO Auto-generated method stub

	}

	public ImageIcon getIcon() {

		return TavernaIcons.windowDiagram;
	}

	public String getName() {
		return "Nested Workflow";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

}
