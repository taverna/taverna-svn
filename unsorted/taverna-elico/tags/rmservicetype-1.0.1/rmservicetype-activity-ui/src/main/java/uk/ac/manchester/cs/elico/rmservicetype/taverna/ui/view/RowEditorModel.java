package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.util.Hashtable;

import javax.swing.table.TableCellEditor;

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

public class RowEditorModel {

	private Hashtable data;
	      public RowEditorModel()
	      {
	          data = new Hashtable();
	      }
	     public void addEditorForRow(int row, TableCellEditor e )
	     {
	    	
	         data.put(new Integer(row), e);
	    	 
	     }
	     public void removeEditorForRow(int row)
	     {
	         data.remove(new Integer(row));
	     }
	     public TableCellEditor getEditor(int row)
	     {
	         return (TableCellEditor)data.get(new Integer(row));
	     }
	
}
