/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scuflui.graph.WorkflowEditor;
import org.embl.ebi.escience.scuflui.results.ResultMapSaveRegistry;
import org.embl.ebi.escience.scuflui.results.ResultTablePanel;
import org.embl.ebi.escience.scuflui.shared.XMLTree;
import org.embl.ebi.escience.scuflui.spi.ResultMapSaveSPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * @author Tom Oinn
 * @author Matthew Pocock
 * @author Stian Soiland
 */
public class EnactorInvocation extends JPanel implements UIComponentSPI {

	private static Logger logger = Logger.getLogger(EnactorInvocation.class);

	/**
	 * A not particularly elegant way of setting the user context from Kevin's
	 * MIR browser plugin. If this is set to non null it will be passed through
	 * to the enactor instance that this invocation component launches, and in
	 * turn through to the event system (hopefully)
	 */
	public static UserContext USERCONTEXT = null;

	boolean workflowStatusUpdateReady = false;

	public void onDisplay() {
		//
	}

	public void onDispose() {
		try {
			workflowEditor.detachFromModel();			
			workflowInstance.cancelExecution();
			// FIXME: Is this the right place to destroy? What about
			// other people attached to the workflow instance? 
			// (We must destroy() it somewhere, otherwise it will float
			// around and reference among other things our possibly large
			// input data)
			workflowInstance.destroy();
			// And remove our reference to it		
			workflowInstance = null;
			theModel = null;
		} catch (Exception e) {
			logger.error("Could not detach EnactorInvocation", e);
		}
	}

