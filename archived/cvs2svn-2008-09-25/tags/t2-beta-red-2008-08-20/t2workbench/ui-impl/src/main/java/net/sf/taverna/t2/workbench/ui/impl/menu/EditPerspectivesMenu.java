package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.Component;
import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.impl.WorkbenchPerspectives;

public class EditPerspectivesMenu extends AbstractMenuCustom {
	public EditPerspectivesMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
			50,
			URI.create("http://taverna.sf.net/2008/t2workbench/menu#editPerspectives"));
	}

	@Override
	protected Component createCustomComponent() {
		WorkbenchPerspectives perspectives = Workbench.getInstance().getPerspectives();
		return perspectives.getEditPerspectivesMenu();
	}
	
	
}
