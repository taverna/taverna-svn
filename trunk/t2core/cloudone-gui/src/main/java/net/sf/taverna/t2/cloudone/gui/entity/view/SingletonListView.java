package net.sf.taverna.t2.cloudone.gui.entity.view;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.SingletonListModel;

import org.apache.log4j.Logger;

public class SingletonListView extends EntityListView {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SingletonListView.class);

	public SingletonListView(SingletonListModel singletonModel) {
		super(singletonModel, null);
	}
	
	@Override
	public void addEntityToModel(EntityModel entityModel) {
		super.addEntityToModel(entityModel);
		if (! getModel().getEntityModels().isEmpty()) {
			addSchemesPanel.setVisible(false);
		}			
	}

	@Override
	protected void removeViewComponent(JComponent view) {
		super.removeViewComponent(view);
		addSchemesPanel.setVisible(true);
	}
	
	@Override
	protected void setDefaultBorder() {
		// None
	}
	
	
}
