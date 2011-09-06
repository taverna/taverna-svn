package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

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

public class JTableParameters extends JTable {
	
	 protected RowEditorModel rm;
	 
	      public JTableParameters()
	      {
	          super();
	          rm = null;
	      }
	 
	      public JTableParameters(TableModel tm)
	      {
	          super(tm);
	          rm = null;
	      }
	 
	     public JTableParameters(TableModel tm, TableColumnModel cm)
	      {
	          super(tm,cm);
	          rm = null;
	      }
	 
	      public JTableParameters(TableModel tm, TableColumnModel cm,
	       ListSelectionModel sm)
	      {
	          super(tm,cm,sm);
	          rm = null;
	      }
	 
	      public JTableParameters(int rows, int cols)
	      {
	          super(rows,cols);
	          rm = null;
	      }
	 
	      public JTableParameters(final Vector rowData, final Vector columnNames)
	      {
	          super(rowData, columnNames);
	          rm = null;
	      }
	 
	      public JTableParameters(final Object[][] rowData, final Object[] colNames)
	      {
	          super(rowData, colNames);
	          rm = null;
	      }
	 
	      // new constructor
	      public JTableParameters(TableModel tm, RowEditorModel rm)
	      {
	          super(tm,null,null);
	          this.rm = rm;
	      }
	 
	      public void setRowEditorModel(RowEditorModel rm)
	      {
	          this.rm = rm;
	      }
	 
	      public RowEditorModel getRowEditorModel()
	      {
	          return rm;
	      }
	 
	      public TableCellEditor getCellEditor(int row, int col)
	      {
	          TableCellEditor tmpEditor = null;
	          if (rm!=null)
	        	  if (col == 7){
	        		  tmpEditor = rm.getEditor(row);
	        	  }
	          
	          if (tmpEditor!=null)
	              return tmpEditor;
	          return super.getCellEditor(row,col);
	      }

}
