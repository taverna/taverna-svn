package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;/*
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
 * Date: Mar 13, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class CSVImporter extends JPanel {

    private String seperator;

    private JTable table;

    private JPanel middle;

    public CSVImporter (final String file) {

        this.setLayout(new BorderLayout());

        // radio button at top

        JPanel top = new JPanel();

        JRadioButton comma   = new JRadioButton("COMMA"  , true);
        comma.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    table = createTable(file, ",");
                    ((DefaultTableModel) table.getModel()).fireTableStructureChanged();
                    ((DefaultTableModel) table.getModel()).fireTableDataChanged();
                    middle.add(new JScrollPane(table));
                    
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        });
        JRadioButton tab    = new JRadioButton("TAB"   , false);
        tab.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    table = createTable(file, "\t");
                    ((DefaultTableModel) table.getModel()).fireTableStructureChanged();
                    ((DefaultTableModel) table.getModel()).fireTableDataChanged();
                    middle.add(new JScrollPane(table));

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        });

        ButtonGroup bgroup = new ButtonGroup();
        bgroup.add(comma);
        bgroup.add(tab);
        top.setLayout(new GridLayout(1, 2));
        top.add(comma);
        top.add(tab);

        top.setBorder(BorderFactory.createTitledBorder(
           BorderFactory.createEtchedBorder(), "Column seperator"));

        // table sample in middle
        middle = new JPanel();
        try {
            JTable table = createTable(file, ",");
            table.setGridColor(Color.LIGHT_GRAY);
            middle.add(new JScrollPane(table));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // bottom panel with ok / cancel button
        JPanel bottom = new JPanel();
        

        this.add(top, BorderLayout.NORTH);
        this.add(middle, BorderLayout.CENTER);

    }

    public JTable createTable (String file, String seperator) throws IOException {

        BufferedReader br = new BufferedReader( new FileReader(file));
        String strLine = "";
        StringTokenizer st = null;
        int lineNumber = 0, tokenNumber = 0;

        String [] columns = null;
        String [][] data = null;
        //read comma separated file line by line

        while( (strLine = br.readLine()) != null)
        {

            if (lineNumber >6) {
                break;
            }
            //break comma separated line using ","
            st = new StringTokenizer(strLine, seperator);
            if (data == null) {
                System.out.println("Token count: " + st.countTokens());
                data = new String [6][st.countTokens()];
                columns = new String[st.countTokens()];
                for (int z = 1;z<=st.countTokens();z++) {
                    columns[z-1] = "col_" + z;
                }
                

            }
            else {
                while(st.hasMoreTokens())
                {
                    //display csv values
                    String token = st.nextToken();
                    data [lineNumber][tokenNumber] = token;
                    System.out.println("Line # " + lineNumber +
                            ", Token # " + tokenNumber
                            + ", Token : "+ token);
                    tokenNumber++;
                }
                lineNumber++;

            }

            //reset token number
            tokenNumber = 0;

        }


        return new JTable(new DefaultTableModel(data, columns));

    }

    public static void main(String[] args) {

        CSVImporter imp = new CSVImporter("/Users/simon/tmp/test.csv");
        JFrame frame = new JFrame();
        frame.add(imp);
        frame.setVisible(true);

    }

}
