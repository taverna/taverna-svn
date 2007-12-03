package net.sf.taverna.t2.plugin.pretest;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import net.sf.taverna.t2.cyclone.WorkflowModelTranslator;
import net.sf.taverna.t2.cyclone.WorkflowTranslationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;

import org.embl.ebi.escience.scufl.ScuflModel;

public class HealthCheckReportPanel extends JPanel {

	private JLabel statusText;
	private HealthReportTreeModel reportTreeModel;
	private JTree reportTree;
	private ScuflModel scuflModel;

	public HealthCheckReportPanel(ScuflModel scuflModel) {
		super();
		this.scuflModel = scuflModel;
		setLayout(new BorderLayout());

		statusText = new JLabel("About to start ...");
		add(statusText, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);

		reportTreeModel = new HealthReportTreeModel();
		reportTree = new JTree(reportTreeModel);
		reportTree.setCellRenderer(new HealthReportCellRenderer());
		reportTree.setExpandsSelectedPaths(true);
		//reportTree.setRowHeight(0);
		
//		reportTreeModel.addTreeModelListener(new TreeModelListener() {
//
//			public void treeNodesChanged(TreeModelEvent e) {
//				reportTree.setSelectionPath(e.getTreePath());
//			}
//
//			public void treeNodesInserted(TreeModelEvent e) {
//				reportTree.setSelectionPath(e.getTreePath());
//			}
//
//			public void treeNodesRemoved(TreeModelEvent e) {
//				
//			}
//
//			public void treeStructureChanged(TreeModelEvent e) {
//				
//			}
//			
//		});
		
		add(new JScrollPane(reportTree), BorderLayout.CENTER);

		revalidate();
	}

	public void start() {
		Dataflow dataflow = doTranslation();
		if (dataflow != null) {
			if (doValidation(dataflow)) {
				checkProcessors(dataflow);
			}
		}
		setStatus("Health check complete.");
	}

	private void checkProcessors(Dataflow dataflow) {
		for (Processor processor : dataflow.getProcessors()) {
			setStatus("Checking health for processor:"
					+ processor.getLocalName());
			HealthReport report = processor.checkProcessorHealth();
			reportTreeModel.addHealthReport(report);
		}
	}

	private boolean doValidation(Dataflow dataflow) {
		setStatus("Validating dataflow");
		DataflowValidationReport validationReport = dataflow.checkValidity();
		if (validationReport.isValid()) {
			reportTreeModel.addHealthReport(new HealthReportImpl(dataflow.getLocalName(),
					"Validated OK", Status.OK));
		} else {
			reportTreeModel.addHealthReport(new HealthReportImpl(dataflow.getLocalName(),
					"There was a problem validating the dataflow",
					Status.SEVERE));
		}
		return validationReport.isValid();
	}

	private void setStatus(String status) {
		statusText.setText(status);
	}

	private Dataflow doTranslation() {
		Dataflow dataflow = null;
		setStatus("Translating workflow");
		HealthReport report;
		try {
			dataflow = WorkflowModelTranslator.doTranslation(scuflModel);
			report = new HealthReportImpl(scuflModel.getDescription().getTitle(),"Translated successfully", Status.OK);
		} catch (WorkflowTranslationException e) {
			report = new HealthReportImpl(scuflModel.getDescription().getTitle(),"This workflow cannot be translated:"
					+ e.getMessage(), Status.SEVERE);
		}
		reportTreeModel.addHealthReport(report);
		return dataflow;

	}

	class HealthReportImpl implements HealthReport {
		private String message;
		private Status status;
		private String subject;

		public HealthReportImpl(String subject,String message, Status status) {
			super();
			this.message = message;
			this.status = status;
			this.subject = subject;
		}

		public String getMessage() {
			return message;
		}

		public Status getStatus() {
			return status;
		}

		public String getSubject() {
			return subject;
		}
		
		

	}

}
