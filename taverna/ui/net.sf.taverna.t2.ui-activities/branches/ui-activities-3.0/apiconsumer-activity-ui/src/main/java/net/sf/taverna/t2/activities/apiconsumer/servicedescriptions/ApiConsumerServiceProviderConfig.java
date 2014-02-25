/**
 * 
 */
package net.sf.taverna.t2.activities.apiconsumer.servicedescriptions;

/**
 * @author alanrw
 *
 */
public class ApiConsumerServiceProviderConfig {
	
	private String absolutePath;
	
	public ApiConsumerServiceProviderConfig(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public ApiConsumerServiceProviderConfig() {
	}

	/**
	 * @return the absolutePath
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}

}
