package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;

public class ParameterTableModel extends AbstractTableModel {

	private String[] columnNames;
	private Object[][] data;
	
	private int parameterCount;
	
	public List<RapidMinerParameterDescription> newTableParameters;
	
	public ParameterTableModel(List<RapidMinerParameterDescription> parameterDescriptions) {

		columnNames = new String[] {
				"Use", "Name", "Description", "Type", "Min", "Max", "Default Value", "Value"};
		
	
		// fill in data table
		newTableParameters = parameterDescriptions;
		parameterCount = parameterDescriptions.size();	// number of rows
		data = new Object[parameterCount][8];
		
		
		Iterator parameterIterator = parameterDescriptions.iterator();
		
		int i = 0;
		
		while (parameterIterator.hasNext()) {
			
			// get the current parameter description
			RapidMinerParameterDescription currentParam = (RapidMinerParameterDescription) parameterIterator.next();
			
				// add to array		0-use, 1-name, 2-description, 3-min, 4-max, 5-defVal, 6 val
				for (int n = 0; n < 8; n++) {
				
					switch (n) {
					
		            	case 0:  data[i][n] = currentParam.getUseParameter(); 
		            			 break;											// use
		            	case 1:  data[i][n] = currentParam.getParameterName();	
				            	 break;											// name
		            	case 2:	 data[i][n] = currentParam.getDescription(); 
				            	 break;											// desc
		            	case 3:  data[i][n] = currentParam.getType();			// type
		            			 break;
		            	case 4:  data[i][n] = currentParam.getMin();			// min
		         
		            		     if (data[i][n].equals("NaN")) {
		            				 data[i][n] = "";
		            			 }
		            			 
		            			 break;											
		            	case 5:	 data[i][n] = currentParam.getMax();			// max
		            			
		            			 if (data[i][n].equals("NaN")) {
		            				 data[i][n] = "";
		            			 }
		            			 
		            			 break;											
		            	case 6:  data[i][n] = currentParam.getDefaultValue();
		            			 if (data[i][n].equals("?")) {
		            				 data[i][n] = "";
		            			 }
		            			 break;											// default val
		            	case 7:  data[i][n] = currentParam.getExecutionValue();
		            			 if (data[i][n].equals("?")) {
		            				 data[i][n] = "";
		            			 }
		            			 break;											// value
		            	default: System.out.println("Invalid");break;

					}
				}
			
			i++;
		}
		
	}
	
	String[] validChoices = { 
		    "True", "False"
		  };

	protected Class[] columnClasses = new Class[] { 
		  boolean.class, String.class, String.class, String.class, double.class, double.class,
		  String.class, String[].class
	};

	 public Class getColumnClass(int c) {
         return getValueAt(0, c).getClass();
     }
	 
	@Override
	public String getColumnName(int iCol)
    {
        return columnNames[iCol];
    }

	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 8;
	}

	public int getRowCount() {
		// TODO Auto-generated method stub
		return parameterCount;
	}
	
	public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 7 || col == 0) {
        	
            return true;
        } else {
        	
            return false;
        }
    }
	
	public void setValueAt(Object value, int row, int col) {
       
            System.out.println("Setting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        data[row][col] = value;
        fireTableCellUpdated(row, col);
        
        // if column 0 - in use is checked
        if (col == 0) {
            newTableParameters.get(row).setUseParameter((Boolean)value);
        }

        if (col == 7) {
        	newTableParameters.get(row).setExecutionValue((String)value);
        }
        
    }
	
	public List<RapidMinerParameterDescription> getUpdatedParameters() {
		//System.out.println(" new table parameters" + newTableParameters.toString());
		return newTableParameters;
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		//if (columnIndex == 6) {
		//	return  "False";
		//} else {
		//	return "tester";
		//}
		return data[rowIndex][columnIndex];
	}

}
