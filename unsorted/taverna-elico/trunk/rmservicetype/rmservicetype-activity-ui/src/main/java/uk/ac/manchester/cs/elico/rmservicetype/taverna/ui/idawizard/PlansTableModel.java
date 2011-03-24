package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import ch.uzh.ifi.ddis.ida.api.OperatorApplication;
import ch.uzh.ifi.ddis.ida.api.Plan;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Date: Feb 28, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class PlansTableModel  extends AbstractTableModel {

    private List<Plan> plans;

    private List<Plan> selectedPlans;

    public PlansTableModel () {
        plans = new ArrayList<Plan>();
        selectedPlans = new ArrayList<Plan>();

    }
    public int getRowCount() {
        return plans.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public Object getValueAt(int row, int col) {

        Plan p = plans.get(row);
        if (col == 0) {
            return selectedPlans.contains(p);
        }
        if (col == 1) {
            return String.valueOf(p.getRank());
        }
        else {

            String planDesc ="";
            for (OperatorApplication opAp : p.getOperatorApplications()) {
                planDesc = processOperatorApplication(opAp);
            }
            return planDesc;
        }
    }

    private String processOperatorApplication(OperatorApplication opAp) {
        StringBuilder sb = new StringBuilder();
        List<OperatorApplication> basicOpApps = new ArrayList<OperatorApplication>();
        if (opAp.getOpType().name().equals("BASIC")) {

            sb.append(opAp.getOperatorName());
            sb.append(", ");
        }
        else {
            for (OperatorApplication steps : opAp.getSteps()) {
                sb.append(processOperatorApplication(steps));
            }
        }
        return sb.toString();
    }


    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex == 0) {
            return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    public String getColumnName(int col) {
        if (col ==0) {
            return "Select";
        }
        else if (col == 1) {
            return "Rank";
        }
        else {
            return "Plan operators";
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Plan p = plans.get(rowIndex);
        if(((Boolean) aValue)) {
            selectedPlans.add(p);
        }
        else {
            selectedPlans.remove(p);
        }
        fireTableDataChanged();
    }

    public class PlansComarator implements Comparator<Plan> {


        public int compare(Plan plan, Plan plan1) {
            return Double.compare(plan1.getRank(),plan.getRank());
        }
    }

    public void addPlans(List<Plan> plans) {

        Collections.sort(plans, new PlansComarator());

        this.plans.addAll(plans);
        fireTableDataChanged();
    }

    public List<Plan> getSelectedPlans() {
        return selectedPlans;
    }

    public void removeAll() {
        plans.clear();
        selectedPlans.clear();
        fireTableDataChanged();
    }
}