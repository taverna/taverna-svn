package net.sf.taverna.t2.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

public abstract class AbstractMenuItem implements MenuComponent {

	public static class DummyAction extends AbstractAction {
		public DummyAction(String name) {
			super(name);
		}

		public DummyAction(String name, Icon icon) {
			super(name, icon);
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	public AbstractMenuItem(MenuType type, URI parentId, URI id) {
		this.type = type;
		this.parentId = parentId;
		this.id = id;
	}

	private final MenuType type;
	private final URI parentId;
	private final URI id;
	protected int positionHint = 100;
	protected Action action;
	protected Component customComponent;

	public Action getAction() {
		return action;
	}

	public Component getCustomComponent() {
		return customComponent;
	}

	public URI getId() {
		return id;
	}

	public URI getParentId() {
		return parentId;
	}

	public int getPositionHint() {
		return positionHint;
	}

	public MenuType getType() {
		return type;
	}

}
