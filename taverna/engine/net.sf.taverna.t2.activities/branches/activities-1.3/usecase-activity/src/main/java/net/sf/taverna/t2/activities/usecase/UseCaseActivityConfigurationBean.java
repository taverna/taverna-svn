/**
 * 
 */
package net.sf.taverna.t2.activities.usecase;

/**
 * @author alanrw
 *
 */
public class UseCaseActivityConfigurationBean {
	
	private String repositoryUrl;
	private String usecaseid;
	/**
	 * @return the repositoryUrl
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}
	/**
	 * @param repositoryUrl the repositoryUrl to set
	 */
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
	/**
	 * @return the usecaseid
	 */
	public String getUsecaseid() {
		return usecaseid;
	}
	/**
	 * @param usecaseid the usecaseid to set
	 */
	public void setUsecaseid(String usecaseid) {
		this.usecaseid = usecaseid;
	}

}
