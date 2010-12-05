package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import javax.swing.table.AbstractTableModel;

public class ParameterTableModel extends AbstractTableModel {

	private String[] columnNames;
	
	public ParameterTableModel() {
		// TODO Auto-generated constructor stub
		columnNames = new String[] {
				"Use", "Name", "Description", "Min", "Max", "Default Value", "Value"};
		
	}
	

	@Override
	public String getColumnName(int iCol)
    {
        return columnNames[iCol];
    }

	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

}
