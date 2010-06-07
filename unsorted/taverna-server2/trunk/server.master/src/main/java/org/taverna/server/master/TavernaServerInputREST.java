package org.taverna.server.master;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.cxf.jaxrs.ext.Description;
import org.taverna.server.master.exceptions.BadPropertyValueException;
import org.taverna.server.master.exceptions.BadStateChangeException;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoUpdateException;

/**
 * This represents how a Taverna Server workflow run's inputs looks to a RESTful
 * API.
 * 
 * @author Donal Fellows.
 */
@Path("/")
@Produces( { "application/xml", "application/json" })
@Consumes( { "application/xml", "application/json" })
@Description("This represents how a Taverna Server workflow run's inputs looks to a RESTful API.")
public interface TavernaServerInputREST {
	/**
	 * @return A description of the various URIs to inputs associated with a
	 *         workflow run.
	 */
	@GET
	@Description("Describe the sub-URIs of this resource.")
	public InputsDescriptor get(@Context UriInfo ui);

	/**
	 * @return The Baclava file that will supply all the inputs to the workflow
	 *         run, or empty to indicate that no such file is specified.
	 */
	@GET
	@Path("baclava")
	@Description("Gives the Baclava file describing the inputs, or empty if individual files are used.")
	public String getBaclavaFile();

	/**
	 * Set the Baclava file that will supply all the inputs to the workflow run.
	 * 
	 * @param filename
	 *            The filename to set.
	 * @param ui
	 *            About the URI used to access this resource.
	 * @return An HTTP response to the request.
	 * @throws NoUpdateException
	 *             If the user can't update the run.
	 * @throws BadStateChangeException
	 *             If the run is not Initialized.
	 * @throws FilesystemAccessException
	 *             If the filename starts with a <tt>/</tt> or if it contains a
	 *             <tt>..</tt> segment.
	 */
	@PUT
	@Path("baclava")
	@Description("Sets the Baclava file describing the inputs.")
	public Response setBaclavaFile(String filename, @Context UriInfo ui)
			throws NoUpdateException, BadStateChangeException,
			FilesystemAccessException;

	/**
	 * Get what input is set for the specific input.
	 * 
	 * @param name
	 *            The input to set.
	 * @return A description of the input.
	 * @throws BadPropertyValueException
	 *             If no input with that name exists.
	 */
	@GET
	@Path("input/{name}")
	@Description("Gives a description of what is used to supply a particular input.")
	public InDesc getInput(@PathParam("name") String name)
			throws BadPropertyValueException;

	/**
	 * Set what an input uses to provide data into the workflow run.
	 * 
	 * @param name
	 *            The name of the input.
	 * @param inputDescriptor
	 *            A description of the input
	 * @param ui
	 *            The HTTP context.
	 * @return A response to the HTTP request.
	 * @throws NoUpdateException
	 *             If the user can't update the run.
	 * @throws BadStateChangeException
	 *             If the run is not Initialized.
	 * @throws FilesystemAccessException
	 *             If a filename is being set and the filename starts with a
	 *             <tt>/</tt> or if it contains a <tt>..</tt> segment.
	 * @throws BadPropertyValueException
	 *             If no input with that name exists.
	 */
	@PUT
	@Path("input/{name}")
	@Description("Sets the source for a particular input port.")
	public Response setInput(@PathParam("name") String name,
			InDesc inputDescriptor, @Context UriInfo ui)
			throws NoUpdateException, BadStateChangeException,
			FilesystemAccessException, BadPropertyValueException;

	/**
	 * A description of the structure of inputs to a Taverna workflow run.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "TavernaRunInputs")
	public static class InputsDescriptor extends DescriptionElement {
		/**
		 * Where to find the overall Baclava document filename (if set).
		 */
		public Uri baclava;
		/**
		 * Where to find the details of inputs to particular ports (if set).
		 */
		public List<Uri> input;
	}

	/**
	 * The Details of a particular input port's value assignment.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "TavernaRunInput")
	public static class InDesc {
		/**
		 * The name of the port.
		 */
		@XmlAttribute(required=false)
		public String name;

		/**
		 * Either a filename or a literal string, used to provide input to a
		 * workflow port.
		 * 
		 * @author Donal Fellows
		 */
		@XmlType
		@XmlTransient
		static abstract class AbstractContents {
		};

		/**
		 * The name of a file that provides input to the port.
		 * 
		 * @author Donal Fellows
		 */
		@XmlType(name = "File")
		public static class File extends AbstractContents {
			/**
			 * The filename.
			 */
			@XmlValue
			public String file;
		}

		/**
		 * The the literal input to the port.
		 * 
		 * @author Donal Fellows
		 */
		@XmlType(name = "Value")
		public static class Value extends AbstractContents {
			/**
			 * The value.
			 */
			@XmlValue
			public String value;
		}

		@XmlElements( { @XmlElement(name = "file", type = File.class),
				@XmlElement(name = "value", type = Value.class) })
		public AbstractContents content;
	}
}
