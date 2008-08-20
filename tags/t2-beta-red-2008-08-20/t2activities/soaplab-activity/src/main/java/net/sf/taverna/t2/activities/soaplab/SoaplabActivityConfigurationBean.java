package net.sf.taverna.t2.activities.soaplab;


/**
 * A configuration bean specific to a Soaplab activity. In particular it provides details
 * about the Soaplab service endpoint and the polling settings.
 * 
 * @author David Withers
 */
public class SoaplabActivityConfigurationBean {

	private String endpoint = null;

	private int pollingInterval = 0;

	private double pollingBackoff = 1.0;

	private int pollingIntervalMax = 0;

	/**
	 * Returns the endpoint.
	 *
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Sets the endpoint.
	 *
	 * @param endpoint the new endpoint
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Returns the pollingInterval.
	 *
	 * @return the pollingInterval
	 */
	public int getPollingInterval() {
		return pollingInterval;
	}

	/**
	 * Sets the pollingInterval.
	 *
	 * @param pollingInterval the new pollingInterval
	 */
	public void setPollingInterval(int pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	/**
	 * Returns the pollingBackoff.
	 *
	 * @return the pollingBackoff
	 */
	public double getPollingBackoff() {
		return pollingBackoff;
	}

	/**
	 * Sets the pollingBackoff.
	 *
	 * @param pollingBackoff the new pollingBackoff
	 */
	public void setPollingBackoff(double pollingBackoff) {
		this.pollingBackoff = pollingBackoff;
	}

	/**
	 * Returns the pollingIntervalMax.
	 *
	 * @return the pollingIntervalMax
	 */
	public int getPollingIntervalMax() {
		return pollingIntervalMax;
	}

	/**
	 * Sets the pollingIntervalMax.
	 *
	 * @param pollingIntervalMax the new pollingIntervalMax
	 */
	public void setPollingIntervalMax(int pollingIntervalMax) {
		this.pollingIntervalMax = pollingIntervalMax;
	}
	
	

}
