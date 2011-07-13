package uk.ac.manchester.cs.elico.utilities.configuration;

import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class RapidAnalyticsPreferences {

	private UsernamePassword usernamePassword = new UsernamePassword();
	private String repositoryLocation;

    public String getPathToTmpDir() {
        return pathToTmpDir;
    }

    public void setPathToTmpDir(String pathToTmpDir) {
        this.pathToTmpDir = pathToTmpDir;
    }

    public String getPathToFlora() {
        return pathToFlora;
    }

    public void setPathToFlora(String pathToFlora) {
        this.pathToFlora = pathToFlora;
    }

    private String pathToTmpDir;
    private String pathToFlora;

	
	// getters
	
	public String getUsername() {
		return usernamePassword.getUsername();
	}
	
	public char[] getPassword() {
		return usernamePassword.getPassword();
	}

    public String getPasswordAsString() {
        return String.valueOf(usernamePassword.getPassword());
    }

	public UsernamePassword getUsernamePasswordObject() {
		return usernamePassword;
	}
	
	public String getRepositoryLocation() {
		return repositoryLocation;
	}
	
	// setters
	public void setUsername(String user) {
		usernamePassword.setUsername(user);
	}
	
	public void setPassword(String pass) {
		usernamePassword.setPassword(pass.toCharArray());
	}
	
	public void setRepositoryLocation(String location) {
		repositoryLocation = location;
	}

	public String getExecutorServiceWSDL() {

        return repositoryLocation + "/e-LICO/ExecutorService?wsdl";
	}

    public String getExecutorServiceLocation() {

        return repositoryLocation + "/e-LICO/ExecutorService";
    }


	public String getBrowserServiceLocation() {

        return repositoryLocation + "/RAWS/resources/";
	}

    public String getMetaDataServiceLocation() {

        return repositoryLocation + "/e-LICO/MetaDataService";
    }


}
