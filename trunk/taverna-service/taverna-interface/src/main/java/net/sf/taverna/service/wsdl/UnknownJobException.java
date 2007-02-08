package net.sf.taverna.service.wsdl;

public class UnknownJobException extends TavernaException {

	private String jobID;

	public UnknownJobException(String jobID) {
		super(jobID);
		this.jobID = jobID;
	}

	public String getJobID() {
		return jobID;
	}
}
