/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;

/**
 * @author alanrw
 * 
 */
public class ComponentMenu extends AbstractMenu {
	public static final URI COMPONENT = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#component");

	public ComponentMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 950, COMPONENT, makeAction());
	}

	public static DummyAction makeAction() {
		DummyAction action = new DummyAction("Components");
		return action;
	}
}
