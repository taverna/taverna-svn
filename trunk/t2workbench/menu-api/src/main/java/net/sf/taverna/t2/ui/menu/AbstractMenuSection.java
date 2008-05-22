package net.sf.taverna.t2.ui.menu;

import java.net.URI;

public abstract class AbstractMenuSection extends AbstractMenuItem {

	public AbstractMenuSection(URI parentId, int positionHint, URI id) {
		super(MenuType.section, parentId, id);
		this.positionHint = positionHint;
	}

}
