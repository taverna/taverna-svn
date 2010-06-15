/**
 * 
 */
package org.taverna.server.master.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.taverna.server.master.exceptions.UnknownRunException;

@Provider
public class UnknownRunHandler implements ExceptionMapper<UnknownRunException> {
	@Override
	public Response toResponse(UnknownRunException exn) {
		return Response.status(FORBIDDEN).type(TEXT_PLAIN_TYPE).entity(
				exn.getMessage()).build();
	}
}