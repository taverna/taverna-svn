/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;

// Utility Imports
import java.util.Iterator;
import java.util.List;

// IO Imports
import java.io.StringReader;
import java.io.IOException;

// JDOM Imports
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.lang.StringBuffer;



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
				  "Last event",
				  "Event timestamp",
				  "Event detail"};

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
	    data[i][0] = org.embl.ebi.escience.scuflworkers.ProcessorHelper.getPreferredIcon(p);
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
		// Add colours using HTML labels
		String colour = "black";
		if (statusString.equals("ProcessComplete")) {
		    colour = "#1C7366";
		}
		else if (statusString.equals("ServiceError") ||
			 statusString.equals("ServiceFailure")) {
		    colour = "red";
		}
		else if (statusString.startsWith("Invoking")) {
		    colour = "purple";
		}
		else if (statusString.equals("ProcessScheduled")) {
		    colour = "#CE7220";
		}
		setValueAt("<html><font color=\""+colour+"\">"+statusString+"</font></html>", i, 2);
		return;
	    }
	}
    }
    
    /**
     * Set the start time string for a given processor
     */
    public void setEventTime(String processorName, String theString) {
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
    public void setEventDetail(String processorName, String theString) {
	for (int i = 0; i < rows; i++) {
	    if (((String)data[i][1]).equals(processorName)) {
		setValueAt(theString, i, 4);
		return;
	    }
	}
    }
    
    /**
     * Update the table with data from the progress report
     */
    public String update(String progressReport) 
	throws InvalidStatusReportException {
	Element processorList;
	String workflowID = null;
	String workflowStatus = null;
	Namespace documentNamespace = null;
	try {
	    SAXBuilder builder = new SAXBuilder(false);
	    Document document = builder.build(new StringReader(progressReport));
	    processorList = document.getRootElement().getChild("processorList");
	    workflowID = document.getRootElement().getAttributeValue("workflowID");
	    workflowStatus = document.getRootElement().getAttributeValue("workflowStatus");
	}
	catch (JDOMException jde) {
	    // Failure, probably not a valid progress report
	    throw new InvalidStatusReportException("Unable to handle the status report, error was : "+
						   jde.getMessage());
	}
	catch (IOException ioe) {
	    // Cannot create the document from the reader
	     throw new InvalidStatusReportException("Unable to handle the status report, error was : "+
						   ioe.getMessage());
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
	for (Iterator i = processorList.getChildren("processor").iterator(); i.hasNext();) {
	    Element processorElement = (Element)i.next();
	    String processorName = processorElement.getAttributeValue("name");
	    String processorStatus = "Unknown";
	    String eventTime = "--";
	    String eventDetail = "--";

	    // Get the first child of the processor element.
	    List childElementList = processorElement.getChildren();
	    if (childElementList.isEmpty()==false) {
		Element firstChildElement = (Element)childElementList.get(0);
		processorStatus = firstChildElement.getName();
		eventTime = firstChildElement.getAttributeValue("TimeStamp");
		StringBuffer eventDetailBuffer = new StringBuffer();
		for (Iterator j = firstChildElement.getAttributes().iterator(); j.hasNext();) {
		    Attribute a = (Attribute)j.next();
		    String attributeName = a.getName();
		    if (!attributeName.equalsIgnoreCase("TimeStamp")) {
			eventDetailBuffer.append(attributeName+"='"+a.getValue()+"' ");
		    }
		}
		eventDetail = eventDetailBuffer.toString();
	    }
	    setEventDetail(processorName, eventDetail);
	    setEventTime(processorName, eventTime);
	    setStatusString(processorName, processorStatus);
	}
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
