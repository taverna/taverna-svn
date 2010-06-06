/**
 * 
 */
package org.taverna.server.master.exceptions;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NoCreateHandler implements
		ExceptionMapper<NoCreateException> {
	@Override
	public Response toResponse(NoCreateException exn) {
		return Response.status(FORBIDDEN).entity(exn.getMessage()).build();
	}
}