/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowInstance;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.WriteQueueAspect;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerRegistry;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class DataflowRunsComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(DataflowRunsComponent.class);

	private static DataflowRunsComponent singletonInstance;

	private ReferenceService referenceService;

	private ReferenceService referenceServiceWithDatabase; // for previous runs, we always need the one using database

	private String referenceContext;

	private DefaultListModel runListModel;

	private JList runList;

	private JButton removeWorkflowRunsButton;

	private JSplitPane topPanel;
	
	// Queue for previous workflow runs to be deleted
	// The reason for not using LinkedBlockingQueue here is that we need to do a peek first and then remove
	// in the delete run thread, rather than taking the first element of the queue since
	// shutdown hook checks if the queue is empty and then pops up a dialog it is taking a while (and if we do
	// a remove immediately the hook would not detect that there is deletion going on if there was only one element 
	// in the queue). Peek in LinkedBlockingQueue does not wait so we would have to sync anyway so there is not 
	// advantage in using it over LinkedList. 
	private static final LinkedList<DataflowRun> runsToBeDeletedQueue = new LinkedList<DataflowRun>();

	private DataflowRunsComponent() {
		super(JSplitPane.VERTICAL_SPLIT);
		setDividerLocation(400);

		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topPanel.setDividerLocation(275);
		topPanel.setBorder(null);
		setTopComponent(topPanel);

		runListModel = new DefaultListModel();
		runList = new JList(runListModel);
		runList.setBorder(new EmptyBorder(5, 5, 5, 5));
		runList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// runList.setSelectedIndex(0);
		runList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = runList.getSelectedValue();

					int location = getDividerLocation();

					if (selection == null) { // there is no workflow items in
						// the list

						JPanel tempMonitorPanel = new JPanel(new BorderLayout());
						tempMonitorPanel.setBorder(LineBorder
								.createGrayLineBorder());
						tempMonitorPanel.setBackground(Color.WHITE);
						tempMonitorPanel.add(new JLabel(
								"No workflows runs available", JLabel.CENTER),
								BorderLayout.CENTER);
						topPanel.setBottomComponent(tempMonitorPanel);

						JPanel tempResultsPanel = new JPanel(new BorderLayout());
						tempResultsPanel.setBackground(Color.WHITE);
						tempResultsPanel.add(new JLabel("Results"),
								BorderLayout.NORTH);
						tempResultsPanel.add(new JLabel("No results available",
								JLabel.CENTER), BorderLayout.CENTER);
						setBottomComponent(tempResultsPanel);

						removeWorkflowRunsButton.setEnabled(false);
						setDividerLocation(location);
						revalidate();
					} else if (selection instanceof DataflowRun) {

						DataflowRun dataflowRun = (DataflowRun) selection;
						topPanel.setBottomComponent(dataflowRun
								.getOrCreateMonitorViewComponent());

						setBottomComponent(dataflowRun.getResultsComponent());
						setDividerLocation(location);
						removeWorkflowRunsButton.setEnabled(true);
						revalidate();
					}
				}
			}
		});

		JPanel runListPanel = new JPanel(new BorderLayout());
		runListPanel.setBorder(LineBorder.createGrayLineBorder());

		JLabel worklflowRunsLabel = new JLabel("Workflow Runs");
		worklflowRunsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		worklflowRunsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		// button to remove previous workflow runs
		removeWorkflowRunsButton = new JButton("Remove"); 
		removeWorkflowRunsButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		removeWorkflowRunsButton.setEnabled(false);
		removeWorkflowRunsButton.setToolTipText("Remove workflow run(s)");
		removeWorkflowRunsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					
				// Warn user that removing workflow run will 
				// cause all provenance data for that run to be deleted
				int option = JOptionPane
						.showConfirmDialog(
								null,
								new JLabel("<html><body>Are you sure you want to delete the selected workflow run(s)?<br>" +
										"Deleting them will remove all provenance data related to the run(s).</body></html>"),
								"Confirm workflow run deletion",
								JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.CANCEL_OPTION){
					return;
				}
				
				int[] selectedRunsToDelete = runList.getSelectedIndices();
				for (int i=0; i < selectedRunsToDelete.length; i++){
					if (((DataflowRun)runListModel.get(i)).getDataflow().isRunning()){
						option = JOptionPane
								.showConfirmDialog(
										null,
										new JLabel(
												"<html><body>Some of the workflow runs you are trying to delete appear not to have finished.<br>Are you sure you want to continue and delete them as well (this is not recommended)?</body></html>"),
										"Confirm unfinished workflow run deletion",
										JOptionPane.WARNING_MESSAGE);
						if (option == JOptionPane.CANCEL_OPTION){
							return;
						}
					}
				}

				int[] selected = runList.getSelectedIndices();
				for (int i = selected.length - 1; i >= 0; i--) {
					final DataflowRun dataflowRunToBeDeleted = (DataflowRun) runListModel
							.remove(selected[i]);
					MonitorViewComponent mvc = dataflowRunToBeDeleted.getMonitorViewComponent();
					if (mvc != null) {
						mvc.onDispose();
					}
					// Add this workflow run to the queue to be deleted
					synchronized (runsToBeDeletedQueue) {
						runsToBeDeletedQueue.add(dataflowRunToBeDeleted);
					}
				}
				if (selected.length >0){
					synchronized (runsToBeDeletedQueue) {
						runsToBeDeletedQueue.notify();
					}
				}
				// Set the first item as selected - if there is one
				if (runListModel.size() > 0) {
					runList.setSelectedIndex(0);
				}
				System.gc();
			}
		});
		JPanel runListTopPanel = new JPanel();
		runListTopPanel.setLayout(new BorderLayout());
		runListTopPanel.add(worklflowRunsLabel, BorderLayout.WEST);
		runListTopPanel.add(removeWorkflowRunsButton, BorderLayout.EAST);

		JPanel runListWithHintTopPanel = new JPanel();
		runListWithHintTopPanel.setLayout(new BorderLayout());
		runListWithHintTopPanel.add(runListTopPanel, BorderLayout.NORTH);

		JPanel hintsPanel = new JPanel();
		hintsPanel.setLayout(new BorderLayout());
		hintsPanel.add(new JLabel("Click on a run to see its values"),
				BorderLayout.NORTH);
		hintsPanel.add(new JLabel("Click on a service in the diagram"),
					BorderLayout.CENTER);
		hintsPanel.add(new JLabel("to see intermediate values (if available)"),
				BorderLayout.SOUTH);
		runListWithHintTopPanel.add(hintsPanel, BorderLayout.SOUTH);

		runListPanel.add(runListWithHintTopPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(runList);
		scrollPane.setBorder(null);
		runListPanel.add(scrollPane, BorderLayout.CENTER);

		topPanel.setTopComponent(runListPanel);

		JPanel tempMonitorPanel = new JPanel(new BorderLayout());
		tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
		tempMonitorPanel.setBackground(Color.WHITE);
		tempMonitorPanel.add(new JLabel("No workflow run selected",
				JLabel.CENTER), BorderLayout.CENTER);
		topPanel.setBottomComponent(tempMonitorPanel);

		JPanel tempResultsPanel = new JPanel(new BorderLayout());
		tempResultsPanel.setBackground(Color.WHITE);
		tempResultsPanel.add(new JLabel("Values"), BorderLayout.NORTH);
		tempResultsPanel.add(new JLabel("No values yet", JLabel.CENTER),
				BorderLayout.CENTER);
		setBottomComponent(tempResultsPanel);

	    // revalidate();
		// setDividerLocation(.3);

		Thread thread = new Thread("Retrieve Previous Runs") {
			@Override
			public void run() {
				// force reference service to be constructed now rather than at first
				// workflow run
				getReferenceService();
				getReferenceServiceWithDatabase(); // get the Reference Service with database for previous runs
				retrievePreviousRuns();				
			}
			
		};
		thread.start();
		
		// Start listening for requests for previous workflow runs to be deleted 
		Thread deleteWorkflowRunsThread = new DeleteWorkflowRunsThread();
		deleteWorkflowRunsThread.start();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<DataflowRun> getPreviousWFRuns(){
		return (ArrayList<DataflowRun>) Collections.list(runListModel.elements());
	}
	
	private void retrievePreviousRuns() {
		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);

		List<WorkflowInstance> allWorkflowRunIDs = provenanceAccess.listRuns(null, null);
		//List<WorkflowInstance> allWorkflowRunIDs = provenanceAccess.getAllWorkflowIDs();
		//Collections.reverse(allWorkflowRunIDs);
		
		for (WorkflowInstance workflowInstance : allWorkflowRunIDs) {
			if (provenanceAccess.isTopLevelDataflow(workflowInstance.getWorkflowIdentifier())){
				logger.info("retrieved previous run, workflow id: "
						+ workflowInstance.getInstanceID() + " date: "
						+ workflowInstance.getTimestamp());
				Timestamp time = Timestamp.valueOf(workflowInstance.getTimestamp());
				Date date = new Date(time.getTime());
				try {
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(new ByteArrayInputStream(workflowInstance.getDataflowBlob()));
					Element rootElement = document.getRootElement();
					Dataflow dataflow = XMLDeserializerRegistry.getInstance()
							.getDeserializer().deserializeDataflow(rootElement);
					DataflowRun runComponent = new DataflowRun(dataflow, date,
							workflowInstance.getInstanceID(), referenceServiceWithDatabase);
					runComponent.setDataSavedInDatabase(true);
					runComponent.setProvenanceEnabledForRun(true);
					runListModel.add(runListModel.getSize(), runComponent);
				} catch (JDOMException e) {
					logger.error("Problem with previous run: "
							+ workflowInstance.getInstanceID() + " " + e);
				} catch (IOException e) {
					logger.error("Problem with previous run: "
							+ workflowInstance.getInstanceID() + " " + e);
				} catch (DeserializationException e) {
					logger.error("Problem with previous run: "
							+ workflowInstance.getInstanceID() + " " + e);
				} catch (EditException e) {
					logger.error("Problem with previous run: "
							+ workflowInstance.getInstanceID() + " " + e);
				}				
			}
		}
	}

	public static DataflowRunsComponent getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DataflowRunsComponent();
		}
		return singletonInstance;
	}

	public long getRunListCount() {
		return runListModel.size();
	}

	public ReferenceService getReferenceService() {
		String context = DataManagementConfiguration.getInstance()
				.getDatabaseContext();
		if (!context.equals(referenceContext)) {
			referenceContext = context;
			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
					context);
			referenceService = (ReferenceService) appContext
					.getBean("t2reference.service.referenceService");
			try {
				WriteQueueAspect cache = (WriteQueueAspect) appContext
						.getBean("t2reference.cache.cacheAspect");
				ReferenceServiceShutdownHook.setReferenceServiceCache(cache);
			} catch (NoSuchBeanDefinitionException e) {
				// ReferenceServiceShutdown.setReferenceServiceCache(null);
			} catch (ClassCastException e) {
				// ReferenceServiceShutdown.setReferenceServiceCache(null);
			}
		}
		return referenceService;

	}
	
	private ReferenceService getReferenceServiceWithDatabase() {
		// Force creation of a Ref. Service that uses database regardless of what current context is
		// This Ref. Service will be used for previous wf runs to get intermediate results even if
		// current Ref. Manager uses in-memory store. 
		if (referenceServiceWithDatabase == null){
			String databasecontext = DataManagementConfiguration.HIBERNATE_CONTEXT;
			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
					databasecontext);
			referenceServiceWithDatabase = (ReferenceService) appContext
						.getBean("t2reference.service.referenceService");
		}
		return referenceServiceWithDatabase;
	}
	
	public void runDataflow(WorkflowInstanceFacade facade,
			Map<String, T2Reference> inputs) {
		DataflowRun runComponent = new DataflowRun(facade, inputs, new Date(), referenceService);
		runComponent.setProvenanceEnabledForRun(DataManagementConfiguration.getInstance().isProvenanceEnabled());
		runComponent.setDataSavedInDatabase(DataManagementConfiguration
				.getInstance().getProperty(
						DataManagementConfiguration.IN_MEMORY)
				.equalsIgnoreCase("false"));
		runListModel.add(0, runComponent);
		runList.setSelectedIndex(0);
		runComponent.run();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}
	
	public static LinkedList<DataflowRun> getRunsToBeDeletedQueue() {
		return runsToBeDeletedQueue;
	}

	/**
	 *  Thread that deletes provenance for previous workflow runs 
	 *  placed in a special queue.
	 */
	private class DeleteWorkflowRunsThread extends Thread {
		
		public DeleteWorkflowRunsThread() {
			super("Deleting old workflow runs");
			setDaemon(true);
		}
		
        public void run() {
			try {
				DataflowRun runToDelete = null;
				while (true) {
					synchronized (runsToBeDeletedQueue) {
						// Wait until an element is placed in the queue
						while (runsToBeDeletedQueue.isEmpty()) {
							runsToBeDeletedQueue.wait();
						}
					}
					// Retrieve the first element from the queue (but do not
					// remove it)
					runToDelete = runsToBeDeletedQueue.peek();

					// Remove provenance data for the run (if any) and all 
					// references held by the workflow run from the Reference Manager's store
					try {
						logger.info("Starting deletion of workflow run '"
								+ runToDelete.toString()
								+ "' (run id "
								+ runToDelete.getRunId()
								+ ").");
						if (runToDelete.isProvenanceEnabledForRun()){
							String connectorType = DataManagementConfiguration
									.getInstance().getConnectorType();
							ProvenanceAccess provenanceAccess = new ProvenanceAccess(
									connectorType);
							// Remove the run from provenance database (if it is stored there at all)
							provenanceAccess.removeRun(runToDelete.getRunId());
						}
						// Remove references from the Reference Manager's store (regardless if in-memory or database)
						runToDelete.getReferenceService().deleteReferencesForWorkflowRun(runToDelete.getRunId());
						logger.info("Deletion of workflow run '"
										+ runToDelete.toString()
										+ "' (run id "
										+ runToDelete.getRunId()
										+ ") from provenance database and Reference Manager's store completed.");
					} catch (Exception ex) {
						logger.error("Failed to delete workflow run '"
								+ runToDelete.toString() + "' (run id "
								+ runToDelete.getRunId()
								+ ") from provenance database and Reference Manager's store.", ex);
					} finally {
						synchronized (runsToBeDeletedQueue) {
							// Remove the run we have just deleted
							runsToBeDeletedQueue.removeFirst();
						}
					}

				}
			} catch (InterruptedException ignored) {

			}
        }
    }
}
