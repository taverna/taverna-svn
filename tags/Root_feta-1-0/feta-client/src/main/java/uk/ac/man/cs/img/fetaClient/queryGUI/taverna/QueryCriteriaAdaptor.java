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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author alperp
 * 
 * 
 * 
 */
public class QueryCriteriaAdaptor implements ChangeListener, CaretListener,
		ActionListener {
	private QueryCriteriaModel model;

	private QueryCriteriaPane view;

	// private SemanticMarkup markup;
	/**
	 * @param element
	 */
	public QueryCriteriaAdaptor(QueryCriteriaModel model) {
		super();
		this.model = model;
	}

	/**
	 * 
	 */
	public QueryCriteriaAdaptor() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public QueryCriteriaType[] getCriteriaTypeArray() {
		// TODO Auto-generated method stub
		QueryCriteriaType[] result = new QueryCriteriaType[QueryCriteriaType
				.getSize(QueryCriteriaType.class)];
		int i = 0;
		for (Iterator iter = QueryCriteriaType
				.iterator(QueryCriteriaType.class); iter.hasNext();) {
			QueryCriteriaType element = (QueryCriteriaType) iter.next();
			result[i] = element;
			++i;

		}
		return result;

	}

	/**
	 * @return
	 */
	public QueryCriteriaModel getModel() {
		return model;
	}

	/**
	 * @param model
	 */
	public void setModel(QueryCriteriaModel model) {
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == model) {
			// System.out.println("adaptor---Change event from model");
			updateView();

		}

		else if (view.eventFromHere(event)) {
			// criteria type changed in view
			// System.out.println("adaptor---Change event detected from view
			// "+event.getSource().getClass().toString());
			updateModel();
			updateEditingStatus();
			updateDefaultValueList(model.getCriteriaType());

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		if (view.eventFromHere(event)) {
			// criteria type changed in view

			// System.out.println("adaptor- Action Performed event from View");
			updateModel();
			updateEditingStatus();
			updateDefaultValueList(model.getCriteriaType());
			// System.out.println("QUERY-CRITERIA ADAPTOR--The View changed so
			// the model will be updated");
		}

	}

	/**
	 * 
	 */
	private void updateEditingStatus() {
		view.allowStringEditing(isStringValue());

	}

	private void updateDefaultValueList(QueryCriteriaType type) {
		view.updateDefaultValueList(type);
	}

	private boolean isStringValue() {
		return model.getCriteriaType().getModelClass() == String.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent arg0) {
		System.out.println("adaptor- Caret Update"
				+ arg0.getSource().getClass());
		updateModel();

	}

	/**
	 * 
	 */
	private void updateModel() {
		System.out.println("adaptor- Debug in  Update Model");
		model.removeChangeListener(this);
		model.setCriteriaType(view.getCriteriaType());
		model.setValue((Object) view.getCriteriaValue());
		model.addChangeListenerQuitely(this);
	}

	/**
	 * @return
	 */
	public QueryCriteriaPane getView() {
		return view;
	}

	/**
	 * @param pane
	 */
	public void setView(QueryCriteriaPane pane) {
		view = pane;
	}

	/**
	 * 
	 */
	public void updateView() {
		// System.out.println("adaptor- Debug in Update View");
		view.removeListener(this);
		updateEditingStatus();
		updateDefaultValueList(model.getCriteriaType());
		view.setCriteriaType(model.getCriteriaType());
		view.setCriteriaValue(model.valueDisplayString());
		view.addListener(this);
	}

	/**
	 * 
	 */
	public void setValue(Object value) {
		// System.out.println("adaptor- Debug in SetValue");
		model.setValue(value);
	}

}
