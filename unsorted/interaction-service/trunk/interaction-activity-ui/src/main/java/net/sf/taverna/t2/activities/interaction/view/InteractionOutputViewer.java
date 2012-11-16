/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.interaction.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * A view representing {@link ActivityInputPortDefinitionBean}s of a
 * {@link BeanshellActivity} and the various parts which can be edited,
 * primarily the name, depth and granular depth.
 *
 * @author Ian Dunlop
 *
 */
@SuppressWarnings("serial")
public class InteractionOutputViewer extends JPanel {
	/** The bean which defines this view */
	private final ActivityOutputPortDefinitionBean bean;
	/** The name of the port */
	private JTextField nameField;
	/** The depth of the port */
	private JSpinner depthSpinner;

	/** Whether the values in the bean can be edited */
	private boolean editable;

	/**
	 * Sets the look and feel of the view through {@link #initView()} and sets
	 * the edit state using {@link #editable}
	 *
	 * @param bean
	 *            One of the output ports of the overall activity
	 * @param editable
	 *            whether the values of the bean are editable
	 */
	public InteractionOutputViewer(final ActivityOutputPortDefinitionBean bean,
			final boolean editable) {
		this.bean = bean;
		this.editable = editable;
		setBorder(javax.swing.BorderFactory.createEtchedBorder());
		initView();
		setEditable(editable);
	}

	/**
	 * Uses {@link GridBagLayout} for the layout. Adds components to edit the
	 * name, depth and granular depth
	 */
	private void initView() {
		setLayout(new GridBagLayout());
		final GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weighty = 0;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;

		this.nameField = new JTextField(this.bean.getName());
		add(this.nameField, outerConstraint);

		outerConstraint.gridx = 1;
		final SpinnerNumberModel depthModel = new SpinnerNumberModel(new Integer(this.bean
				.getDepth()), new Integer(0), new Integer(100), new Integer(1));
		this.depthSpinner = new JSpinner(depthModel);
		this.depthSpinner.setToolTipText("A depth of 0 means a simple value, like a string. Depth 1 is a list of simple values, while depth 2 is a list of a list of simple values");
		// depthSpinner.setValue(bean.getDepth());
		add(this.depthSpinner, outerConstraint);
	}

	/**
	 * Get the component which edits the name of the
	 * {@link ActivityOutputPortDefinitionBean}
	 *
	 * @return
	 */
	public JTextField getNameField() {
		return this.nameField;
	}

	/**
	 * The component which allows the depth of the
	 * {@link ActivityOutputPortDefinitionBean} to be changed
	 *
	 * @return
	 */
	public JSpinner getDepthSpinner() {
		return this.depthSpinner;
	}

	/**
	 * The actual {@link ActivityOutputPortDefinitionBean} described by this
	 * view
	 *
	 * @return
	 */
	public ActivityOutputPortDefinitionBean getBean() {
		return this.bean;
	}

	/**
	 * Can the bean be edited by this view?
	 *
	 * @return
	 */
	public boolean isEditable() {
		return this.editable;
	}

	/**
	 * Set the editable state of the view
	 *
	 * @param editable
	 */
	public void setEditable(final boolean editable) {
		this.editable = editable;
		setEditMode();
	}

	/**
	 * Sets the depth, granular depth and name components to be editable
	 */
	public void setEditMode() {
		// this.addMimeTypeButton.setVisible(editable);
		// this.mimeDropList.setVisible(editable);
		this.depthSpinner.setEnabled(this.editable);
		this.nameField.setEditable(this.editable);

	}

}
