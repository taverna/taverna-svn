package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import ch.uzh.ifi.ddis.ida.api.DataRequirement;
import uk.ac.manchester.cs.elico.converter.InputIOObject;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;/*
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
 * Author: Simon Jupp<br>
 * Date: Feb 25, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class InputDataTableModel extends AbstractTableModel {

    private List<InputIOObject> inputFileURIs;

    public void addItem(String chosenRepositoryPath, DataRequirement dr) {
        if (inputFileURIs == null) {
            inputFileURIs = new ArrayList<InputIOObject>();
        }

        inputFileURIs.add(new InputIOObject(chosenRepositoryPath, dr));
        fireTableDataChanged();
    }

    public InputDataTableModel() {
        inputFileURIs = new ArrayList<InputIOObject>();

    }

    public int getRowCount() {
        if (inputFileURIs == null) {
            return 0;
        }
        return inputFileURIs.size();
    }

    public String getColumnName(int i) {
        if (i == 0) {
            return "Location";
        }
        else if (i ==1) {
            return "Document Type";
        }
        else {
            return "Remove";
        }
    }

    public Class<?> getColumnClass(int i) {
        return super.getColumnClass(i);
    }

    public boolean isCellEditable(int row, int col) {
        if (col == 1) {
            return true;
        }
        else if (col == 2) {
            return true;
        }
        return false;
    }

    public int getColumnCount() {
        return 3;
    }

    public void fireTableCellUpdated(int i, int i1) {
        super.fireTableCellUpdated(i, i1);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public Object getValueAt(int row, int col) {

        if (col == 0 ) {

            return inputFileURIs.get(row).getFilePath();

        }
        else if (col == 1){
            return inputFileURIs.get(row).getDataRequirement().getRoleName();

        }

        return null;
    }

    public void setValueAt(Object o, int rowIndex, int columnIndex) {

        InputIOObject re;

        if (inputFileURIs == null ) {
            inputFileURIs = new ArrayList<InputIOObject>();
            re = new InputIOObject();
            inputFileURIs.add(re);
        }
        else {
            re = inputFileURIs.get(rowIndex);
        }

        if (columnIndex ==0) {
            re.setFilePath((String) o);
        }
        else if (columnIndex ==1 ) {
            re.setDataRequirement((DataRequirement) o);
        }


        fireTableDataChanged();
    }

    public void removeItem(int row) {
        inputFileURIs.remove(row);
        fireTableDataChanged();
    }

    public List<InputIOObject> getSelectedFiles() {
        return inputFileURIs;

    }




}
