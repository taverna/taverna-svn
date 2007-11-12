package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.log4j.Logger;

public class EntityListView extends
		EntityView<EntityListModel, EntityModel, EntityListModelEvent> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EntityListView.class);

	private JButton dataDocButton;

	private JPanel entityViews;

	@SuppressWarnings("unchecked")
	private EntityView lastEditedView;

	private JButton listButton;

	public EntityListView(EntityListModel entityListModel) {
		super(entityListModel);
		initialise();
	}

	public void addEntityToModel(EntityModel entityModel) {
		getParentModel().addEntityModel(entityModel);
	}

	private JPanel createSchemeButtons() {
		JPanel addSchemes = new JPanel();
		addSchemes.setLayout(new GridBagLayout());
		GridBagConstraints cLabel = new GridBagConstraints();
		cLabel.gridx = 0;
		cLabel.weightx = 0.1;
		cLabel.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints cButton = new GridBagConstraints();
		cButton.gridx = 1;
		cButton.fill = GridBagConstraints.HORIZONTAL;

		JLabel dataDocLabel = new JLabel("Data Document");
		JLabel listLabel = new JLabel("List");
		CreatDataDocAction createDataDocAction = new CreatDataDocAction(
				getParentModel());
		CreateListAction createListAction = new CreateListAction(
				getParentModel());
		dataDocButton = new JButton(createDataDocAction);
		listButton = new JButton(createListAction);

		addSchemes.add(dataDocLabel, cLabel);
		addSchemes.add(dataDocButton, cButton);
		addSchemes.add(listLabel, cLabel);
		addSchemes.add(listButton, cButton);
		return addSchemes;
	}

	private JPanel createEntityViewsPanel() {
		JPanel views = new JPanel();
		views.setLayout(new GridBagLayout());
		for (EntityModel model : getParentModel().getEntityModels()) {
			addModelView(model);
		}
		return views;
	}

	private void initialise() {
		setLayout(new GridBagLayout());

		JPanel addSchemes = createSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		add(addSchemes, outerConstraint);

		entityViews = createEntityViewsPanel();
		add(entityViews, outerConstraint);

		outerConstraint.gridx = 1;
		outerConstraint.gridy = 100;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		// filler.setBorder(BorderFactory.createEtchedBorder());
		add(filler, outerConstraint);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected EntityView createModelView(EntityModel model) {
		EntityView view;
		if (model instanceof DataDocumentModel) {
			view = new DataDocumentEditView((DataDocumentModel) model);
		} else if (model instanceof EntityListModel) {
			view = new EntityListView((EntityListModel) model);
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
		// indent?
		c.gridx = 0;
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

}
