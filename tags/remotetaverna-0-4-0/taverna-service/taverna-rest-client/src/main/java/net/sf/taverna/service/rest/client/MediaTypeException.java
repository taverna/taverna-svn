package net.sf.taverna.service.rest.client;

import org.restlet.data.MediaType;

public class MediaTypeException extends RESTException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7276581116666598180L;

	private MediaType requested;

	private MediaType received;

	public MediaTypeException(MediaType requested, MediaType received) {
		super("Unexpected media type returned " + received + ", requested "
			+ requested);
		this.requested = requested;
		this.received = received;
	}

	public MediaType getReceived() {
		return received;
	}

	public MediaType getRequested() {
		return requested;
	}

}
