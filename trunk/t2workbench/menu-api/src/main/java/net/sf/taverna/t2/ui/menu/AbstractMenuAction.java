package net.sf.taverna.t2.ui.menu;

import java.net.URI;

import javax.swing.Action;

public abstract class AbstractMenuAction extends AbstractMenuItem {

	public AbstractMenuAction(URI parentId, int positionHint) {
		this(parentId, null, positionHint);
	}

	public AbstractMenuAction(URI parentId, URI id, int positionHint) {
		super(MenuType.action, parentId, id);
		this.positionHint = positionHint;
	}
	
	@Override
	public synchronized Action getAction() {
		if (action == null) {
			action = createAction();
		}
		return action;
	}

	protected abstract Action createAction();
}
