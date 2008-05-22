package net.sf.taverna.t2.ui.menu;

import java.net.URI;

public abstract class AbstractToolBar extends AbstractMenuItem {

	public AbstractToolBar(URI id) {
		super(MenuType.toolBar, null, id);
	}

}
