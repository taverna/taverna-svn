package net.sf.taverna.t2.workbench.ui.credentialmanager.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ui.credentialmanager.action.CredentialManagerAction;

public class CredentialManagerMenu extends AbstractMenuAction{

	public CredentialManagerMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),100);
	}

	@Override
	protected Action createAction() {
		return new CredentialManagerAction();
	}

}
