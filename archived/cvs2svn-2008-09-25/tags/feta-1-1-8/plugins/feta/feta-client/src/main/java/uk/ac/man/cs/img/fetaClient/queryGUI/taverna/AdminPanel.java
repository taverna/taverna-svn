/*
 * Copyright (C) 2005 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import uk.ac.man.cs.img.fetaClient.resource.FetaResources;

/**
 * @author alperp
 * 
 */
public class AdminPanel extends JPanel implements ActionListener {

	private QueryHelper helper;

	private JTextArea storeContentArea, rdqlQueryArea, rdqlResultArea;

	private JTextField fetaAdminPortLocField;

	JButton refreshButton, queryButton;

	public AdminPanel(QueryHelper queryHelper) {

		super();

		this.helper = queryHelper;

		initialize();

	}

	private void initialize() {

		fetaAdminPortLocField = new JTextField();
		fetaAdminPortLocField.setMaximumSize(new Dimension(250, 50));

		fetaAdminPortLocField
				.setText("http://localhost:8080/fetaEngine/services/fetaAdmin");

		refreshButton = new JButton("Refresh Engine");
		refreshButton.addActionListener(this);
		refreshButton.setIcon(FetaResources.getIcon("refresh.gif"));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Bottom Pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.add(fetaAdminPortLocField);
		bottomPane.add(refreshButton);

		storeContentArea = new JTextArea();
		storeContentArea.setPreferredSize(new Dimension(150, 450));
		storeContentArea.setEditable(false);
		storeContentArea.setBackground(java.awt.Color.lightGray);

		// Topright Pane
		JPanel topRightPane = new JPanel();
		topRightPane.setLayout(new BorderLayout());
		JScrollPane topRightScrollPane = new JScrollPane(storeContentArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		topRightPane.add(topRightScrollPane, BorderLayout.CENTER);

		// Top left Pane
		JPanel topLeftPane = new JPanel();
		topLeftPane.setLayout(new BoxLayout(topLeftPane, BoxLayout.Y_AXIS));

		rdqlQueryArea = new JTextArea();
		rdqlQueryArea.setPreferredSize(new Dimension(150, 200));

		rdqlQueryArea.setEditable(true);

		queryButton = new JButton("Submit RDQL Query");
		queryButton.addActionListener(this);
		// queryButton.setIcon(FetaResources.getIcon("refresh.gif"));

		rdqlResultArea = new JTextArea();
		rdqlResultArea.setPreferredSize(new Dimension(150, 200));
		// rdqlResultArea.setEditable(false);
		rdqlResultArea.setBackground(java.awt.Color.lightGray);

		JScrollPane rdqlQueryScrollPane = new JScrollPane(rdqlQueryArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane rdqlResultScrollPane = new JScrollPane(rdqlResultArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		topLeftPane.add(rdqlQueryScrollPane);
		topLeftPane.add(queryButton);
		topLeftPane.add(rdqlResultScrollPane);

		// Top left Pane
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
		topPane.add(topLeftPane);
		topPane.add(topRightPane);

		this.add(topPane);
		this.add(bottomPane);

		this.setMaximumSize(new Dimension(500, 700));

	}

	public QueryHelper getHelper() {
		return helper;
	}

	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == refreshButton) {
			refresh();
		}
		if (event.getSource() == queryButton) {
			query();
		}
	}

	public void refresh() {
		URL fetaAdminPortLocation;
		try {
			fetaAdminPortLocation = new URL(this.fetaAdminPortLocField
					.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to generate a valid URL from the provided Feta admin port location."
							+ "Error message is :" + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.storeContentArea
				.setText("Store Contents in RDF/XML : \n-------------------------");
		this.storeContentArea.append(helper
				.getStoreContent(fetaAdminPortLocation));

	}

	public void query() {
		URL fetaPortLocation;
		try {
			fetaPortLocation = new URL(this.fetaAdminPortLocField.getText()
					.replaceAll("fetaAdmin", "feta"));
			System.out.println(fetaPortLocation.toString());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to generate a valid URL from the provided Feta admin port location."
							+ "Error message is :" + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.rdqlResultArea
				.setText("QueryResults in RDF/XML : \n------------------------");
		this.rdqlResultArea.append(helper.freeFormQuery(fetaPortLocation,
				this.rdqlQueryArea.getText()));

	}

}
