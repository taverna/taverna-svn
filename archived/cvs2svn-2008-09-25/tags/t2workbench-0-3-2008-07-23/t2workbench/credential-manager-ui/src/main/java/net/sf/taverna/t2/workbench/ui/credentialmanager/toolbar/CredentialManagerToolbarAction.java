package net.sf.taverna.t2.workbench.ui.credentialmanager.toolbar;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ui.credentialmanager.action.CredentialManagerAction;

public class CredentialManagerToolbarAction extends AbstractMenuAction{

	public CredentialManagerToolbarAction() {
		super(CredentialManagerToolbarSection.CREDENTIAL_MANAGER_TOOLBAR_SECTION,100,URI.create("http://taverna.sf.net/2008/t2workbench/toolbar#credentialManagerAction"));
	}

	@Override
	protected Action createAction() {
		return new CredentialManagerAction();
	}

}
