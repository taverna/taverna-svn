/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import org.embl.ebi.escience.scufl.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.taverna.enactor.TavernaWorkflowEnactor;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaBinaryWorkflowSubmission;

// Utility Imports
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import org.embl.ebi.escience.scuflui.ScuflIcons;
import java.lang.Class;
import java.lang.InterruptedException;
import java.lang.Object;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



public class EnactorInvocation extends JDialog {

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
    
    private TavernaWorkflowEnactor theEnactor;
    private ScuflModel theModel;
    private TavernaBinaryWorkflowSubmission theSubmission; 

    /**
     * Create a new enactor run panel including creating the
     * status panel, invoking the workflow and all the rest.
     */
    public EnactorInvocation(TavernaWorkflowEnactor enactor,
			     ScuflModel model,
			     Input inputData,
			     String userID) {
	super((JFrame)null,"Enactor invocation run", false);
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
								 inputData,
								 userID,
								 DEFAULT_USER_CONTEXT);
	System.out.println("Created the TavernaBinaryWorkflowSubmission : "+this.theSubmission.toString());
	
	// Create the UI
	getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
	final JPanel processorListPanel = new JPanel();
	processorListPanel.setLayout(new BoxLayout(processorListPanel, BoxLayout.PAGE_AXIS));
	processorListPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								      "Processor statii"));
	final JTable processorTable = new JTable(new EnactorStatusTableModel(theModel));
	//processorTable.setPreferredScrollableViewportSize(new Dimension(500,100));
	JScrollPane scrollPane = new JScrollPane(processorTable);
	scrollPane.setPreferredSize(new Dimension(500,150));
	processorListPanel.add(scrollPane);
	//processorListPanel.pack();
	processorListPanel.setPreferredSize(new Dimension(500,150));
	getContentPane().add(processorListPanel);
	pack();
	setSize(new Dimension(600,300));
	setVisible(true);
	
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

    /**
     * Create the enactor run thread, passing it
     * the EnactorInvocation instance that created
     * it in the first place
     */
    public EnactorInvocationStatusThread(EnactorInvocation theEnactorInvocation) {
	super();
	this.theEnactorInvocation = theEnactorInvocation;
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
		Thread.sleep(2000);
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


/** 
 * A Swing table model implementation that can be constructed from a
 * ScuflModel instance and updated by the XML coming back from the status
 * reports from the enactor
 */
class EnactorStatusTableModel extends AbstractTableModel {
    
    private ScuflModel scuflModel;
    private int rows = 0;
    private Object[][] data;
    final String[] columnNames = {"Type",
				  "Name",
				  "Status",
				  "Start Time",
				  "End Time"};

    public EnactorStatusTableModel(ScuflModel scufl) {
	this.scuflModel = scufl;
	// One row for each processor.
	Processor[] processors = scuflModel.getProcessors();
	rows = processors.length;
	data = new Object[rows][columnNames.length];
	// Put appropriate content in the rows
	for (int i = 0; i < rows; i++) {
	    Processor p = processors[i];
	    // do the icon
	    if (p instanceof WorkflowProcessor) {
		data[i][0] = ScuflIcons.workflowIcon;
	    }
	    else if (p instanceof WSDLBasedProcessor) {
		data[i][0] = ScuflIcons.wsdlIcon;
	    }
	    else if (p instanceof TalismanProcessor) {
		data[i][0] = ScuflIcons.talismanIcon;
	    }
	    else if (p instanceof SoaplabProcessor) {
		data[i][0] = ScuflIcons.soaplabIcon;
	    }
	    // do the name
	    data[i][1] = p.getName();
	    // do status
	    data[i][2] = "No data";
	    // do start time
	    data[i][3] = "--";
	    // do end time
	    data[i][4] = "--";
	}
    }
    
    /**
     * Set the status string for a given processor
     */
    public void setStatusString(String processorName, String statusString) {
	for (int i = 0; i < rows; i++) {
	    if (((String)data[i][1]).equals(processorName)) {
		setValueAt(statusString, i, 2);
		return;
	    }
	}
    }
    
    public void setStartTime(String processorName, String theString) {
	for (int i = 0; i < rows; i++) {
	    if (((String)data[i][1]).equals(processorName)) {
		setValueAt(theString, i, 3);
		return;
	    }
	}
    }

    public void setEndTime(String processorName, String theString) {
	for (int i = 0; i < rows; i++) {
	    if (((String)data[i][1]).equals(processorName)) {
		setValueAt(theString, i, 4);
		return;
	    }
	}
    }


    public Class getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }

    public int getColumnCount() {
	return columnNames.length;
    }

    public String getColumnName(int col) {
	return columnNames[col];
    }

    public int getRowCount() {
	return this.rows;
    }
    
    public Object getValueAt(int row, int column) {
	return data[row][column];
    }

    public boolean isCellEditable(int row, int col) {
	return false;
    }
    
    /**
     * Set the value and fire the events appropriately
     */
    public void setValueAt(Object value, int row, int col) {
	data[row][col] = value;
	fireTableCellUpdated(row, col);
    }

}
