package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

public class RapidAnalyticsPreferences {

	private UsernamePassword usernamePassword = new UsernamePassword();
	private String repositoryLocation = new String();
	
	// getters
	
	public String getUsername() {
		return usernamePassword.getUsername();
	}
	
	public String getPassword() {
		return usernamePassword.getPasswordAsString();
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

	public String getExecutorServiceLocation() {
	
		String newLocation = repositoryLocation + "/e-LICO/ExecutorService?wsdl";
		
		return newLocation;
	}
	
	public String getBrowserServiceLocation() {
		
		String newLocation = repositoryLocation + "/RAWS/resources/";
		
		return newLocation;
	}
}
