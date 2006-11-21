/*
 *
 * Copyright (C) 2003 The University of Manchester
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.man.cs.img.fetaClient.resource.FetaResources;

/**
 * @author alperp
 * 
 * 
 */
@SuppressWarnings("serial")
public class QueryPanel extends JPanel implements ActionListener,
		ChangeListener {

	private QueryCriteriaList model;

	private JButton addButton, queryButton;

	private QueryHelper helper;

	private List criteriaList;

	private JPanel titlePane, buttonPane, criteriaPane;

	private JTextField criteriaDrop;

	private SemanticMarkupManager markupManager;

	/**
	 * 
	 */
	public QueryPanel(QueryHelper queryHelper) {
		super();

		this.helper = queryHelper;
		model = new QueryCriteriaList();
		markupManager = new SemanticMarkupManager();

		assignList();

		this.setMaximumSize(new Dimension(500, 700));

		this.setLayout(new BorderLayout());
		criteriaPane = new JPanel();
		criteriaPane.setLayout(new BoxLayout(criteriaPane, BoxLayout.Y_AXIS));
		criteriaPane.setMaximumSize(new Dimension(700, Short.MAX_VALUE));
		criteriaPane.setOpaque(true);

		titlePane = new JPanel();
		titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.X_AXIS));
		titlePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

		titlePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel criteriaLabel = new JLabel("With Property");
		criteriaLabel.setFont(new Font("Arial", Font.BOLD, 12));

		JLabel valueLabel = new JLabel("Having Value");
		valueLabel.setFont(new Font("Arial", Font.BOLD, 12));

		titlePane.add(criteriaLabel);
		titlePane.add(Box.createRigidArea(new Dimension(100, 0)));
		titlePane.add(valueLabel);

		buttonPane = new JPanel();
		buttonPane.setLayout(new BorderLayout());
		addButton = new JButton("");
		addButton.setIcon(FetaResources.getIcon("add.gif"));
		addButton.addActionListener(this);

		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

		buttonPane.add(addButton, BorderLayout.EAST);

		model.addChangeListenerQuitely(this);
		buildPanel();

		queryButton = new JButton("Find Service");
		queryButton.setFont(new Font("Arial", Font.BOLD, 12));
		queryButton.addActionListener(this);
		queryButton.setIcon(FetaResources.getIcon("find.gif"));

		criteriaDrop = new JTextField();
		criteriaDrop.setEditable(false);
		criteriaDrop.setFont(new Font("Courier New", Font.PLAIN, 12));
		criteriaDrop.setText("Results from Registry at :  "
				+ queryHelper.getFetaEngineLocation().toString());

		this.add(criteriaPane, BorderLayout.CENTER);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		// bottomPane.setMaximumSize(new Dimension(/**Short.MAX_VALUE**/700,
		// 40));
		bottomPane.add(queryButton);
		bottomPane.add(criteriaDrop);
		this.add(bottomPane, BorderLayout.SOUTH);

	}

	/**
	 * 
	 */
	private void buildPanel() {

		criteriaPane.removeAll();
		criteriaPane.add(titlePane);
		criteriaPane.add(Box.createRigidArea(new Dimension(0, 4)));
		for (Iterator iter = model.getQueryList().iterator(); iter.hasNext();) {
			QueryCriteriaModel element = (QueryCriteriaModel) iter.next();
			QueryCriteriaAdaptor adaptor = new QueryCriteriaAdaptor(element);

			QueryCriteriaPane pane = new QueryCriteriaPane(adaptor,
					markupManager);
			pane.setCriteriaList(model);
			element.addChangeListener(adaptor);

			criteriaPane.add(pane);
		}
		criteriaPane.add(buttonPane);
	}

	/**
	 * 
	 */
	private void assignList() {
		for (int i = 0; i < 2; i++) {
			model.addQuery(new QueryCriteriaModel());

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub

		if (event.getSource() == addButton) {
			// model.addQuery(new QueryCriteriaModel(model));
			System.out.println("QueryPanel--action event from add button");
			model.addQuery(new QueryCriteriaModel());
		}

		if (event.getSource() == queryButton) {
			helper.query(model);
		}
	}

	/**
	 * @param pane
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == model) {
			System.out
					.print("QueryPanel -- model has changed so panel will be re-built");
			buildPanel();
			this.getParent().repaint();
		}

	}

	/**
	 * @param helper
	 */
	public void setHelper(QueryHelper helper) {
		this.helper = helper;
	}

}
