/**
 * 
 */
package org.taverna.server.master.exceptions;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NoUpdateHandler implements
		ExceptionMapper<NoUpdateException> {
	@Override
	public Response toResponse(NoUpdateException exn) {
		return Response.status(FORBIDDEN).entity(exn.getMessage()).build();
	}
}