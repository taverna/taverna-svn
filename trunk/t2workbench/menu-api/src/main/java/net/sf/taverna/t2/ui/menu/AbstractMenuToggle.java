package net.sf.taverna.t2.ui.menu;

import java.net.URI;

import javax.swing.Action;

public abstract class AbstractMenuToggle extends AbstractMenuItem {

	public AbstractMenuToggle(URI parentId, int positionHint) {
		this(parentId, null, positionHint);
	}

	public AbstractMenuToggle(URI parentId, URI id, int positionHint) {
		super(MenuType.toggle, parentId, id);
		this.positionHint = positionHint;
	}

	@Override
	public abstract Action getAction();
	
}
