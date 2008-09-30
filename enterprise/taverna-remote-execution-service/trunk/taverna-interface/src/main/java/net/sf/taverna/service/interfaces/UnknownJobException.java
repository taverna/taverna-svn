package net.sf.taverna.service.interfaces;

public class UnknownJobException extends TavernaException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6727089879679063124L;
	private String jobID;

	public UnknownJobException(String jobID) {
		super(jobID);
		this.jobID = jobID;
	}

	public String getJobID() {
		return jobID;
	}
}
