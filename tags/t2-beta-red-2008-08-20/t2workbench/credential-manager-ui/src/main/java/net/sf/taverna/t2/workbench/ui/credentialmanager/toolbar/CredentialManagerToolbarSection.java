package net.sf.taverna.t2.workbench.ui.credentialmanager.toolbar;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;

public class CredentialManagerToolbarSection extends AbstractMenuSection {

	public static URI CREDENTIAL_MANAGER_TOOLBAR_SECTION = URI.create("http://taverna.sf.net/2008/t2workbench/toolbar#credentialManagerSection");
	
	public CredentialManagerToolbarSection() {
		super(DefaultToolBar.DEFAULT_TOOL_BAR, 100, CREDENTIAL_MANAGER_TOOLBAR_SECTION);
	}

}
