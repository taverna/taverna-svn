package net.sf.taverna.t2.plugin.pretest;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.compatibility.WorkflowTranslationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheckerFactory;

import org.embl.ebi.escience.scufl.ScuflModel;

public class HealthCheckReportPanel extends JPanel {

	private static final long serialVersionUID = 3317933644645279003L;

	private JLabel statusText;
	private HealthReportTreeModel reportTreeModel;
	private JTree reportTree;
	private ScuflModel scuflModel;
	private JProgressBar progressBar;
	private boolean closed = false;

	public HealthCheckReportPanel() {
		super();
		setLayout(new BorderLayout());

		statusText = new JLabel("About to start ...");
		statusText.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(statusText, BorderLayout.NORTH);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);

		add(progressBar, BorderLayout.SOUTH);
		
		reportTreeModel = new HealthReportTreeModel();
		reportTree = new JTree(reportTreeModel);
		reportTree.setBorder(new EmptyBorder(5, 5, 5, 5));
		reportTree.setShowsRootHandles(true);
		reportTree.setCellRenderer(new HealthReportCellRenderer());
		
		add(new JScrollPane(reportTree), BorderLayout.CENTER);

		revalidate();
	}
	
	public void setModel(ScuflModel scuflModel) {
		this.scuflModel = scuflModel;
		reportTreeModel = new HealthReportTreeModel();
		reportTree.setModel(reportTreeModel);
	}

	public void start() {
		progressBar.setIndeterminate(true);
		Dataflow dataflow = doTranslation();
		reportTree.expandPath(reportTree.getPathForRow(0));
		if (dataflow != null && !closed) {
			if (doValidation(dataflow) && !closed) {
				checkProcessors(dataflow);
			}
		}
		setStatus("Health check complete.");
		if (progressBar.isIndeterminate()) {
			progressBar.setValue(progressBar.getMaximum());
			progressBar.setIndeterminate(false);
		}
	}


	private void close() {
		closed = true;
		JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
		if (frame != null) {
			frame.setVisible(false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkProcessors(Dataflow dataflow) {
		List<? extends Processor> processors = dataflow.getProcessors();
		if (processors.size() > 0) {
			int processorsChecked = 0;
			progressBar.setMaximum(processors.size());
			progressBar.setValue(processorsChecked);
			progressBar.setIndeterminate(false);
			for (Processor processor : processors) {
				if (closed) {
					break;
				}
				setStatus("Checking health for processor:"
						+ processor.getLocalName());
				for (HealthChecker checker : HealthCheckerFactory.getInstance().getHealthCheckersForObject(processor)) {
					HealthReport report = checker.checkHealth(processor);
					reportTreeModel.addHealthReport(report);
				}
				progressBar.setValue(++processorsChecked);
			}
		}
	}

	private boolean doValidation(Dataflow dataflow) {
		setStatus("Validating dataflow");
		DataflowValidationReport validationReport = dataflow.checkValidity();
		if (validationReport.isValid()) {
			reportTreeModel.addHealthReport(new HealthReport(dataflow.getLocalName(),
					"Validated OK", Status.OK));
		} else {
			reportTreeModel.addHealthReport(new HealthReport(dataflow.getLocalName(),
					"There was a problem validating the dataflow",
					Status.SEVERE));
		}
		return validationReport.isValid();
	}

	private void setStatus(String status) {
		statusText.setText(status);
		progressBar.setString(status);
	}

	private Dataflow doTranslation() {
		Dataflow dataflow = null;
		setStatus("Translating workflow");
		HealthReport report;
		try {
			dataflow = WorkflowModelTranslator.doTranslation(scuflModel);
			report = new HealthReport(scuflModel.getDescription().getTitle(),"Translated successfully", Status.OK);
		} catch (WorkflowTranslationException e) {
			report = new HealthReport(scuflModel.getDescription().getTitle(),"This workflow cannot be translated:"
					+ e.getMessage(), Status.SEVERE);
		}
		reportTreeModel.addHealthReport(report);
		return dataflow;

	}

}
