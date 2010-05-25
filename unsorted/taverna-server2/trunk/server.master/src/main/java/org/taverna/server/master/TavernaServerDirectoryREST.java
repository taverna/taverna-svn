package org.taverna.server.master;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoUpdateException;

/**
 * Representation of how a workflow run's working directory tree looks.
 * 
 * @author Donal Fellows
 */
@Produces( { "application/xml", "application/json" })
@Consumes( { "application/xml", "application/json" })
public interface TavernaServerDirectoryREST {
	/**
	 * Get the working directory of the workflow run.
	 * 
	 * @param ui
	 *            About how this method was called.
	 * @return A description of the working directory.
	 */
	@GET
	public DirEntryReference getDescription(@Context UriInfo ui);

	/**
	 * Gets a description of the named entity in or beneath the working
	 * directory of the workflow run, which may be either a {@link Directory} or
	 * a {@link File}.
	 * 
	 * @param path
	 *            The path to the thing to describe.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response containing a description of the named thing.
	 * @throws FilesystemAccessException
	 *             If something went wrong during the filesystem operation.
	 */
	@GET
	@Path("{path:.*}")
	public Response getDirectoryOrFileContents(
			@PathParam("path") List<PathSegment> path, @Context UriInfo ui)
			throws FilesystemAccessException;

	/**
	 * Creates a directory in the filesystem beneath the working directory of
	 * the workflow run.
	 * 
	 * @param parent
	 *            The directory to create the directory in.
	 * @param name
	 *            What to call the directory to create.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response indicating where the directory was actually
	 *         made.
	 * @throws NoUpdateException
	 *             If the user is not permitted to update the run.
	 * @throws FilesystemAccessException
	 *             If something went wrong during the filesystem operation.
	 */
	@POST
	@Path("{path:.*}")
	@Consumes("text/plain")
	public Response makeDirectory(@PathParam("path") List<PathSegment> parent,
			String name, @Context UriInfo ui) throws NoUpdateException,
			FilesystemAccessException;

	/**
	 * Creates or updates a file's contents, where that file is in or below the
	 * working directory of a workflow run.
	 * 
	 * @param parent
	 *            The directory holding the file.
	 * @param name
	 *            What to call the file to create or update.
	 * @param contents
	 *            What to put in the file.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response to the method.
	 * @throws NoUpdateException
	 *             If the user is not permitted to update the run.
	 * @throws FilesystemAccessException
	 *             If something went wrong during the filesystem operation.
	 */
	@PUT
	@Path("{path:.*}/{name}")
	@Consumes("application/octet-stream")
	public Response makeOrUpdateFile(
			@PathParam("path") List<PathSegment> parent,
			@PathParam("name") String name, byte[] contents, @Context UriInfo ui)
			throws NoUpdateException, FilesystemAccessException;

	/**
	 * Deletes a file or directory that is in or below the working directory of
	 * a workflow run.
	 * 
	 * @param path
	 *            The path to the file or directory.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response to the method.
	 * @throws NoUpdateException
	 *             If the user is not permitted to update the run.
	 * @throws FilesystemAccessException
	 *             If something went wrong during the filesystem operation.
	 */
	@DELETE
	@Path("{path:.*}")
	public Response destroyDirectoryEntry(
			@PathParam("path") List<PathSegment> path, @Context UriInfo ui)
			throws NoUpdateException, FilesystemAccessException;
}
