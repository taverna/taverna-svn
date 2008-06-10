package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.FileSaveAction;
import net.sf.taverna.t2.workbench.file.impl.actions.FileSaveAsAction;

public class FileSaveAsMenuAction extends AbstractMenuAction {

	public FileSaveAsMenuAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenSection"), 60);
	}

	@Override
	protected Action createAction() {
		return new FileSaveAsAction();
	}

	
}
