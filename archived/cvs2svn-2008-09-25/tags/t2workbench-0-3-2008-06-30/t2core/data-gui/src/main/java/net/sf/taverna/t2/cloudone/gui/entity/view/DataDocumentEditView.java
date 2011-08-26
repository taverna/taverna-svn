package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.FileRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.log4j.Logger;

/**
 * View (in MVC terms) for a Data Document builder GUI. This is the editable
 * version (a button in the gui toggles editable status on or off), the
 * non-editable is {@link DataDocumentView}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 */
@SuppressWarnings("unchecked")
public class DataDocumentEditView
		extends
		EntityView<DataDocumentModel, ReferenceSchemeModel, DataDocumentModelEvent> {

	private static Logger logger = Logger.getLogger(DataDocumentEditView.class);
	private static final long serialVersionUID = 1L;

	private JPanel actionPanel;

	private JButton editButton;

	private JButton fileButton;

	private JButton httpButton;

	private JButton removeButton;

	/** What {@link RefSchemeView} was the last one to be edited */
	private RefSchemeView lastEditedView;
	private DataDocumentView nonEditPanel;
	private JPanel outerPanel;
	private JPanel refViews;

	/**
	 * Constructor creates a {@link net.sf.taverna.t2.cloudone.gui.entity.view.ModelObserver} 
	 * and registers it as an observer with the {@link DataDocumentEditView} and calls
	 * {@link #initialiseGui()} to create the GUI.
	 * 
	 * @param model
	 *            the {@link DataDocumentModel} model part in
	 *            Model-View-Controller terms
	 */
	public DataDocumentEditView(DataDocumentModel model,
			EntityListView parentView) {
		super(model, parentView);
		initialiseGui();
	}

	/**
	 * Resets buttons/text fields etc to their correct editing states eg, if OK
	 * is clicked and the input is valid then only the Edit & Remove buttons
	 * should be enabled
	 * 
	 * @param model
	 *            the {@link ReferenceSchemeModel} whose state has changed
	 * @throws IllegalStateException
	 *             If the current editable could not set it's fields to the
	 *             model
	 */
	public void edit(ReferenceSchemeModel model) throws IllegalStateException {
		// Might return null, which is OK
		RefSchemeView view = (RefSchemeView) modelViews.get(model);
		if (lastEditedView != null && lastEditedView != view) {
			try {
				lastEditedView.setEdit(false);
			} catch (IllegalStateException ex) {
				throw new IllegalStateException(ex);
			}
		}
		lastEditedView = view;
		if (view != null) {
			view.setEdit(true);
		}
	}

	private JPanel createdRefs() {
		JPanel refs = new JPanel();
		refs.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		// TODO: Add existing from model
		// model.getReferenceSchemeModels();
		return refs;
	}

	private void initialiseGui() {
		setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Data Document",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		// panel inside panel for on-click activation
		setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.BLUE));
		// setBackground(new Color(180, 240, 160, 64));

		// JPanel addSchemes = addSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		// add to outer panel first then the parent?

		outerPanel = outerPanel();
		add(outerPanel, outerConstraint);
		// add(addSchemes, outerConstraint);

		nonEditPanel = createNonEditView();
		add(nonEditPanel, outerConstraint);

		refViews = createdRefs();
		add(refViews, outerConstraint);

		outerConstraint.gridx = 1;
		outerConstraint.gridy = 100;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		// filler.setOpaque(false);
		// filler.setBorder(BorderFactory.createEtchedBorder());
		add(filler, outerConstraint);
		// panel which is switched in/out on edit button click
	}

	private JPanel outerPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;

		// JLabel header = new JLabel("<html><strong>Data
		// Document</strong></html>");
		// panel.add(header, c);

		c.gridy = 1;

		RemoveAction removeAction = new RemoveAction();
		removeButton = new JButton(removeAction);
		panel.add(removeButton, c);

		// outerPanel.setOpaque(false);
		EditPanelAction editPanelAction = new EditPanelAction();
		editButton = new JButton(editPanelAction);
		editButton.setText("Edit");
		// editButton.setOpaque(false);
		panel.add(editButton, c);

		actionPanel = addSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		// does this need a grid bag constraint?
		panel.add(actionPanel, c);
		c.gridy = 2;
		actionPanel.setVisible(false);
		return panel;
	}

	private DataDocumentView createNonEditView() {
		DataDocumentView nonEditView = new DataDocumentView(getModel());
		nonEditView.setVisible(false);
		return nonEditView;
	}

	protected void addRefToModel(ReferenceSchemeModel refModel) {
		getModel().addReferenceScheme(refModel);
		try {
			// Make it editable
			edit(refModel);
		} catch (IllegalStateException ex) {
			// Could not change editable, remove fresh model
			getModel().removeReferenceScheme(refModel);
		}
	}

	protected JPanel addSchemeButtons() {
		JPanel addSchemes = new JPanel();
		addSchemes.setLayout(new GridBagLayout());

		GridBagConstraints cButton = new GridBagConstraints();
		cButton.fill = GridBagConstraints.HORIZONTAL;

		CreateHttpAction createHttpAction = new CreateHttpAction(getModel());
		CreateFileAction createFileAction = new CreateFileAction(getModel());
		httpButton = new JButton(createHttpAction);
		fileButton = new JButton(createFileAction);

		addSchemes.add(httpButton, cButton);
		addSchemes.add(fileButton, cButton);
		return addSchemes;
	}

	@Override
	protected JComponent createModelView(ReferenceSchemeModel refModel) {
		RefSchemeView refView = null;
		if (refModel instanceof HttpRefSchemeModel) {
			refView = new HttpRefSchemeView((HttpRefSchemeModel) refModel, this);
		} else if (refModel instanceof FileRefSchemeModel) {
			refView = new FileRefSchemeView((FileRefSchemeModel) refModel, this);
		} else {
			logger.warn("Unsupported reference model " + refModel);
		}
		return refView;
	}

	@Override
	protected void placeViewComponent(JComponent view) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		refViews.add(view, c);
		refViews.revalidate();

	}

	@Override
	protected void removeViewComponent(JComponent view) {
		if (view != null) {
			refViews.remove(view);
		}
		if (lastEditedView == view) {
			lastEditedView = null;
		}
		refViews.revalidate();

	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link FileReferenceScheme} via clicking the appropriate button
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateFileAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private DataDocumentModel dataDocModel;

		public CreateFileAction(DataDocumentModel dataDocModel) {
			super("Create file reference");
			this.dataDocModel = dataDocModel;
		}

		public void actionPerformed(ActionEvent e) {
			FileRefSchemeModel refModel = new FileRefSchemeModel(dataDocModel);
			addRefToModel(refModel);
			if (refModel.getFile() == null) {
				// User cancelled, remove it
				refModel.remove();
			}
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link HttpReferenceScheme} via clicking the appropriate button
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateHttpAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private DataDocumentModel dataDocModel;

		public CreateHttpAction(DataDocumentModel dataDocModel) {
			super("Create HTTP reference");
			this.dataDocModel = dataDocModel;
		}

		public void actionPerformed(ActionEvent e) {
			HttpRefSchemeModel refModel = new HttpRefSchemeModel(dataDocModel);
			addRefToModel(refModel);
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for toggling the editable
	 * status
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class EditPanelAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public EditPanelAction() {
			super("Edit Data Document");

		}

		public void actionPerformed(ActionEvent e) {
			boolean editable = editButton.getText().equalsIgnoreCase("Edit");
			// FIXME: Check editable state instead of button text
			if (getParentView() == null) {
				setEdit(editable);
			} else {
				if (editable) {
					getParentView().edit(getModel());
				} else {
					getParentView().edit(null);
				}
			}
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for removing the model &
	 * view from the parent
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
			getModel().remove();
		}
	}

	/**
	 * Set whether the panel can be edited or not. Used to check whether the
	 * view can lose the focus in-case any required information has not been
	 * filled in
	 */
	@Override
	public void setEdit(boolean editable) {
		if (!editable) {
			edit(null);
			// read-only
			actionPanel.setVisible(false);
			refViews.setVisible(false);
			nonEditPanel.setVisible(true);
			editButton.setText("Edit");
		} else {
			actionPanel.setVisible(true);
			refViews.setVisible(true);
			nonEditPanel.setVisible(false);
			editButton.setText("Hide");
		}
		revalidate();

	}

}
