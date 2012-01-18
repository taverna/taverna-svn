package idaservicetype.idaservicetype.ui.idawizard.wizard;

import ch.uzh.ifi.ddis.ida.api.Plan;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 28, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class PlansTable extends JTable {
    private Plan[] selectedPlans;


    public PlansTable () {
        super(new PlansTableModel());
        setGridColor(Color.LIGHT_GRAY);
        setRowHeight(getRowHeight() + 4);
        getColumnModel().getColumn(0).setMinWidth(5);
        getColumnModel().getColumn(1).setMinWidth(5);
        getColumnModel().getColumn(0).setResizable(false);
        getColumnModel().getColumn(1).setResizable(false);
        getColumnModel().getColumn(2).setMinWidth(300);
        getColumnModel().getColumn(2).setResizable(true);
    }

    public void addPlans (List<Plan> plans) {
        ((PlansTableModel) getModel()).addPlans(plans);
    }


    public List<Plan> getSelectedPlans() {
        return ((PlansTableModel) getModel()).getSelectedPlans();
    }

    public void removeAll() {
        ((PlansTableModel) getModel()).removeAll();
    }
}
