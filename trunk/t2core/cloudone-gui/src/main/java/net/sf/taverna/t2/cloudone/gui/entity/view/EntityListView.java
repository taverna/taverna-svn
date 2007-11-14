package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModel;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.log4j.Logger;

public class EntityListView extends
		EntityView<EntityListModel, EntityModel, EntityListModelEvent> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EntityListView.class);

	private JButton dataDocButton;

	private JPanel entityViews;
	
	private JButton literalButton;

	@SuppressWarnings("unchecked")
	private EntityView lastEditedView;

	private JButton listButton;

	private JButton stringButton;


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
		System.out.println("add");
		try {
			// Make it editable
			edit(entityModel);
		} catch (IllegalStateException ex) {
			// Could not change editable, remove fresh model
			getModel().removeEntityModel(entityModel);
		}
	}

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

	private JPanel createSchemeButtons() {
		JPanel addSchemes = new JPanel();
		addSchemes.setLayout(new GridBagLayout());
		// addSchemes.setOpaque(false);
		GridBagConstraints cLabel = new GridBagConstraints();
		cLabel.gridx = 0;
		cLabel.weightx = 0.1;
		cLabel.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints cButton = new GridBagConstraints();
		cButton.gridx = 1;
		cButton.fill = GridBagConstraints.HORIZONTAL;

		JLabel dataDocLabel = new JLabel("Data Document");
		JLabel listLabel = new JLabel("List");
		JLabel literalLabel = new JLabel("Literal");
		JLabel stringLabel = new JLabel("String");
		CreatDataDocAction createDataDocAction = new CreatDataDocAction(
				getModel());
		CreateListAction createListAction = new CreateListAction(
				getModel());
		CreateLiteralAction createLiteralAction = new CreateLiteralAction(getModel());
		CreateStringAction createStringAction = new CreateStringAction(getModel());
		dataDocButton = new JButton(createDataDocAction);
		// dataDocButton.setOpaque(false);
		listButton = new JButton(createListAction);
		// listButton.setOpaque(false);
		literalButton = new JButton(createLiteralAction);
		stringButton = new JButton(createStringAction);

		addSchemes.add(dataDocLabel, cLabel);
		addSchemes.add(dataDocButton, cButton);
		addSchemes.add(listLabel, cLabel);
		addSchemes.add(listButton, cButton);
		addSchemes.add(literalLabel, cLabel);
		addSchemes.add(literalButton, cButton);
		addSchemes.add(stringLabel, cLabel);
		addSchemes.add(stringButton, cButton);
		return addSchemes;
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
		setLayout(new GridBagLayout());

		JPanel addSchemes = createSchemeButtons();
		GridBagConstraints schemeC = new GridBagConstraints();
		schemeC.gridx = 0;
		schemeC.gridy = 0;
		schemeC.gridwidth = 2;
		// schemeC.fill = GridBagConstraints.HORIZONTAL;
		schemeC.anchor = GridBagConstraints.FIRST_LINE_START;
		add(addSchemes, schemeC);

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
		} else if (model instanceof StringModel){
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
		System.out.println("removing");
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link HttpReferenceScheme} via clicking the appropriate button
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreatDataDocAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel entityListModel;

		public CreatDataDocAction(EntityListModel entityListModel) {
			super("Create Data Document");
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
	 * {@link FileReferenceScheme} via clicking the appropriate button
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateListAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel parentModel;

		public CreateListAction(EntityListModel parentModel) {
			super("Create List");
			this.parentModel = parentModel;
		}

		public void actionPerformed(ActionEvent e) {
			EntityListModel entityListModel = new EntityListModel(parentModel);
			addEntityToModel(entityListModel);
		}
	}
	
	public class CreateLiteralAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel parentModel;

		public CreateLiteralAction(EntityListModel parentModel) {
			super("Create Literal");
		}

		public void actionPerformed(ActionEvent e) {
			LiteralModel literalModel = new LiteralModel(getModel());
			System.out.println("hello");
			addEntityToModel(literalModel);
		}
	}
	
	public class CreateStringAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private EntityListModel parentModel;

		public CreateStringAction(EntityListModel parentModel) {
			super("Create String");
			this.parentModel = parentModel;
		}

		public void actionPerformed(ActionEvent e) {
			StringModel stringModel = new StringModel(parentModel);
			addEntityToModel(stringModel);
		}
	}

	@Override
	public void setEdit(boolean editable) {
		if (! editable) {
			System.out.println("aSD");
			edit(null);
		}
	}

}
