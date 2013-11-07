/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.registry.standard;

import java.net.URI;
import java.security.cert.X509Certificate;

import net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

/**
 * Provides passwords for a test server.
 *
 * @author David Withers
 */
public class TestPasswordProvider implements CredentialProviderSPI {

	@Override
	public boolean canHandleTrustConfirmation(X509Certificate[] arg0) {
		return false;
	}

	@Override
	public boolean canProvideJavaTruststorePassword() {
		return false;
	}

	@Override
	public boolean canProvideMasterPassword() {
		return true;
	}

	@Override
	public boolean canProvideUsernamePassword(URI arg0) {
		return true;
	}

	@Override
	public String getJavaTruststorePassword() {
		return null;
	}

	@Override
	public String getMasterPassword(boolean arg0) {
		return "";
	}

	@Override
	public int getProviderPriority() {
		return 0;
	}

	@Override
	public UsernamePassword getUsernamePassword(URI arg0, String arg1) {
		return new UsernamePassword("test", "zxzxzx");
	}

	@Override
	public TrustConfirmation shouldTrust(X509Certificate[] arg0) {
		return null;
	}

}
