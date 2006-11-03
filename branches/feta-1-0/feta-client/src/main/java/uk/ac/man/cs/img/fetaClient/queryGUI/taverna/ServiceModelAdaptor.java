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

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author alperp
 * 
 * 
 */
public class ServiceModelAdaptor implements ActionListener, DocumentListener,
		ChangeListener, CaretListener {

	BasicServiceModel model;

	ServiceFormPanel view;

	/**
	 * 
	 */
	public ServiceModelAdaptor() {
		super();

	}

	public ServiceModelAdaptor(BasicServiceModel model) {
		super();
		this.model = model;
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == model) {
			if (view != null) {

				// view.removeListener(this);
				view.setServiceName(model.getServiceName());
				view.setServiceDescriptionText(model
						.getServiceDescriptionText());
				view.setLocationURL(model.getServiceLocation());
				view.setInterfaceLocationURL(model
						.getServiceInterfaceLocation());
				view.setOrganisationName(model.getServiceOrganisationName());
				view.setServiceDescriptionLocation(model
						.getServiceDescriptionLocation());

				if (model.getOperationModel() != null) {
					// System.out.println("Debug in else the operation model is
					// NOTTTT null");
					view.setOperationName(model.getOperationModel()
							.getOperationName());
					view.setOperationDescriptionText(model.getOperationModel()
							.getOperationDescriptionText());
					view.setOperationMethod(model.getOperationModel()
							.getOperationMethod() != null ? model
							.getOperationModel().getOperationMethod()
							.getLabel() : null);
					view.setOperationTask(model.getOperationModel()
							.getOperationTask() != null ? model
							.getOperationModel().getOperationTask().getLabel()
							: null);
					view.setOperationResource(model.getOperationModel()
							.getOperationResource() != null ? model
							.getOperationModel().getOperationResource()
							.getLabel() : null);
					// view.setOperationResourceContent(model.getOperationModel().getOperationResourceContent()!=null?model.getOperationModel().getOperationResourceContent().getLabel():null);
					// view.setOperationApplication(model.getOperationModel().getOperationApplication()!=null?model.getOperationModel().getOperationApplication().getLabel():null);
				} else {
					// System.out.println("Debug in else the operation model is
					// null");
				}

				if (model.getServiceType() != null) {
					view.setServiceType(model.getServiceType());
				}

			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		updateModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent arg0) {
		updateModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent arg0) {
		updateModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent arg0) {
		updateModel();

	}

	private void updateModel() {

		// do nothing..... our view is read only for now
	}

	/**
	 * @return
	 */
	public BasicServiceModel getModel() {
		return model;
	}

	/**
	 * @return
	 */
	public ServiceFormPanel getView() {
		return view;
	}

	/**
	 * @param model
	 */
	public void setModel(BasicServiceModel model) {
		this.model = model;
	}

	/**
	 * @param panel
	 */
	public void setView(ServiceFormPanel panel) {
		view = panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent arg0) {
		updateModel();

	}

}
