package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModel;
import org.apache.log4j.Logger;

/**
 * The View (in MVC terms) for an {@link EntityList}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class EntityListView extends
		EntityView<EntityListModel, EntityModel, EntityListModelEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8759220294605298180L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EntityListView.class);

	private JPanel entityViews;

	private JButton removeButton;

	@SuppressWarnings("unchecked")
	private EntityView lastEditedView;

	protected CreateDataDocAction createDataDocAction;

	protected CreateListAction createListAction;

	protected CreateLiteralAction createLiteralAction;

	protected CreateStringAction createStringAction;

	protected JPanel addSchemesPanel;

	public EntityListView(EntityListModel entityListModel,
			EntityListView parentView) {
		super(entityListModel, parentView);
		initialise();
		// setBorder(BorderFactory.createLineBorder(Color.YELLOW));
	}

	public EntityListView(EntityListModel model) {
		this(model, null);
	}

	public void addEntityToModel(EntityModel entityModel) {
		getModel().addEntityModel(entityModel);
		try {
			// Make it editable
			edit(entityModel);
		} catch (IllegalStateException ex) {
			// Could not change editable, remove fresh model
			getModel().removeEntityModel(entityModel);
		}
	}

	/**
	 * Check whether the selected type of {@link EntityModel} can be added and if
	 * so add the appropriate {@link EntityView}
	 * 
	 * @param model
	 * @throws IllegalStateException
	 */
	@SuppressWarnings("unchecked")
	public void edit(EntityModel model) throws IllegalStateException {
		// Might return null, which is OK
		EntityView view = (EntityView) modelViews.get(model);
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
		entityViews.invalidate();

		revalidate();
	}

	private JPanel createAddSchemePanel() {

		JPanel addSchemesPanel = new JPanel();
		addSchemesPanel.setLayout(new GridBagLayout());
		// addSchemes.setOpaque(false);
		GridBagConstraints cLabel = new GridBagConstraints();
		cLabel.gridy = 0;
		// cLabel.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints cButton = new GridBagConstraints();
		cButton.gridy = 1;
		cButton.fill = GridBagConstraints.HORIZONTAL;
		cButton.anchor = GridBagConstraints.LINE_END;

		// JLabel headerLabel = new
		// JLabel("<html><strong>List</strong></html>");
		RemoveAction removeAction = new RemoveAction();

		removeButton = new JButton(removeAction);
		if (getModel().isRemovable()) {
			// addSchemes.add(headerLabel, cLabel);
			addSchemesPanel.add(removeButton, cLabel);
		}

		JLabel createLabel = new JLabel("Create:");
		addSchemesPanel.add(createLabel, cButton);
		cButton.anchor = GridBagConstraints.LINE_START;
		for (Action createAction : getCreateActions()) {
			JButton createButton = new JButton(createAction);
			addSchemesPanel.add(createButton, cButton);
		}
		return addSchemesPanel;
	}

	protected void setDefaultBorder() {
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "List",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
	}

	protected List<Action> getCreateActions() {
		List<Action> createActions = new ArrayList<Action>();

		// Note: on UNKNOWN_DEPTH we get all actions, otherwise either just the
		// list, or all the 0-depth ones (literal/data/string)
		int depth = getModel().getDepth();
		if (depth == EntityModel.UNKNOWN_DEPTH || depth > 1) {
			createActions.add(createListAction);
		}
		if (depth == EntityModel.UNKNOWN_DEPTH || depth == 1) {
			createActions.add(createDataDocAction);
			createActions.add(createLiteralAction);
			createActions.add(createStringAction);
		}
		return createActions;
	}

	protected void makeCreateActions() {
		createDataDocAction = new CreateDataDocAction(getModel());
		createListAction = new CreateListAction(getModel());
		createLiteralAction = new CreateLiteralAction(getModel());
		createStringAction = new CreateStringAction(getModel());
	}

	private JPanel createEntityViewsPanel() {
		JPanel views = new JPanel();
		// views.setBackground(new Color(255, 255, 255, 0));
		views.setLayout(new GridBagLayout());
		for (EntityModel model : getModel().getEntityModels()) {
			addModelView(model);
		}
		return views;
	}

	private void initialise() {
		makeCreateActions();
		setDefaultBorder();
		setLayout(new GridBagLayout());

		addSchemesPanel = createAddSchemePanel();
		GridBagConstraints schemeC = new GridBagConstraints();
		schemeC.gridx = 0;
		schemeC.gridy = 0;
		schemeC.gridwidth = 2;
		// schemeC.fill = GridBagConstraints.HORIZONTAL;
		schemeC.anchor = GridBagConstraints.FIRST_LINE_START;
		add(addSchemesPanel, schemeC);

		GridBagConstraints viewsC = new GridBagConstraints();
		viewsC.gridx = 1;
		viewsC.gridy = 1;
		viewsC.fill = GridBagConstraints.HORIZONTAL;
		viewsC.weighty = 0.1;
		viewsC.anchor = GridBagConstraints.FIRST_LINE_START;
		entityViews = createEntityViewsPanel();
		add(entityViews, viewsC);

		GridBagConstraints indentC = new GridBagConstraints();
		indentC.gridx = 0;
		indentC.gridy = 1;
		indentC.weighty = 0.1;
		indentC.fill = GridBagConstraints.VERTICAL;
		JPanel indent = new JPanel();
		// indent.setOpaque(false);
		// indent.setBackground(Color.CYAN);
		add(indent, indentC);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected EntityView createModelView(EntityModel model) {
		EntityView view;
		if (model instanceof DataDocumentModel) {
			view = new DataDocumentEditView((DataDocumentModel) model, this);
		} else if (model instanceof EntityListModel) {
			view = new EntityListView((EntityListModel) model, this);
		} else if (model instanceof LiteralModel) {
			view = new LiteralView((LiteralModel) model, this);
		} else if (model instanceof StringModel) {
			view = new StringView((StringModel) model, this);
		} else {
			// TODO: Strings and literals
			throw new IllegalArgumentException("Unsupported model type "
					+ model);
		}
		return view;
	}

	@Override
	protected void placeViewComponent(JComponent view) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		entityViews.add(view, c);
		entityViews.revalidate();
	}

	@Override
	protected void removeViewComponent(JComponent view) {
		entityViews.remove(view);
		if (lastEditedView == view) {
			lastEditedView = null;
		}
		entityViews.revalidate();
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link DataDocumentModel}
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateDataDocAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel entityListModel;

		public CreateDataDocAction(EntityListModel entityListModel) {
			super("Data Document");
			this.entityListModel = entityListModel;
		}

		public void actionPerformed(ActionEvent e) {
			DataDocumentModel dataDocModel = new DataDocumentModel(
					entityListModel);
			addEntityToModel(dataDocModel);
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link EntityList}
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateListAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel parentModel;

		public CreateListAction(EntityListModel parentModel) {
			super("List");
			this.parentModel = parentModel;
		}

		public void actionPerformed(ActionEvent e) {
			EntityListModel entityListModel = new EntityListModel(parentModel);
			addEntityToModel(entityListModel);
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link LiteralModel}
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class CreateLiteralAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private EntityListModel parentModel;

		public CreateLiteralAction(EntityListModel parentModel) {
			super("Literal");
		}

		public void actionPerformed(ActionEvent e) {
			LiteralModel literalModel = new LiteralModel(getModel());
			addEntityToModel(literalModel);
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link StringModel}
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class CreateStringAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel parentModel;

		public CreateStringAction(EntityListModel parentModel) {
			super("String");
			this.parentModel = parentModel;
		}

		public void actionPerformed(ActionEvent e) {
			StringModel stringModel = new StringModel(parentModel);
			addEntityToModel(stringModel);
		}
	}

	public class RemoveAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public RemoveAction() {
			super("Remove");

		}

		public void actionPerformed(ActionEvent e) {
			getModel().remove();
		}
	}

	@Override
	public void setEdit(boolean editable) {
		if (!editable) {
			edit(null);
		}
	}

}
