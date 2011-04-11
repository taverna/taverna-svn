package uk.ac.manchester.cs.elico.utilities.configuration;

import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

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
