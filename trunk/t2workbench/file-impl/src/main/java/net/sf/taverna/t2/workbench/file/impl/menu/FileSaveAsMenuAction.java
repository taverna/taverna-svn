package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.FileSaveAction;

public class FileSaveAsMenuAction extends AbstractMenuAction {

	public FileSaveAsMenuAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpen"), 60);
	}

	@Override
	protected Action createAction() {
		return new FileSaveAction(false);
	}

	
}
