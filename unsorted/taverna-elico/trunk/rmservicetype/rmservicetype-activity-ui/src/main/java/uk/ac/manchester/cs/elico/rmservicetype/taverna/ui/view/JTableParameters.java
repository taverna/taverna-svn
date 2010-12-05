package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

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
	        	  if (col == 6){
	        		  tmpEditor = rm.getEditor(row);
	        	  }
	          
	          if (tmpEditor!=null)
	              return tmpEditor;
	          return super.getCellEditor(row,col);
	      }

}
