package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.Component;
import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.impl.WorkbenchPerspectives;

public class DisplayPerspectivesMenu extends AbstractMenuCustom {

	public DisplayPerspectivesMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
			30,
			URI.create("http://taverna.sf.net/2008/t2workbench/menu#displayPerspectives"));
	}
	

	@Override
	protected Component createCustomComponent() {
		WorkbenchPerspectives perspectives = Workbench.getInstance().getPerspectives();
		return perspectives.getDisplayPerspectivesMenu();
	}
 
}