	public String getName() {
		return "Enactor invocation";
	}

	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.windowRun;
	}

	// private TavernaWorkflowEnactor theEnactor;
	private ScuflModel theModel;

	private String instanceID = null;

	private EnactorStatusTableModel statusTableModel = null;

	WorkflowEditor workflowEditor = null;

	// private FlowReceipt flowReceipt = null;
	private WorkflowInstance workflowInstance = null;

	private JTextArea resultsText = null;

	private JTextArea provenanceText = null;

	private JPanel provenancePanel = null;

	private JTabbedPane individualResults = new JTabbedPane();

	private JPanel resultsPanel = null;

	private JTabbedPane tabs = null;

	private JToolBar toolbar = null;

	/**
	 * Get the workflow instance ID for this invocation
	 */
	public String getInstanceID() {
		return instanceID;
	}

	public WorkflowInstance getWorkflowInstance() {
		return workflowInstance;
	}
	
	// The workflow status label
	JLabel flowLabel = null;

	/**
	 * Returns the processor that user pointed to obtain a breakpoint
	 */
	private Processor getPointedProcessor(int X, int Y, JTable table) {
		final Processor[] processors = theModel.getProcessors();
		for (int i = 0; i < processors.length; i++)
			if (table.getCellRect(i, 5, true).contains(X, Y))
				return processors[i];
		return null;
	}

	/**
	 * Get the status text for this invocation
	 */
	public String getStatusText() {
		return workflowInstance.getProgressReportXMLString();
	}

	/**
	 * Ensure that the results have been retrieved. This is code factored out
	 * from showResults().
	 */
	protected void ensureGotResults() {
		try {
			logger.debug("Getting results");
			while (true) {
				if (workflowInstance.getStatus().equalsIgnoreCase(
						"Complete")) {
					break;
				}
				// logger.debug(workflowInstance.getStatus());
				// results = workflowInstance.getOutputXMLString();
				// if (results.equals("") == false) {
				// break;
				Thread.sleep(1000);
			}
		} catch (InterruptedException ie) {
			// todo: ugly hack - I didn't want to change the logic just incase
			// but shouldn't the try be close on the sleep()?
			resultsText.setText("No results available : " + ie.toString());
		}
	}

	// todo: should this be public? We only call it here.
	/**
	 * Show the results in the text area.
	 */
	public void showResults() {
		ensureGotResults();
		toolbar.removeAll();
		// Show the results
		tabs.add("Results", individualResults);
		// Populate the toolbar with all the buttons from
		// the ResultMapSaveSPI
		Map resultMap = workflowInstance.getOutput();
		ResultMapSaveSPI[] savePlugins = ResultMapSaveRegistry.plugins();
		for (int i = 0; i < savePlugins.length; i++) {
			JButton saveAction = new JButton(savePlugins[i].getName(),
					savePlugins[i].getIcon());
			saveAction.addActionListener(savePlugins[i].getListener(resultMap,
					this));
			toolbar.add(saveAction);
			if (i < savePlugins.length) {
				toolbar.addSeparator();
			}
		}
		toolbar.add(Box.createHorizontalGlue());

		// saveResultsButton.setEnabled(true);
		// Get the output map and create new result detail panes
		for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
			String resultName = (String) i.next();
			DataThing resultValue = (DataThing) resultMap.get(resultName);
			ResultItemPanel rip = new ResultItemPanel(resultValue,
					workflowInstance);
			individualResults.add(resultName, rip);
		}
		tabs.setSelectedComponent(individualResults);
	}

	public void showResultTable() {
		if (MyGridConfiguration.getProperty("taverna.resulttable.enable") == null) {
			return;
		}
		try {
			int sizeLimit = 128000;
			try {
				sizeLimit = Integer.parseInt(
						MyGridConfiguration.getProperty("taverna.resulttable.sizelimit"));
			} catch (NumberFormatException ex) {
				logger.error("Could not set taverna.resulttable.sizelimit", ex);
			}
			if (workflowInstance.getProvenanceXMLString().length() < sizeLimit) {
				tabs.add("Result Table", new JScrollPane(
						new ResultTablePanel(theModel, workflowInstance)));
			}
		} catch (RuntimeException e) {
			// The above can cause a NPE, we need to track this down
			// FIXME
			logger.error("Could not show results", e);
		}
	}

	/**
	 * Show the detailed enactor progress report as a tree
	 */
	public void showProgressReport() {
		String progressReport = "";
		try {
			progressReport = workflowInstance.getProgressReportXMLString();
			// JEditTextArea display = new JEditTextArea(new
			// TextAreaDefaults());
			// display.setText(progressReport);
			// display.setTokenMarker(new XMLTokenMarker());
			// display.setEditable(false);
			// display.setPreferredSize(new Dimension(0,0));
			// tabs.add("Process report", display);
			tabs.add("Process report", new JScrollPane(new XMLTree(
					progressReport, false)));
		} catch (Exception ex) {
			//
		}
	}

	/**
	 * Remove the detailed enactor progress report as a tree
	 */
	public void rmvProgressReport() {
		try {
			tabs.remove(tabs.indexOfTab("Process report"));
		} catch (Exception ex) {
		}
	}

	/**
	 * Get the table model that is being used by this invocation panel to
	 * display the statii of the workflow processors
	 */
	public EnactorStatusTableModel getTableModel() {
		return statusTableModel;
	}

	/**
	 * Create a new enactor run panel using the new plugable enactor proxy.
	 * 
	 * @throws WorkflowSubmissionException
	 *             if the submission fails for some reason.
	 */
	public EnactorInvocation(EnactorProxy enactor, ScuflModel model,
			Map inputDataThings) throws WorkflowSubmissionException {
		this(enactor.compileWorkflow(model, inputDataThings,
				EnactorInvocation.USERCONTEXT));
	}

	/**
	 * Create new enactor panel from an existing workflow instance
	 */
	public EnactorInvocation(WorkflowInstance instance)
			throws WorkflowSubmissionException {
		super(new BorderLayout());
		workflowInstance = instance;
		try {
			theModel = instance.getWorkflowModel().clone();
		} catch (CloneNotSupportedException ce) {
			logger.error("Could not clone workflow model", ce);			
			WorkflowSubmissionException wfex = new WorkflowSubmissionException();
			wfex.initCause(ce);			
			throw wfex;
		}
		setPreferredSize(new Dimension(100, 100));

		// Create a new toolbar for the save results option...
		toolbar = new JToolBar("Invocation tools");
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));

		// saveResultsButton = new JButton( "Save all results",
		// ScuflIcons.saveIcon);
		// toolbar.add(saveResultsButton);
		// toolbar.add(Box.createHorizontalGlue());
		// saveResultsButton.setEnabled(false);
		add(toolbar, BorderLayout.PAGE_START);
		// saveResultsButton.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// EnactorInvocation.this.saveResults();
		// }
		// });

		// Create a tabbed pane for the status, results and provenance panels.
		tabs = new JTabbedPane();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(tabs, BorderLayout.CENTER);
		final JPanel processorListPanel = new JPanel();
		processorListPanel.setLayout(new BoxLayout(processorListPanel,
				BoxLayout.PAGE_AXIS));
		processorListPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Processor statii"));

		statusTableModel = new EnactorStatusTableModel(theModel);
		final JTable processorTable = new JTable(statusTableModel);
		processorTable.setGridColor(new Color(235, 235, 235));
		processorTable.setSelectionBackground(new Color(232, 242, 254));
		processorTable.setSelectionForeground(Color.BLACK);
		processorTable.setIntercellSpacing(new Dimension(0, 1));
		processorTable.setShowVerticalLines(false);
		processorTable.getColumnModel().getColumn(0).setMaxWidth(30);
		processorTable.getColumnModel().getColumn(0).setResizable(false);
		processorTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				final Processor theProcessor = getPointedProcessor(e.getX(), e
						.getY(), processorTable);
				if (theProcessor != null) {
					if (!theProcessor.hasBreakpoint()) {
						theProcessor.addBreakpoint();
						try {
							statusTableModel.update(getStatusText());
						} catch (InvalidStatusReportException isre) {
						}
						processorTable.repaint();
						workflowInstance.pause(theProcessor.getName());
					} else {
						theProcessor.rmvBreakpoint();
						try {
							statusTableModel.update(getStatusText());
						} catch (InvalidStatusReportException isre) {
						}
						processorTable.repaint();
						workflowInstance.resume(theProcessor.getName());
					}
				}
			}

		});

		// Create a computation steering contorl area
		final JButton playButton = new JButton("Resume", TavernaIcons.playIcon);
		final JButton pauseButton = new JButton("Pause", TavernaIcons.pauseIcon);
		final JButton stopButton = new JButton("Stop", TavernaIcons.stopIcon);
		//final JLabel taskLabel = new JLabel("Processor");
		flowLabel = new JLabel("<html><em>New</em></html>");
		// Eugh. Ugly. Will use HTML labels instead. tmo
		// flowLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD,
		// 16));
		//final JButton breakButton = new JButton("Add Breakpoint",
		//		TavernaIcons.breakIcon);
		//final JButton rbreakButton = new JButton("Resume",
		//		TavernaIcons.rbreakIcon);

		playButton.setVisible(false);
		playButton.setSize(70, 30);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					workflowInstance.resumeExecution();
					playButton.setVisible(false);
					pauseButton.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		pauseButton.setSize(70, 30);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					workflowInstance.pauseExecution();
					pauseButton.setVisible(false);
					playButton.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		stopButton.setSize(70, 30);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					workflowInstance.cancelExecution();
					// pauseButton.setEnabled(false);
					// playButton.setEnabled(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// toolbar.add(new JLabel(" "));
		toolbar.add(new JLabel("Workflow Status : "));
		// toolbar.add(new JLabel(" "));
		toolbar.add(flowLabel);
		toolbar.add(Box.createHorizontalGlue());
		toolbar.addSeparator();
		toolbar.add(playButton);
		toolbar.add(pauseButton);
		toolbar.addSeparator();
		toolbar.add(stopButton);

		// Add a listener to the table to allow the display of intermediate
		// results
		JTabbedPane intermediateResults = new JTabbedPane();
		final JTabbedPane intermediateOutputs = new JTabbedPane();
		final JTabbedPane intermediateInputs = new JTabbedPane();
		workflowEditor = new WorkflowEditor();
		final ScuflModel workflowModel = theModel;
		new Thread() {
			public void run() {
				workflowEditor.getScuflGraphModel().setShowBoring(false);
				workflowEditor.attachToModel(workflowModel);
				workflowEditor.updateStatus(getStatusText());
				workflowEditor.setEnabled(false);
				workflowEditor.setEditable(false);
				workflowStatusUpdateReady = true;
			}
		}.start();
		// workflowEditor.updateStatus(getStatusText());
		// workflowEditor.setEnabled(false);
		intermediateResults.add("Graph", new JScrollPane(workflowEditor));
		intermediateResults.add("Intermediate inputs", intermediateInputs);
		intermediateResults.add("Intermediate outputs", intermediateOutputs);
		processorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Ask to be notified of selection changes.
		ListSelectionModel rowSM = processorTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// no rows are selected
				} else {
					int selectedRow = lsm.getMinSelectionIndex();
					// get the processor name
					String processorName = (String) statusTableModel
							.getValueAt(selectedRow, 1);
					Map[] intermediateResultMaps;
					try {
						intermediateResultMaps = workflowInstance
								.getIntermediateResultsForProcessor(processorName);
					} catch (UnknownProcessorException ex) {
						logger.error("Unknown processor " + processorName, ex);
						return;
					}
					// Clear the tabs
					intermediateInputs.removeAll();
					intermediateOutputs.removeAll();
					// Do the inputs
					for (Iterator i = intermediateResultMaps[0].keySet()
							.iterator(); i.hasNext();) {
						String name = (String) i.next();
						DataThing value = (DataThing) intermediateResultMaps[0]
								.get(name);
						ResultItemPanel rip = new ResultItemPanel(value,
								workflowInstance);
						intermediateInputs.add(name, rip);
					}
					// And the outputs
					for (Iterator i = intermediateResultMaps[1].keySet()
							.iterator(); i.hasNext();) {
						String name = (String) i.next();
						DataThing value = (DataThing) intermediateResultMaps[1]
								.get(name);
						ResultItemPanel rip = new ResultItemPanel(value,
								workflowInstance);
						rip.setSelectedPort(processorName, name);
						intermediateOutputs.add(name, rip);
					}

				}

			}
		});

		JScrollPane scrollPane = new JScrollPane(processorTable);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		intermediateResults.setPreferredSize(new Dimension(0, 0));
		JSplitPane statusSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				scrollPane, intermediateResults);

		processorListPanel.add(statusSplitPane);
		// processorListPanel.pack();
		// processorListPanel.setPreferredSize(new Dimension(500,150));
		tabs.add(processorListPanel, "Status");

		// Create a text area to show the results
		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BorderLayout());
		resultsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Workflow results"));
		resultsText = new JTextArea();
		JScrollPane resultsScrollPane = new JScrollPane(resultsText);
		resultsScrollPane.setPreferredSize(new Dimension(100, 100));
		resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);

		// tabs.add(resultsPanel,"Results");

		// Create a text area to show the provenance
		provenancePanel = new JPanel();
		provenancePanel.setLayout(new BorderLayout());
		provenancePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Workflow provenance"));
		provenanceText = new JTextArea();
		JScrollPane provenanceScrollPane = new JScrollPane(provenanceText);
		provenanceScrollPane.setPreferredSize(new Dimension(100, 100));
		provenancePanel.add(provenanceScrollPane, BorderLayout.CENTER);
		// tabs.add(provenancePanel,"Provenance");

		// individualResults = new JTabbedPane();
		// tabs.add(individualResults, "Detail");

		// todo: why show() rather than setVisible(true)?
		// pack();
		// setSize(new Dimension(600,300));
		setVisible(true);
		// show();
		// Run the workflow and poll for status messages
		try {
			workflowInstance.run();
		} catch (InvalidInputException e) {
			WorkflowSubmissionException wse = new WorkflowSubmissionException(e
					.getMessage());
			throw wse;
		}
		new EnactorInvocationStatusThread(this);
	}

	/**
	 * Workflow run and poll
	 */
	class EnactorInvocationStatusThread extends Thread {

		boolean running = true;

		boolean paused = true;

		boolean abort = false;

		EnactorInvocation theEnactorInvocation;

		String instanceID = null;

		/**
		 * Create the enactor run thread, passing it the EnactorInvocation
		 * instance that created it in the first place
		 */
		public EnactorInvocationStatusThread(
				EnactorInvocation theEnactorInvocation) {
			super();
			this.theEnactorInvocation = theEnactorInvocation;
			this.instanceID = theEnactorInvocation.getInstanceID();
			this.start();
		}

		/**
		 * Polite request for the thread to stop
		 */
		public void stopPlease() {
			running = false;
			abort = true;
			// Wait for the enactor invocation thread to terminate
			try {
				this.join();
			} catch (InterruptedException ie) {
				//
			} finally {
				// TODO - Tidy up this enactor run
			}
		}

		public void run() {
			// TODO - Run the workflow
			while (running) {
				// TODO - Poll and update the status display
				// fixed the tight loop that was hanging the display when
				// this method was called. Of course, this doesn't remove
				// the need to actually implement this properly.
				try {
					// Get the status, update the model with it and check the
					// overall
					// workflow status. If the workflow status is completed or
					// aborted
					// then set running to false which will drop us neatly out
					// of the
					// polling loop.
					// logger.debug("Polling...");

					try {
						String statusText = theEnactorInvocation
								.getStatusText();
						// logger.debug("Status document : "+statusText);
						String workflowStatus;
						try {
							workflowStatus = theEnactorInvocation
									.getTableModel().update(statusText);
						} catch (InvalidStatusReportException e) {
							logger.error("Could not get workflow status", e);
							continue; // loop and try again
						}
						if (workflowStatusUpdateReady) {
							theEnactorInvocation.workflowEditor
									.updateStatus(statusText);
						}
						// logger.debug("Workflow status :
						// "+workflowStatus);
						if (workflowStatus.equals("CANCELLED")) {
							theEnactorInvocation.rmvProgressReport();
							theEnactorInvocation.showProgressReport();
							running = false;
							abort = true;
							theEnactorInvocation.flowLabel
									.setText("<html><font color=\"red\">Cancelled</font></html>");
						} else if (workflowStatus.equals("COMPLETE")) {
							running = false;
							// Set the results display in the display panel
							theEnactorInvocation.showResults();
							theEnactorInvocation.showResultTable();
							theEnactorInvocation.rmvProgressReport();
							theEnactorInvocation.showProgressReport();
							// theEnactorInvocation.saveResults(); - commented
							// out as it's anoying MRP
							theEnactorInvocation.flowLabel
									.setText("<html><font color=\"blue\">Complete</font></html>");
						} else if (workflowStatus.equals("PAUSED") && !paused) {
							paused = true;
							theEnactorInvocation.showProgressReport();
							theEnactorInvocation.flowLabel
									.setText("<html><font color=\"purple\">Paused</font></html>");
						} else if (workflowStatus.equals("FAILED")) {
							theEnactorInvocation.rmvProgressReport();
							theEnactorInvocation.showProgressReport();
							running = false;
							abort = true;
							theEnactorInvocation.flowLabel
									.setText("<html><font color=\"red\">Failed</font></html>");
						} else if (workflowStatus.equals("RUNNING")) {
							paused = false;
							theEnactorInvocation.rmvProgressReport();
							theEnactorInvocation.flowLabel
									.setText("<html><font color=\"green\">Running</font></html>");
						}
					} catch (Exception e) {
						logger.error("Error while invoking", e);
					}
					if (running) {
						Thread.sleep(2000);
					}
				} catch (InterruptedException ie) {
					running = false;
				}
			}
			// If we're here then either the enactor has finished or
			// the workflow has been aborted intentionally
			if (abort) {
				// Do nothing
			} else {
				// TODO - Show results
			}
		}
	}

}
