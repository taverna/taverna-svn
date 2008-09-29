/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.InvalidFlowBrokerRequestException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.WorkflowCommandException;
import uk.ac.soton.itinnovation.taverna.enactor.TavernaWorkflowEnactor;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaBinaryWorkflowSubmission;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;

// Utility Imports
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;




public class EnactorInvocation extends JPanel implements ScuflUIComponent {

    // A default instance of the workflow enactor to use if
    // the caller doesn't specify one to use
    private static TavernaWorkflowEnactor DEFAULT_ENACTOR;
    private static String DEFAULT_USER = "DEFAULT_USER";
    private static String DEFAULT_USER_CONTEXT = "DEFAULT_USER_CONTEXT";

    // Initialize the default enactor
    static {
        ResourceBundle rb = ResourceBundle.getBundle("mygrid");
        Properties sysProps = System.getProperties();
        Enumeration keys = rb.getKeys();
	while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) rb.getString(key);
	    sysProps.put(key, value);
        }
	DEFAULT_ENACTOR = new TavernaWorkflowEnactor();
    }
    
    public void attachToModel(ScuflModel theModel) {
	//
    }

    public void detachFromModel() {
	//
    }

    public String getName() {
	return "Enactor invocation";
    }

    private TavernaWorkflowEnactor theEnactor;
    private ScuflModel theModel;
    private TavernaBinaryWorkflowSubmission theSubmission; 
    private String instanceID = null;
    private EnactorStatusTableModel statusTableModel = null;
    private FlowReceipt flowReceipt = null;
    private JTextArea resultsText = null;
    private JTextArea provenanceText = null;
    private JPanel provenancePanel = null;
    private JTabbedPane individualResults = new JTabbedPane();
    private JPanel resultsPanel = null;
    private JTabbedPane tabs = null;

    /**
     * Get the workflow instance ID for this invocation
     */
    public String getInstanceID() {
	return this.instanceID;
    }

    /**
     * Get the status text for this invocation
     */
    public String getStatusText() {
	return ((TavernaFlowReceipt)(this.flowReceipt)).getProgressReportXMLString();
    }
    
    /**
     * Show the results in the text area
     */
    public void showResults() {
	String results = "";
	try {
	    System.out.println("Getting results...");
	    boolean gotResults = false;
	    while (!gotResults) {
		results = ((TavernaFlowReceipt)(this.flowReceipt)).getOutputString();
		if (results.equals("") == false) {
		    gotResults = true;
		}
		else {
		    Thread.sleep(1000);
		}
	    }
	    this.tabs.add("Results",individualResults);
	    this.tabs.add("Results as XML",resultsPanel);
	    this.resultsText.setText(results);
	    this.resultsText.setFont(new Font("Monospaced",Font.PLAIN,12));
	    this.resultsText.setLineWrap(true);
	    this.resultsText.setWrapStyleWord(true);
	    // Get the output map and create new result detail panes
	    Map resultMap = ((TavernaFlowReceipt)(this.flowReceipt)).getOutput();
	    for (Iterator i = resultMap.keySet().iterator(); i.hasNext(); ) {
		String resultName = (String)i.next();
		DataThing resultValue = (DataThing)resultMap.get(resultName);
		this.individualResults.add(resultName, new ResultItemPanel(resultValue));
	    }
	}
	catch (Exception ex) {
	    this.resultsText.setText("No results available : "+ex.toString());
	}
    }

    /**
     * Show the current provenance of this invocation
     */
    public void showProvenance() {
	String provenance = "";
	try {
	    provenance = ((TavernaFlowReceipt)(this.flowReceipt)).getProvenanceXMLString();
	    this.provenanceText.setFont(new Font("Monospaced",Font.PLAIN,12));
	    this.provenanceText.setLineWrap(true);
	    this.provenanceText.setWrapStyleWord(true);
	    this.provenanceText.setText(provenance);
	    this.tabs.add("Provenance Text", provenancePanel);
	    this.tabs.add("Provenance Tree", new JScrollPane(new XMLTree(provenance)));
	}
	catch (Exception ex) {
	    this.provenanceText.setText("No provenance available : "+ex.toString());
	}
    }

    /**
     * Get the table model that is being used by this
     * invocation panel to display the statii of the
     * workflow processors
     */
    public EnactorStatusTableModel getTableModel() {
	return this.statusTableModel;
    }

    /**
     * Create a new enactor run panel including creating the
     * status panel, invoking the workflow and all the rest.
     * @throws InvalidFlowBrokerRequestException if the taverna flow broker is not found
     * @throws WorkflowCommandException if the submission is invalid in some way
     */
    public EnactorInvocation(TavernaWorkflowEnactor enactor,
			     ScuflModel model,
			     Map inputDataThings,
			     String userID) 
	throws InvalidFlowBrokerRequestException, 
	       WorkflowCommandException {
	super(new BorderLayout());
	setPreferredSize(new Dimension(100,100));
	//super((JFrame)null,"Enactor invocation run", false);
	// Non modal dialog box
	
	// If the user didn't supply an enactor use the
	// default singleton in this class.
	if (enactor == null) {
	    this.theEnactor = DEFAULT_ENACTOR;
	}
	else {
	    this.theEnactor = enactor;
	}

	// If no user supplied use the default one
	if (userID == null) {
	    userID = DEFAULT_USER;
	}

	// Store a reference to the ScuflModel
	this.theModel = model;
	
	// Create a new submission object
	this.theSubmission = new TavernaBinaryWorkflowSubmission(this.theModel,
								 inputDataThings,
								 userID,
								 DEFAULT_USER_CONTEXT);
	System.out.println("Created the TavernaBinaryWorkflowSubmission : "+this.theSubmission.toString());
	
	// Invoke the enactor
	FlowBroker broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
	this.flowReceipt = (TavernaFlowReceipt) ((TavernaFlowBroker)broker).submitFlow(this.theModel,
								  inputDataThings,
								  userID,
								  DEFAULT_USER_CONTEXT);
	this.instanceID = this.flowReceipt.getID();
	
	// Create the UI
	// Create a tabbed pane for the status, results and provenance panels.
	tabs = new JTabbedPane();
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	add(tabs);
	final JPanel processorListPanel = new JPanel();
	processorListPanel.setLayout(new BoxLayout(processorListPanel, BoxLayout.PAGE_AXIS));
	processorListPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								      "Processor statii"));
	statusTableModel = new EnactorStatusTableModel(theModel);
	final JTable processorTable = new JTable(statusTableModel);
	//processorTable.setPreferredScrollableViewportSize(new Dimension(500,100));
	JScrollPane scrollPane = new JScrollPane(processorTable);
	scrollPane.setPreferredSize(new Dimension(500,200));
	processorListPanel.add(scrollPane);
	//processorListPanel.pack();
	//processorListPanel.setPreferredSize(new Dimension(500,150));
	tabs.add(processorListPanel,"Status");

	// Create a text area to show the results
	resultsPanel = new JPanel();
	resultsPanel.setLayout(new BorderLayout());
	resultsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								"Workflow results"));
	resultsText = new JTextArea();
	JScrollPane resultsScrollPane = new JScrollPane(resultsText);
	resultsScrollPane.setPreferredSize(new Dimension(100,100));
	resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);
	
	//tabs.add(resultsPanel,"Results");

	// Create a text area to show the provenance
	provenancePanel = new JPanel();
	provenancePanel.setLayout(new BorderLayout());
	provenancePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								   "Workflow provenance"));
	provenanceText = new JTextArea();
	JScrollPane provenanceScrollPane = new JScrollPane(provenanceText);
	provenanceScrollPane.setPreferredSize(new Dimension(100,100));
	provenancePanel.add(provenanceScrollPane, BorderLayout.CENTER);
	//tabs.add(provenancePanel,"Provenance");

	//individualResults = new JTabbedPane();
	//tabs.add(individualResults, "Detail");

	//pack();
	//setSize(new Dimension(600,300));
	//setVisible(true);
	show();
	// Run the workflow and poll for status messages
	EnactorInvocationStatusThread s = new EnactorInvocationStatusThread(this);

    }
}    

