package net.sf.taverna.t2.ui.menu;

import java.net.URI;

import javax.swing.Action;

public abstract class AbstractMenu extends AbstractMenuItem {

	public AbstractMenu(URI id) {
		super(MenuType.menu, (URI) null, id);
	}

	public AbstractMenu(URI parentId, int positionHint, URI id, String label) {
		this(parentId, positionHint, id, new DummyAction(label));
	}

	public AbstractMenu(URI parentId, int positionHint, URI id,
			Action action) {
		super(MenuType.menu, parentId, id);
		this.action = action;
		this.positionHint = positionHint;
	}

}
