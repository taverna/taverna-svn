/**
 * 
 */
package org.taverna.server.master.exceptions;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class BadStateChangeHandler implements
		ExceptionMapper<BadStateChangeException> {
	@Override
	public Response toResponse(BadStateChangeException exn) {
		return Response.status(FORBIDDEN).entity(exn.getMessage()).build();
	}
}