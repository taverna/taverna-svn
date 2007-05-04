package net.sf.taverna.service.rest.client;

import org.restlet.data.Status;

public class NotSuccessException extends Exception {

	private Status status;

	public NotSuccessException(Status status) {
		super(status.toString());
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

}
