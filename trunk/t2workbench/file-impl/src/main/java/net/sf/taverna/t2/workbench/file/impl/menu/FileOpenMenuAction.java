package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.FileOpenAction;

public class FileOpenMenuAction extends AbstractMenuAction {

	public FileOpenMenuAction() {
		super(
				URI
						.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenSection"),
				URI
						.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpen"),
				10);
	}

	@Override
	protected Action createAction() {
		return new FileOpenAction();
	}

}
