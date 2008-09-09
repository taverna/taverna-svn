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
package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.cloudone.gui.entity.model.StringModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModelEvent;

import org.apache.log4j.Logger;

/**
 * A View (in MVC terms) for a {@link String}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class StringView extends
		EntityView<StringModel, String, StringModelEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6732819222408128223L;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(StringView.class);
	private EntityListView parentView;
	private JTextArea textArea;
	private JButton okButton;
	private JButton editButton;
	private JButton removeButton;
	private OKAction okAction = new OKAction();
	private EditAction editAction = new EditAction();
	private RemoveAction removeAction = new RemoveAction();
	@SuppressWarnings("unused")
	private JComboBox comboBox;
	private JPanel editPanel;
	@SuppressWarnings("unused")
	private JPanel viewPanel;
	private StringModel model;
	@SuppressWarnings("unused")
	private String string;

	/**
	 * Set the {@link StringModel} and the parent view.
	 * 
	 * @param model
	 * @param parentView
	 */
	public StringView(StringModel model, EntityListView parentView) {
		super(model, parentView);
		this.parentView = parentView;
		this.model = model;
		initialise();
	}

	private void initialise() {
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "String",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		editPanel = new JPanel();
		editPanel.setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.RED));
		// setOpaque(false);
		GridBagConstraints headerC = new GridBagConstraints();
		headerC.gridx = 0;
		headerC.gridy = 0;
		headerC.gridwidth = 4;
		headerC.anchor = GridBagConstraints.LAST_LINE_START;
		headerC.ipadx = 4;
		// editPanel.add(new JLabel("<html><small>String</small></html>"),
		// headerC);

		GridBagConstraints fieldC = new GridBagConstraints();
		fieldC.gridx = 0;
		fieldC.gridy = 1;
		fieldC.gridwidth = 4;
		textArea = new JTextArea(6, 30);
		editPanel.add(new JScrollPane(textArea), fieldC);

		GridBagConstraints buttonC = new GridBagConstraints();
		buttonC.gridy = 2;
		buttonC.gridx = 0;
		okButton = new JButton(okAction);
		editPanel.add(okButton, buttonC);
		buttonC.gridx = GridBagConstraints.RELATIVE;
		editButton = new JButton(editAction);
		editPanel.add(editButton, buttonC);
		removeButton = new JButton(removeAction);
		editPanel.add(removeButton, buttonC);

		editAction.setEnabled(false);
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				parentView.edit(getModel());
			}
		});
		// By default, a new view is not editable
		setFieldsEditable(true);
		add(editPanel);
	}

	@Override
	protected JComponent createModelView(String model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void placeViewComponent(JComponent view) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeViewComponent(JComponent view) {
		// TODO Auto-generated method stub

	}

	/**
	 * Check whether this view can lose the focus or not
	 */
	@Override
	public void setEdit(boolean editable) {
		if (!editable) {
			setStringFromField();
		}
		// Disable buttons and stuff
		setFieldsEditable(editable);
	}

	private void setStringFromField() {
		model.setString(textArea.getText());
	}

	private void setFieldsEditable(boolean editable) {
		textArea.setEditable(editable);
		editAction.setEnabled(!editable);
		okAction.setEnabled(editable);
		if (editable) {
			textArea.requestFocusInWindow();
		}
	}

	@Override
	protected void addModelView(String string) {
		textArea.setText(string);
	}

	@Override
	protected void removeModelView(String refModel) {
		textArea.setText("");
	}

	/**
	 * A Controller (in MVC terms) to set the view to editable and allow the
	 * user to enter a new value for the {@link String}
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class EditAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public EditAction() {
			super("Edit");
		}

		public void actionPerformed(ActionEvent e) {
			parentView.edit(getModel());
		}
	}

	/**
	 * After entering the value for the {@link String} the user clicks OK and
	 * this Controller is used to set the editable state
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class OKAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public OKAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				parentView.edit(null);
			} catch (IllegalStateException ex) {
				// Warning box already shown, won't do edit(null)
			}
		}
	}

	/**
	 * Remove this View and the associated Model
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class RemoveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RemoveAction() {
			super("Remove");
		}

		public void actionPerformed(ActionEvent e) {
			model.remove();
		}
	}

}