/**
 * Workflow run and poll
 */
class EnactorInvocationStatusThread extends Thread {
    
    boolean running = true;
    boolean abort = false;
    EnactorInvocation theEnactorInvocation;
    String instanceID = null;

    /**
     * Create the enactor run thread, passing it
     * the EnactorInvocation instance that created
     * it in the first place
     */
    public EnactorInvocationStatusThread(EnactorInvocation theEnactorInvocation) {
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
	}
	catch (InterruptedException ie) {
	    //
	}
	finally {
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
		// Get the status, update the model with it and check the overall
		// workflow status. If the workflow status is completed or aborted
		// then set running to false which will drop us neatly out of the
		// polling loop.
		System.out.println("Polling...");
		
		try {
		    String statusText = theEnactorInvocation.getStatusText();
		    //System.out.println("Status document : "+statusText);
		    String workflowStatus = theEnactorInvocation.getTableModel().update(statusText);
		    //System.out.println("Workflow status : "+workflowStatus);
		    if (workflowStatus.equals("FAILED") ||
			workflowStatus.equals("CANCELLED")) {
			theEnactorInvocation.showProvenance();
			running = false;
			abort = true;
		    }
		    else if (workflowStatus.equals("COMPLETE")) {
			running = false;
			// Set the results display in the display panel
			theEnactorInvocation.showResults();
			theEnactorInvocation.showProvenance();
		    }
		}
		catch ( Exception e ) {
		    // System.out.println(e.getMessage());
		    // Status message not available I guess
		}
		if (running) {
		    Thread.sleep(2000);
		}
	    }
	    catch (InterruptedException ie) {
		running = false;
	    }
	}
	// If we're here then either the enactor has finished or
	// the workflow has been aborted intentionally
	if (abort) {
	    // Do nothing
	}
	else {
	    // TODO - Show results
	}
    }    
}

