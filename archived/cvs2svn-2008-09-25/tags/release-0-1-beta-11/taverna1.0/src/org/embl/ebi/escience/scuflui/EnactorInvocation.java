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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scuflui.results.ResultTablePanel;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;


/**
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public class EnactorInvocation extends JPanel implements ScuflUIComponent {

    /**
     * A not particularly elegant way of setting the user context
     * from Kevin's MIR browser plugin. If this is set to non null
     * it will be passed through to the enactor instance that
     * this invocation component launches, and in turn through to
     * the event system (hopefully)
     */
    public static UserContext USERCONTEXT = null; 

    public void attachToModel(ScuflModel theModel) {
	//
    }

    public void detachFromModel() {
	// detachFromModel() is called by GeneridUIComponentFrame when
        // it is closing.  Cleanup of remote resources will be done here.
        try {
            workflowInstance.cancel();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
	return "Enactor invocation";
    }

    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.windowRun;
    }

    //private TavernaWorkflowEnactor theEnactor;
    private ScuflModel theModel;
    private String instanceID = null;
    private EnactorStatusTableModel statusTableModel = null;
    //private FlowReceipt flowReceipt = null;
    private WorkflowInstance workflowInstance = null;
    private JTextArea resultsText = null;
    private JTextArea provenanceText = null;
    private JPanel provenancePanel = null;
    private JTabbedPane individualResults = new JTabbedPane();
    private JPanel resultsPanel = null;
    private JTabbedPane tabs = null;
    private JButton saveResultsButton = null;

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
	return this.workflowInstance.getProgressReportXMLString();
    }

  /**
   * Ensure that the results have been retrieved. This is code factored out
   * from showResults().
   */
  protected void ensureGotResults()
  {
    // todo: surely we can re-write this to use synchronization rather than sleep?
    String results = "";
    try {
      System.out.println("Getting results...");
      boolean gotResults = false;
      while (!gotResults) {
	results = this.workflowInstance.getOutputXMLString();
        if (results.equals("") == false) {
          gotResults = true;
        } else {
          Thread.sleep(1000);
        }
      }
    } catch (InterruptedException ie) {
      // todo: ugly hack - I didn't want to change the logic just incase
      // but shouldn't the try be close on the sleep()?
      this.resultsText.setText("No results available : " + ie.toString());
    }
  }

  // todo: should this be public? We only call it here.
  /**
   * Show the results in the text area.
   */
  public void showResults() {
    ensureGotResults();

    this.tabs.add("Results",individualResults);
    saveResultsButton.setEnabled(true);
    // Get the output map and create new result detail panes
    Map resultMap = this.workflowInstance.getOutput();
    for (Iterator i = resultMap.keySet().iterator(); i.hasNext(); ) {
      String resultName = (String)i.next();
      DataThing resultValue = (DataThing)resultMap.get(resultName);
      this.individualResults.add(resultName, new ResultItemPanel(resultValue));
    }
    this.tabs.setSelectedComponent(individualResults);
  }

  // todo: this should be the same level of access as showResults()
  /**
   * Save the results to a directory. This will display a dialog box to find
   * the directory.
   */
  public void saveResults()
  {
    ensureGotResults();
    // Get the output map and create new result detail panes
    Map resultMap = this.workflowInstance.getOutput();
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = chooser.showSaveDialog(this);
    for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
      String resultName = (String) i.next();
      DataThing resultValue = (DataThing) resultMap.get(resultName);
      try {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File f = chooser.getSelectedFile();
          String name = resultName;
          resultValue.writeToFileSystem(f, name);
        }
      } catch (IOException ioe) {
        // todo: ugly hack - how are we meant to be handling errors?
        ioe.printStackTrace();
        //
      }
    }
  }

    /**
     * Show the current provenance of this invocation
     */
    public void showProvenance() {
	String provenance = "";
	try {
	    provenance = this.workflowInstance.getProvenanceXMLString();
	    //this.provenanceText.setFont(new Font("Monospaced",Font.PLAIN,12));
	    //this.provenanceText.setLineWrap(true);
	    //this.provenanceText.setWrapStyleWord(true);
	    //this.provenanceText.setText(provenance);
	    //this.tabs.add("Provenance Text", provenancePanel);
	    this.tabs.add("Provenance Tree", new JScrollPane(new XMLTree(provenance)));

	}
	catch (Exception ex) {
	    this.provenanceText.setText("No provenance available : "+ex.toString());
	}
    }

    public void showResultTable()
    {
	    this.tabs.add("Result Table", new JScrollPane(new ResultTablePanel(theModel, workflowInstance)));    	
    }
    
    /**
     * Show the detailed enactor progress report as a tree
     */
    public void showProgressReport() {
	String progressReport = "";
	try {
	    progressReport = this.workflowInstance.getProgressReportXMLString();
	    this.tabs.add("Process report", new JScrollPane(new XMLTree(progressReport)));
	}
	catch (Exception ex) {
	    //
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
     * Create a new enactor run panel using the new plugable enactor
     * proxy.
     * @throws WorkflowSubmissionException if the submission fails
     * for some reason.
     */
    public EnactorInvocation(EnactorProxy enactor,
			     ScuflModel model,
			     Map inputDataThings)
	throws WorkflowSubmissionException {
	super(new BorderLayout());
	setPreferredSize(new Dimension(100,100));
	this.theModel = model;
	/**
	 * TODO - get the user proxy object from somewhere!
	 */
	this.workflowInstance = enactor.compileWorkflow(model, inputDataThings, EnactorInvocation.USERCONTEXT);
	// Create a new toolbar for the save results option...
	JToolBar toolbar = new JToolBar("Invocation tools");
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	saveResultsButton = new JButton( "Save all results", ScuflIcons.saveIcon);
	toolbar.add(saveResultsButton);
	toolbar.add(Box.createHorizontalGlue());
	saveResultsButton.setEnabled(false);
	add(toolbar, BorderLayout.PAGE_START);
	saveResultsButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    EnactorInvocation.this.saveResults();
		}
	    });

    	// Create a tabbed pane for the status, results and provenance panels.
	tabs = new JTabbedPane();
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	add(tabs, BorderLayout.CENTER);
	final JPanel processorListPanel = new JPanel();
	processorListPanel.setLayout(new BoxLayout(processorListPanel, BoxLayout.PAGE_AXIS));
	processorListPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								      "Processor statii"));

	statusTableModel = new EnactorStatusTableModel(theModel);
	final JTable processorTable = new JTable(statusTableModel);
	processorTable.setGridColor(new Color(235,235,235));
	processorTable.setSelectionBackground(new Color(232,242,254));
	processorTable.setSelectionForeground(Color.BLACK);	
	processorTable.setIntercellSpacing(new Dimension(0, 1));
	processorTable.setShowVerticalLines(false);
	processorTable.getColumnModel().getColumn(0).setMaxWidth(30);
	processorTable.getColumnModel().getColumn(0).setResizable(false);	
	// Add a listener to the table to allow the display of intermediate results
	JTabbedPane intermediateResults = new JTabbedPane();
	final JTabbedPane intermediateOutputs = new JTabbedPane();
	final JTabbedPane intermediateInputs = new JTabbedPane();
	intermediateResults.add("Intermediate inputs", intermediateInputs);
	intermediateResults.add("Intermediate outputs", intermediateOutputs);
	processorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	//Ask to be notified of selection changes.
	ListSelectionModel rowSM = processorTable.getSelectionModel();
	rowSM.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    //Ignore extra messages.
		    if (e.getValueIsAdjusting()) return;
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		    if (lsm.isSelectionEmpty()) {
			//no rows are selected
		    }
		    else {
			int selectedRow = lsm.getMinSelectionIndex();
			// get the processor name
			try {
			    String processorName = (String)statusTableModel.getValueAt(selectedRow, 1);
			    Map[] intermediateResultMaps = EnactorInvocation.this.workflowInstance.getIntermediateResultsForProcessor(processorName);
			    // Clear the tabs
			    intermediateInputs.removeAll();
			    intermediateOutputs.removeAll();
			    // Do the inputs
			    for (Iterator i = intermediateResultMaps[0].keySet().iterator(); i.hasNext(); ) {
				String name = (String)i.next();
				DataThing value = (DataThing)intermediateResultMaps[0].get(name);
				intermediateInputs.add(name, new ResultItemPanel(value));
			    }
			    // And the outputs
			    for (Iterator i = intermediateResultMaps[1].keySet().iterator(); i.hasNext(); ) {
				String name = (String)i.next();
				DataThing value = (DataThing)intermediateResultMaps[1].get(name);
				intermediateOutputs.add(name, new ResultItemPanel(value));
			    }


			}
			catch (UnknownProcessorException upe) {
			    //
			}
		    }

		}
	    });




	//processorTable.setPreferredScrollableViewportSize(new Dimension(500,100));
	JScrollPane scrollPane = new JScrollPane(processorTable);
	scrollPane.setPreferredSize(new Dimension(500,200));
	JSplitPane statusSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
						    scrollPane,
						    intermediateResults);

	processorListPanel.add(statusSplitPane);
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

      // todo: why show() rather than setVisible(true)?
	//pack();
	//setSize(new Dimension(600,300));
	setVisible(true);
	//show();
	// Run the workflow and poll for status messages
        try {
            workflowInstance.run();
        }
        catch(InvalidInputException e) {
            WorkflowSubmissionException wse = new WorkflowSubmissionException(e.getMessage());     
            throw wse;
        }
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
		    // System.out.println("Status document : "+statusText);
		    String workflowStatus = theEnactorInvocation.getTableModel().update(statusText);
		    // System.out.println("Workflow status : "+workflowStatus);
		    if (workflowStatus.equals("FAILED") ||
			workflowStatus.equals("CANCELLED")) {
			theEnactorInvocation.showProvenance();
			theEnactorInvocation.showProgressReport();
			running = false;
			abort = true;
		    }
		    else if (workflowStatus.equals("COMPLETE")) {
			running = false;
			// Set the results display in the display panel
			theEnactorInvocation.showResults();
			theEnactorInvocation.showResultTable();			
			theEnactorInvocation.showProvenance();
			theEnactorInvocation.showProgressReport();
      // theEnactorInvocation.saveResults(); - commented out as it's anoying MRP
		    }
		}
		catch ( Exception e ) {
			e.printStackTrace();
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

