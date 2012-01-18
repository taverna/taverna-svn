package idaservicetype.idaservicetype.ui.idawizard.wizard;

import ch.uzh.ifi.ddis.ida.api.DataRequirement;


import idaservicetype.idaservicetype.ui.converter.InputIOObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/* Copyright (C) 2007, University of Manchester
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
public class InputDataTable extends JTable {

    private List<DataRequirement> ds;

    public InputDataTable () {
        super(new InputDataTableModel());
        ds = new ArrayList<DataRequirement>();
        setGridColor(Color.LIGHT_GRAY);
        setRowHeight(getRowHeight() + 4);
        getColumnModel().getColumn(0).setMinWidth(300);
        getColumnModel().getColumn(1).setMinWidth(100);

        getColumnModel().getColumn(2).setMinWidth(50);
        getColumnModel().getColumn(2).setResizable(false);
        getColumnModel().getColumn(2).setCellRenderer(new DeleteButtonRenderer());

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                JTable target = (JTable) mouseEvent.getSource();
                if (target.getSelectedColumn() == 2) {
                    removeItem(target.getSelectedRow());
                }
            }
        });



    }


    public List<DataRequirement> getDataRequirements () {
        return ds;
    }

    private void removeItem (int row) {
        System.err.println("removing from row" + row);
        ((InputDataTableModel) getModel()).removeItem(row);
    }

    public List<InputIOObject> getSelectInputFiles() {
        return ((InputDataTableModel) getModel()).getSelectedFiles();
    }

    static class DeleteButtonRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {

            ImageIcon icon = new ImageIcon(getClass().getResource("/net/sf/taverna/t2/workbench/icons/generic/delete.png"));
            return new JLabel(icon);

        }

    }

    public TableCellEditor getCellEditor(int row, int columns) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEnabled(true);
        comboBox.setRenderer(new ListCellRenderer () {

            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
                DataRequirement dr = (DataRequirement) o;

                JLabel l = new JLabel(dr.getRoleName());
                l.setBorder(UIManager.getBorder("Table.focusSelectedCellHighlightBorder"));
                return l;
            }
        });

        if (columns == 1) {
            for (DataRequirement dr : ds) {
                if (dr.getClassName().equals("Thing")) {
                    comboBox.addItem(dr);
                    comboBox.setSelectedItem(dr);

                }
            }
            return new DefaultCellEditor(comboBox);
        }
        else if (columns == 2) {
        }

        return new DefaultCellEditor(new JTextField());

    }

    public void addItem(String chosenRepositoryPath) {

        ((InputDataTableModel) getModel()).addItem(chosenRepositoryPath, ds.get(0));

    }
}
