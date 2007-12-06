package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.List;

import org.apache.log4j.Logger;

public class SingletonListModel extends EntityListModel {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SingletonListModel.class);

	public SingletonListModel(int depthOfSingleton) {
		super(null);
		setDepth(depthOfSingleton + 1);
	}

	@Override
	public void addEntityModel(EntityModel entityModel) {
		synchronized (this) {
			if (!getEntityModels().isEmpty()) {
				throw new IllegalStateException(
						"Can't add more than one entity model");
			}
			super.addEntityModel(entityModel);
		}
	}

	public EntityModel getSingleton() {
		List<EntityModel> children = getEntityModels();
		if (children.isEmpty()) {
			return null;
		}
		return children.get(0);
	}

}
