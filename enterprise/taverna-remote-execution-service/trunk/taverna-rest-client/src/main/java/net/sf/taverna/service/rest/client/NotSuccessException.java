package net.sf.taverna.service.rest.client;

import org.restlet.data.Status;

public class NotSuccessException extends RESTException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -594509501212057625L;
	private Status status;

	public NotSuccessException(Status status) {
		super(status.toString());
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

}
