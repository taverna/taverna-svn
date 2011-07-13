package uk.ac.manchester.cs.elico.utilities.csvimporter;

import java.awt.Point;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class CsvImporterTableModel extends AbstractTableModel {

	private Hashtable lookup;

	private final int rows;

	private final int columns;

	private String headers[];
	  
	public CsvImporterTableModel(int rows, String columnHeaders[]) {
		
		if ((rows < 0) || (columnHeaders == null)) {
		      throw new IllegalArgumentException(
		          "Invalid row count/columnHeaders");
		}
		
		this.rows = rows;
		this.columns = columnHeaders.length;
		headers = columnHeaders;
		lookup = new Hashtable();
		
	}
	
	public void setColumnHeaders(String columnHeaders[] ) {
		
		headers = columnHeaders;
		
	}
	
	protected Class[] columnClasses = new Class[] { 
			  String.class, String.class, String.class, String.class, String.class,
			  String.class, String.class, String.class, String.class, String.class
	};
	
	public Class getColumnClass(int c) {
        	return String.class;
    }
	 
	@Override
	public String getColumnName(int column) {
		    return headers[column];
	}
	
	public int getColumnCount() {
		return columns;
	}

	public int getRowCount() {
		return rows;
	}

	public Object getValueAt(int row, int column) {
		 return lookup.get(new Point(row, column));
	}
	
	public void setValueAt(Object value, int row, int column) {
		
		if ((rows < 0) || (columns < 0)) {
	      throw new IllegalArgumentException("Invalid row/column setting");
	    }
	    if ((row < rows) && (column < columns)) {
	      lookup.put(new Point(row, column), value);
	    }
	
	}
	
	public void clearTable() {
		
		lookup.clear();
		
	}
	

}
