package uk.org.mygrid.logbook.ui;

import javax.swing.table.DefaultTableModel;

import uk.org.mygrid.logbook.ui.util.Label;

public class LabelListTableModel extends DefaultTableModel {

    private Label[] data;

    final String[] columnNames;

    public LabelListTableModel(Label[] data, String[] columnNames) {
        super();
        this.data = data;

        this.columnNames = columnNames;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int column) {

        return data[row].toString();

    }

    public Object[] getWorkflowsAt(int row) {

        return data[row].getWorkflowRuns();

    }

    public void setData(Label[] data) {

        this.data = data;
    }

    public void clear() {

        this.setDataVector(new Object[0][0], columnNames);

    }

    public Class getColumnClass(int c) {

        return String.class;
    }

}
