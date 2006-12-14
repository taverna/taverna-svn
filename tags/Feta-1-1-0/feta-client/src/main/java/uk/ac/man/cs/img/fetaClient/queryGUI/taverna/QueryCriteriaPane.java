/*
 * Created on Feb 25, 2004
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
// TODO sort out how to enter controlled vocabulary
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

import uk.ac.man.cs.img.fetaClient.resource.FetaResources;

/**
 * @author alperp
 * 
 * 
 * 
 */
public class QueryCriteriaPane extends JPanel implements ActionListener {

	// P2 private JButton setButton;
	private JButton subtractButton;

	private JComboBox criteriaTypeCombo;

	private JComboBox valueCombo;

	private JTextField valueField;

	private QueryCriteriaAdaptor adaptor;

	private QueryCriteriaList criteriaList;

	private SemanticMarkupManager markupManager;

	/**
	 * 
	 */
	public QueryCriteriaPane(QueryCriteriaAdaptor adaptor,
			SemanticMarkupManager listt) {
		super();
		this.adaptor = adaptor;
		this.markupManager = listt;
		adaptor.setView(this);
		initialise();
	}

	/**
	 * 
	 */
	private void initialise() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
		this.setLayout(layout);

		this.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		criteriaTypeCombo = new JComboBox(adaptor.getCriteriaTypeArray());
		criteriaTypeCombo.setEditable(false);

		valueField = new JTextField();
		valueField.setPreferredSize(new Dimension(40, 20));
		valueField.setSize(new Dimension(40, 20));

		valueCombo = new JComboBox();
		valueCombo.setPreferredSize((new Dimension(40, 20)));
		valueCombo.setSize(new Dimension(40, 20));
		valueCombo.setEditable(false);
		valueCombo.addActionListener(this);

		// P2 setButton = new JButton("Set");
		// P2 setButton.addActionListener(this);

		subtractButton = new JButton();
		subtractButton.setIcon(FetaResources.getIcon("delete.gif"));
		subtractButton.addActionListener(this);

		// P this.add(criteriaTypeSpinner);

		this.add(criteriaTypeCombo);
		this.add(Box.createRigidArea(new Dimension(20, 0)));
		this.add(valueField);

		this.add(valueCombo);
		valueCombo.setVisible(false);
		FetaOntologyComboRenderer renderer = new FetaOntologyComboRenderer();
		// renderer.setPreferredSize(new Dimension(60, 20));
		valueCombo.setRenderer(renderer);

		// this.add(setButton);
		this.add(subtractButton);

		addListener(adaptor);
		adaptor.updateView();
	}

	/**
	 * @return
	 */
	public QueryCriteriaAdaptor getAdaptor() {
		return adaptor;
	}

	/**
	 * @param adaptor
	 */
	public void setAdaptor(QueryCriteriaAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == subtractButton) {
			criteriaList.removeQuery(adaptor.getModel());
		}
		if (event.getSource() == valueCombo) {
			valueField.setText(valueCombo.getSelectedItem().toString());
			// adaptor.setValue(valueCombo.getSelectedItem());
			// PERSEMBE
		}
		// P2 if (event.getSource()==setButton) {
		// P2 adaptor.setValue();
		// P2 }

	}

	/**
	 * @return
	 */
	public QueryCriteriaList getCriteriaList() {
		return criteriaList;
	}

	/**
	 * @param list
	 */
	public void setCriteriaList(QueryCriteriaList list) {
		criteriaList = list;
	}

	/**
	 * @param string
	 */
	public void setCriteriaType(QueryCriteriaType type) {
		// P criteriaTypeSpinner.setValue(type);
		criteriaTypeCombo.setSelectedItem(type);

	}

	/**
	 * @param string
	 */
	public void setCriteriaValue(String value) {
		QueryCriteriaType typee = (QueryCriteriaType) criteriaTypeCombo
				.getSelectedItem();
		if ((typee == QueryCriteriaType.NAME_CRITERIA_TYPE)
				|| (typee == QueryCriteriaType.DESCRIPTION_CRITERIA_TYPE)) {
			valueField.setText(value);
		} else {
			valueCombo.setSelectedItem(value);
		}

	}

	/**
	 * 
	 */
	public QueryCriteriaType getCriteriaType() {
		return (QueryCriteriaType) criteriaTypeCombo.getSelectedItem();
		// return (QueryCriteriaType) criteriaTypeSpinner.getValue();

	}

	/**
	 * @return
	 */
	public Object getCriteriaValue() {
		/* return valueField.getText(); */

		QueryCriteriaType typee = (QueryCriteriaType) criteriaTypeCombo
				.getSelectedItem();
		if ((typee == QueryCriteriaType.NAME_CRITERIA_TYPE)
				|| (typee == QueryCriteriaType.DESCRIPTION_CRITERIA_TYPE)) {
			return (Object) valueField.getText();
		} else {
			return (Object) valueCombo.getSelectedItem();
		}

	}

	/**
	 * 
	 */
	public void allowStringEditing(boolean flag) {
		valueField.setVisible(flag);
		valueCombo.setVisible(!flag);
		// setButton.setEnabled(!flag);
	}

	public void updateDefaultValueList(QueryCriteriaType type) {

		if ((type == QueryCriteriaType.NAME_CRITERIA_TYPE)
				|| (type == QueryCriteriaType.DESCRIPTION_CRITERIA_TYPE)) {
			allowStringEditing(true);

		} else {

			allowStringEditing(false);
			fillCombo(this.markupManager.getListForCriteriaType(type));

		}

		this.repaint();
	}

	public boolean eventFromHere(ChangeEvent event) {
		for (int i = 0; i < this.getComponents().length; i++) {
			if (event.getSource() == this.getComponent(i)) {
				return true;
			}
		}
		return false;
	}

	public boolean eventFromHere(ActionEvent event) {
		for (int i = 0; i < this.getComponents().length; i++) {
			if (event.getSource() == this.getComponent(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param adaptor
	 */
	public void removeListener(QueryCriteriaAdaptor adaptor) {
		criteriaTypeCombo.removeActionListener(adaptor);
		valueField.removeCaretListener(adaptor);

		// PERSEMBE
	}

	/**
	 * @param adaptor
	 */
	public void addListener(QueryCriteriaAdaptor adaptor) {
		criteriaTypeCombo.addActionListener(adaptor);
		valueField.addCaretListener(adaptor);

		// PERSEMBE

	}

	private void fillCombo(List fillers) {
		Vector tmp = new Vector(fillers);
		valueCombo.setModel(new DefaultComboBoxModel(tmp));

		if (valueCombo.getItemCount() != 0) {
			valueCombo.setSelectedIndex(0);
		}
		valueCombo.repaint();
	}

}
