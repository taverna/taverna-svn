/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Iterator;
import java.util.List;

// IO Imports
import java.io.StringReader;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import org.embl.ebi.escience.scuflui.ScuflIcons;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;



/** 
 * A Swing table model implementation that can be constructed from a
 * ScuflModel instance and updated by the XML coming back from the status
 * reports from the enactor.
 * @author Tom Oinn
 */
public class EnactorStatusTableModel extends AbstractTableModel {
    
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
    
    /**
     * Set the start time string for a given processor
     */
    public void setStartTime(String processorName, String theString) {
	for (int i = 0; i < rows; i++) {
	    if (((String)data[i][1]).equals(processorName)) {
		setValueAt(theString, i, 3);
		return;
	    }
	}
    }

    /**
     * Set the end time string for a given processor
     */
    public void setEndTime(String processorName, String theString) {
	for (int i = 0; i < rows; i++) {
	    if (((String)data[i][1]).equals(processorName)) {
		setValueAt(theString, i, 4);
		return;
	    }
	}
    }
    
    /**
     * Given a string containing an XML status report, update the
     * model to reflect the current statii and start / end times
     * of the processors in the model. The progress report should
     * conform to the schema in docs/enactor-progress-report.xsd
     * The returned string is the workflowStatus string, this could
     * be used to determine whether to stop polling for status, for 
     * example.
     */
    public String update(String progressReport) 
	throws InvalidStatusReportException {
	// Construct an XML document from the progress report
	Element processorList;
	String workflowID = null;
	String workflowStatus = null;
	Namespace documentNamespace = null;
	try {
	    SAXBuilder builder = new SAXBuilder(false);
	    Document document = builder.build(new StringReader(progressReport));
	    documentNamespace = document.getRootElement().getNamespace();
	    processorList = document.getRootElement().getChild("processorList", documentNamespace);
	    workflowID = document.getRootElement().getAttributeValue("workflowID");
	    workflowStatus = document.getRootElement().getAttributeValue("workflowStatus");
	}
	catch (JDOMException jde) {
	    // Failure, probably not a valid progress report
	    throw new InvalidStatusReportException("Unable to handle the status report, error was : "+
						   jde.getMessage());
	}
	if (workflowID == null) {
	    throw new InvalidStatusReportException("Workflow ID was null, exiting.");
	}
	if (workflowStatus == null) {
	    throw new InvalidStatusReportException("Workflow status was null, exiting.");
	}
	if (processorList == null) {
	    throw new InvalidStatusReportException("Workflow progress report didn't contain a processorList");
	}
	// Iterate over the children of the processorList, 
	// which should be one processor node for each processor
	// in the model to update.
	List processors = processorList.getChildren("processor", documentNamespace);
	for (Iterator i = processors.iterator(); i.hasNext(); ) {
	    
	    // Get the processor element, read the name and status
	    // and update the TableModel with the data appropriately
	    Element processorElement = (Element)i.next();
	    String processorName = processorElement.getAttributeValue("name");
	    String processorStatus = processorElement.getAttributeValue("status");
	    setStatusString(processorName, processorStatus);
	    
	    // Optional start and end times if present
	    // if they are absent, don't set the values.
	    String startTime = processorElement.getAttributeValue("startTime");
	    if (startTime != null) {
		setStartTime(processorName, startTime);
	    }
	    String endTime = processorElement.getAttributeValue("endTime");
	    if (endTime != null) {
		setEndTime(processorName, endTime);
	    }

	}
	
	// Return the overall workflow status
	return workflowStatus;
    }


    public Class getColumnClass(int c) {
	if (c == 0) {
	    return ImageIcon.class;
	}
	else {
	    return java.lang.String.class;
	}
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

/**
 * An exception to represent an invalid status report
 */
class InvalidStatusReportException extends Exception {
    
    public InvalidStatusReportException(String message) {
	super(message);
    }

}
