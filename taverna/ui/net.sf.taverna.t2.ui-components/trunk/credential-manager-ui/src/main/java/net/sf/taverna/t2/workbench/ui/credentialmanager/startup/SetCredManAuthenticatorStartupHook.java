package net.sf.taverna.t2.workbench.ui.credentialmanager.startup;

import java.net.Authenticator;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;
import net.sf.taverna.t2.workbench.StartupSPI;

public class SetCredManAuthenticatorStartupHook implements StartupSPI {

	private CredentialManager credManager;

	public int positionHint() {
		return 50;
	}

	public boolean startup() {
		Authenticator.setDefault(new CredentialManagerAuthenticator(credManager));
		return true;
	}

	public void setCredentialManager(CredentialManager credManager){
		this.credManager = credManager;
	}

}
