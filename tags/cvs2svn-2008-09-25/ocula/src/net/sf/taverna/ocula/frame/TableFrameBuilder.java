/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 * Copyright 2005 University of Manchester
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.Parser;
import net.sf.taverna.ocula.ui.ErrorLabel;
import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.ResultSetPanel;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Handles the &lt;table&gt; element and builds a TableModel corresponding
 * to the specified columns in the objects resulting from the script.
 * @author Tom Oinn
 * @author Ismael Juma
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
	
	// Get the column scripts
	List columnScripts = new ArrayList();
	List columnNames = new ArrayList();
	for (Iterator i = element.getChildren("column").iterator(); i.hasNext();) {
	    Element colDef = (Element)i.next();
	    columnNames.add(colDef.getAttributeValue("name","NoName"));
	    columnScripts.add(colDef.getTextTrim());
	}
	final TableFrame rsp = new TableFrame(name, Icons.getIcon(iconName));
	rsp.getContents().setLayout(new BorderLayout());
	
	String key = element.getAttributeValue("key");
	if (key != null) {
	    o.putContext(key, new FrameAndElement(rsp, element));
	}
	
	Parser parser = new Parser(o);
	Object[] targetObjects = null;
	rsp.getProgressBar().setIndeterminate(true);
	try {
	    targetObjects = parser.parseScript(element);
	}
	catch (EvalError ee) {
		if (rsp != null) {
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    rsp.getContents().removeAll();
			    rsp.getContents().add(new ErrorLabel("<html><body>Can't" +
			    		"fetch components.<p>"+
			    		"See log output for more details.</body></html>"));
			    rsp.getProgressBar().setIndeterminate(false);
			    rsp.getProgressBar().setValue(100);
			    rsp.revalidate();
			}
		    });
		}
		log.error("Can't evaluate main table script", ee);
	}
	
	String sortColumn = element.getAttributeValue("sortcolumn");
	boolean ascending = true;
	if (sortColumn != null) {
	    String sortType = element.getAttributeValue("sorttype", "ascending");
	    if (sortType.equals("descending")) {
		ascending = false;
	    }
	}
	
	ScriptTableModel stb = new ScriptTableModel(o, rsp, targetObjects,
		(String[])columnScripts.toArray(new String[0]), 
		(String[])columnNames.toArray(new String[0]), sortColumn, ascending);
	JTable table = new JTable(stb);
	parser.parseClick(element, table, targetObjects);
	parser.parseDoubleClick(element, table, targetObjects);
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
	List tableData;
	final ResultSetPanel panel;
	final Object[] targetObjects;
	
	public ScriptTableModel(Ocula o, ResultSetPanel panel,
		final Object[] targetObjects, String[] colScripts, String[] colNames,
		final String sortColumn, final boolean ascending) {
	    //super();
	    this.ocula = o;
	    this.targetObjects = targetObjects;
	    this.colScripts = colScripts;
	    this.colNames = colNames;
	    this.panel = panel;
	    tableData = new ArrayList();
	    new Thread() {
		public void run() {
		    try {
			
			// Now have the array of object, just need to call the methods on them
			// corresponding to the colScripts.
			if (ScriptTableModel.this.panel != null) {
			    SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    ScriptTableModel.this.panel.getProgressBar().setIndeterminate(false);
				    ScriptTableModel.this.panel.getProgressBar().setValue(0);    
				}
			    });
			}
			for (int i = 0; i < targetObjects.length; i++) {
			    Object[] row = new Object[ScriptTableModel.this.colScripts.length];
			    // For each result object in the array we create a new row in the table
			    for (int j = 0; j < ScriptTableModel.this.colScripts.length; j++) {
				String colScript = "tableItemValue = "+ScriptTableModel.this.colScripts[j];
				Object value = targetObjects[i];
				Interpreter beanshell = new Interpreter();
				beanshell.set("value",value);
				synchronized (ocula.getContextMutex()) {
				    for (Iterator contextIterator = ocula
					    .getContextKeySetIterator(); contextIterator
					    .hasNext();) {
					String keyName = (String) contextIterator
						.next();
					beanshell.set(keyName, ocula
						.getContext(keyName));
				    }
				}
				Object result = "Error";
				try {
				    beanshell.eval(colScript);
				    result = beanshell.get("tableItemValue");
				}
				catch (EvalError ee) {
				    log.error("Unable to invoke column script", ee);
				}
				finally {
				    row[j] = result;
				}
			    }
			    tableData.add(row);
			    ScriptTableModel.this.fireTableRowsInserted(ScriptTableModel.this.rows,
									ScriptTableModel.this.rows);
			    ScriptTableModel.this.rows++;
			    if (ScriptTableModel.this.panel != null) {
				final int m = i;
				SwingUtilities.invokeLater(new Runnable() {
				    public void run() {
					ScriptTableModel.this.panel.getProgressBar().setValue((100*(m+1))/targetObjects.length);
					ScriptTableModel.this.panel.revalidate();
				    }
				});
			    } 
			}
			if (sortColumn != null) {
			    sortData(sortColumn, ascending);
			}
			ScriptTableModel.this.fireTableChanged(new TableModelEvent(ScriptTableModel.this));
			if (ScriptTableModel.this.panel != null) {
			    SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    ScriptTableModel.this.panel.getProgressBar().setValue(100);
				    ScriptTableModel.this.panel.revalidate();
				}
			    });
			}
		    }
		    catch (EvalError ee) {
			// Failed to evaluate the script, results in an empty table model
			if (ScriptTableModel.this.panel != null) {
			    SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    ScriptTableModel.this.panel.getContents().removeAll();
				    ScriptTableModel.this.panel.getContents().
					add(new ErrorLabel("<html><body>Can't fetch components.<p>"+
							   "See log output for more details.</body></html>"));
				    ScriptTableModel.this.panel.revalidate();
				    ScriptTableModel.this.panel.getProgressBar().setIndeterminate(false);
				    ScriptTableModel.this.panel.getProgressBar().setValue(100);    
				}
			    });
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
	    Object[] o = (Object[]) tableData.get(0);
	    if (o == null)
		return String.class;
	    
	    else
		return o[col].getClass();
	}

	public Object getValueAt(int row, int col) {
	    return ((Object[]) tableData.get(row))[col];
	}
	
	public void sortData(String columnName, boolean ascending) {
	    int columnNumber = getColumnNumber(columnName);
	    Collections.sort(tableData, new TableComparator(columnNumber, ascending));
	}
	
	public void sortData(int columnNumber, boolean ascending) {
	    Collections.sort(tableData, new TableComparator(columnNumber, ascending));
	}
	
	private int getColumnNumber(String columnName) {
	    for (int i = 0; i < colNames.length; ++i) {
		if (columnName.equals(colNames[i])) {
		    return i;
		}
	    }
	    return -1;
	}
    }
    
    /**
     * A comparison function that sorts a table according to the natural (or
     * inverse) ordering of the elements in a specified column. This natural
     * ordering is ascertained by using the compareTo method from the
     * Comparable interface for each element. As a result, it is a
     * requirement that the objects in the specified column implement the
     * Comparable interface.
     * 
     * @author Ismael Juma (ismael@juma.me.uk)
     */
    class TableComparator implements Comparator {
	private int columnNumber;

	private boolean ascending;

	/**
	 * Creates a Comparator object with the specified parameters.
	 * @param columnNumber The number of the column whose elements should be
	 * used by the sorting function.
	 * @param ascending Whether the items should be sorted according to their
	 * natural or inverse ordering.
	 */
	public TableComparator(int columnNumber, boolean ascending) {
	    this.columnNumber = columnNumber;
	    this.ascending = ascending;
	}

	/**
	 * Retrieves two elements from two arrays and compares them by using the
	 * compareTo method specified by the Comparable interface.
	 * @param o1 An array of objects that implement the Comparable interface.
	 * @param o2 An array of objects that implement the Comparable interface.
	 * @return A negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * 
	 * @throws ClassCastException if the specified object's type prevents it
	 * from being compared to this Object.
	 */
	public int compare(Object o1, Object o2) {
	    Object[] array1 = (Object[]) o1;
	    Object[] array2 = (Object[]) o2;
	    Comparable comp1 = (Comparable) array1[columnNumber];
	    Comparable comp2 = (Comparable) array2[columnNumber];
	    int result = comp1.compareTo(comp2);
	    if (!ascending) {
		result = -result;
	    }
	    return result;
	}
    }

}
