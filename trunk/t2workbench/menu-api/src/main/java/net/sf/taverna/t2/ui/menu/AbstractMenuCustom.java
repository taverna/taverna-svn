package net.sf.taverna.t2.ui.menu;

import java.awt.Component;
import java.net.URI;

public abstract class AbstractMenuCustom extends AbstractMenuItem {

	public AbstractMenuCustom(URI parentId, int positionHint) {
		this(parentId, positionHint, null);
	}

	public AbstractMenuCustom(URI parentId, int positionHint, URI id) {
		super(MenuType.custom, parentId, id);
		this.positionHint = positionHint;
	}

	protected abstract Component createCustomComponent();

	@Override
	public final synchronized Component getCustomComponent() {
		if (customComponent == null) {
			customComponent = createCustomComponent();
		}
		return customComponent;
	}

}