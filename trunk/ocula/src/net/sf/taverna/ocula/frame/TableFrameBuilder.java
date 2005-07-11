/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.frame;

import net.sf.taverna.ocula.ui.*;
import net.sf.taverna.ocula.Ocula;
import org.apache.log4j.Logger;
import javax.swing.*;
import java.util.*;
import org.jdom.Element;
import bsh.*;
import java.awt.BorderLayout;
import javax.swing.table.*;

/**
 * Handles the &lt;table&gt; element and builds a TableModel corresponding
 * to the specified columns in the objects resulting from the script.
 * @author Tom Oinn
 */
public class TableFrameBuilder implements FrameSPI {
    
    private static Logger log = Logger.getLogger(TableFrameBuilder.class);
    
    public String getElementName() {
	return "table";
    }
    
    public OculaFrame makeFrame(Ocula o, Element element) {
	String name = element.getAttributeValue("name");
	if (name == null) {
	    name = "No Name";
	}
	String iconName = element.getAttributeValue("icon");
	if (iconName == null) {
	    iconName = "NoIcon";
	}
	Element scriptElement = element.getChild("script");
	String script;
	if (scriptElement == null) {
	    // Will fail at eval time but that's fine, that's
	    // a checked exception and easier to deal with
	    script = "";
	}
	else {
	    script = scriptElement.getTextTrim();
	}
	// Get the column scripts
	List columnScripts = new ArrayList();
	List columnNames = new ArrayList();
	for (Iterator i = element.getChildren("column").iterator(); i.hasNext();) {
	    Element colDef = (Element)i.next();
	    columnNames.add(colDef.getAttributeValue("name","NoName"));
	    columnScripts.add(colDef.getTextTrim());
	}
	TableFrame rsp = new TableFrame(name, Icons.getIcon(iconName));
	rsp.getContents().setLayout(new BorderLayout());
	JTable table = new JTable(new ScriptTableModel(o, rsp, script, (String[])columnScripts.toArray(new String[0]), 
						       (String[])columnNames.toArray(new String[0])));
	rsp.getContents().add(table, BorderLayout.CENTER);
	rsp.getContents().add(table.getTableHeader(), BorderLayout.PAGE_START);
	return rsp;
    }

    class TableFrame extends ResultSetPanel implements OculaFrame {
	public TableFrame(String name, Icon icon) {
	    super(name, icon);
	}
    }
     
    class ScriptTableModel extends AbstractTableModel {
	
	int rows = 0;
	String[] colScripts, colNames;
	String script;
	Ocula ocula;
	List[] tableData;
	ResultSetPanel panel;
	
	public ScriptTableModel(Ocula o, ResultSetPanel panel, String script, String[] colScripts, String[] colNames) {
	    //super();
	    this.ocula = o;
	    this.script = script;
	    this.colScripts = colScripts;
	    this.colNames = colNames;
	    this.panel = panel;
	    tableData = new List[colNames.length];
	    for (int i = 0; i < colNames.length; i++) {
		tableData[i] = new ArrayList();
	    }
	    new Thread() {
		public void run() {
		    if (ScriptTableModel.this.panel != null) {
			ScriptTableModel.this.panel.getProgressBar().setIndeterminate(true);
		    }
		    Object resultArray[];
		    try {
			Object resultItem = ocula.evaluate(ScriptTableModel.this.script);
			if (resultItem instanceof Collection) {
			    // Convert collection to array
			    resultItem = ((Collection)resultItem).toArray();
			}
			if (resultItem instanceof Object[]) {
			    // Copy array reference
			    resultArray = (Object[])resultItem;
			}
			else {
			    // Wrap single item
			    resultArray = new Object[1];
			    resultArray[0] = resultItem;
			}
			// Now have the array of object, just need to call the methods on them
			// corresponding to the colScripts.
			if (ScriptTableModel.this.panel != null) {
			    ScriptTableModel.this.panel.getProgressBar().setIndeterminate(false);
			    ScriptTableModel.this.panel.getProgressBar().setValue(0);
			}
			for (int i = 0; i < resultArray.length; i++) {
			    // For each result object in the array we create a new row in the table
			    for (int j = 0; j < ScriptTableModel.this.colScripts.length; j++) {
				String colScript = "tableItemValue = "+ScriptTableModel.this.colScripts[j];
				Object value = resultArray[i];
				Interpreter beanshell = new Interpreter();
				beanshell.set("value",value);
				Object result = "Error";
				try {
				    beanshell.eval(colScript);
				    result = beanshell.get("tableItemValue");
				}
				catch (EvalError ee) {
				    log.error("Unable to invoke column script", ee);
				}
				finally {
				    tableData[j].add(result);
				}
			    }
			    ScriptTableModel.this.fireTableRowsInserted(ScriptTableModel.this.rows,
									ScriptTableModel.this.rows);
			    ScriptTableModel.this.rows++;
			    if (ScriptTableModel.this.panel != null) {
				ScriptTableModel.this.panel.getProgressBar().setValue((100*(i+1))/resultArray.length);
				ScriptTableModel.this.panel.revalidate();
			    } 
			}
			if (ScriptTableModel.this.panel != null) {
			    ScriptTableModel.this.panel.getProgressBar().setValue(100);
			    try {
				Thread.sleep(200);
			    }
			    catch (Exception ex) {
				//
			    }
			    ScriptTableModel.this.panel.revalidate();
			}
		    }
		    catch (EvalError ee) {
			// Failed to evaluate the script, results in an empty table model
			if (ScriptTableModel.this.panel != null) {
			    ScriptTableModel.this.panel.getContents().removeAll();
			    ScriptTableModel.this.panel.getContents().
				add(new ErrorLabel("<html><body>Can't fetch components.<p>"+
						   "See log output for more details.</body></html>"));
			    ScriptTableModel.this.panel.revalidate();
			    ScriptTableModel.this.panel.getProgressBar().setIndeterminate(false);
			    ScriptTableModel.this.panel.getProgressBar().setValue(100);
			}
			log.error("Can't evaluate main table script", ee);
		    }
		    
		}
	    }.start();
	}
	
	public int getRowCount() {
	    return this.rows;
	}
	
	public int getColumnCount() {
	    return this.colScripts.length;
	}
	
	public String getColumnName(int col) {
	    return this.colNames[col];
	}

	public Class getColumnClass(int col) {
	    List l = tableData[col];
	    if (l.isEmpty()) {
		return String.class;
	    }
	    else {
		return l.get(0).getClass();
	    }
	}

	public Object getValueAt(int row, int col) {
	    return tableData[col].get(row);
	}

    }
    
}
