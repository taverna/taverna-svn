/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.io.Serializable;

/**
 * An abstract superclass of all processor ports
 * 
 * @author Tom Oinn
 */
public abstract class Port implements Serializable {

	private String name = "";

	private Processor processor = null;

	private String syntacticType = "";

	private SemanticMarkup metadata = null;

	/**
	 * Create a new port (obviously you can't actually construct this because
	 * it's abstract. Names should match [a-zA-Z_0-9].
	 */
	public Port(Processor processor, String name)
			throws DuplicatePortNameException, PortCreationException {
		// Create a new metadata holder
		metadata = new PortSemanticMarkup(this);
		// Check we have no nulls
		if (processor == null) {
			throw new PortCreationException(
					"Invalid call to create a port, the processor was null!");
		}
		if (name == null) {
			throw new PortCreationException(
					"Invalid call to create a port, the name was null!");
		}
		if (name.equals("")) {
			throw new PortCreationException(
					"Refusing to create a port with name ''");
		}
		// Commented out - was failing with certain soaplab EMBOSS services
		// which had names like foo-2
		// tmo 30th May 2003
		/**
		 * if (Pattern.matches("\\w++",name) == false) { throw new
		 * PortCreationException("Name contains an invalid character,\n"+ "names
		 * must match [a-zA-Z_0-9]."); }
		 */
		// In the XScufl links to/from input/outputports are just named as
		// "myport", while links with ports within a processor are named as
		// "myproc:myport". By allowing ":" in the name could add an input port
		// called "myproc:myport" and create havoc.
		if (name.indexOf(":") > -1) {
			throw new PortCreationException("Illegal port name " + name);			
		}

		// Scan through the list of ports defined within
		// the parent processor and check that the name
		// isn't a duplicate.
		Port[] the_ports = processor.getPorts();
		for (int i = 0; i < the_ports.length; i++) {
			String existing_port_name = the_ports[i].getName();
			if (existing_port_name.equalsIgnoreCase(name)) {
				if (this.getClass().toString().equals(
						the_ports[i].getClass().toString())) {
					throw new DuplicatePortNameException(
							"Cannot create duplicate port name, was attempting to create '"
									+ name
									+ "', but it already exists in processor '"
									+ processor.getName() + "'.");
				}
			}
		}
		// Assign internal private members
		this.processor = processor;
		// this.name = name.toLowerCase(); //this causes problems with wsdl
		// invocations
		this.name = name;
		fireModelEvent(new ScuflModelAddEvent(processor, this));
	}

	/**
	 * Get a reference to the SemanticMarkup container associated with this port
	 */
	public SemanticMarkup getMetadata() {
		return this.metadata;
	}

	/**
	 * Check if port name is editable, ie. that setName() will have an effect.
	 * Only workflow sink or source ports are normally editable.
	 * 
	 * @return true if setName() will set the name.
	 */
	public boolean isNameEditable() {
		ScuflModel model = this.processor.getModel();
		if (model == null)
			return false;
		// TODO: Why not use isSource() and isSink() ?
		if (model.getWorkflowSinkProcessor() == this.processor)
			return true;
		if (model.getWorkflowSourceProcessor() == this.processor)
			return true;
		return false;
	}

	/**
	 * Set the name of the port.
	 * 
	 * Should only ever be called on workflow sink or source ports, will have no
	 * effect on others.
	 * 
	 * The new name must match the regular expression
	 * 
	 * <pre>
	 * \w+
	 * </pre>
	 * 
	 * @see isNameEditable()
	 */
	public void setName(String name) {
		if (!isNameEditable()) {
			return;
		}
		if (name.equals(this.name)) {
			return; // ignore
		}
		if (name.matches("\\w+")) {
			this.name = name;
			fireModelEvent(new MinorScuflModelEvent(this,
					"Port name changed to " + name));
		}
	}

	/**
	 * Set the syntactic type of the port
	 */
	public void setSyntacticType(String new_type) {
		if (new_type.equals(this.syntacticType) == false) {
			this.syntacticType = new_type;
			// Add any mime types from the syntactic type into the
			// semantic markup object
			String[] split = new_type.split("'");
			String mime;
			if (split.length == 1) {
				mime = split[0];
			} else {
				mime = split[1];
			}
			metadata.addMIMEType(mime);
			fireModelEvent(new ScuflModelEvent(this,
					"Syntactic type changed to '" + new_type + "'"));
		}
	}

	/**
	 * Get the syntactic type of the port
	 */
	public String getSyntacticType() {
		return this.syntacticType;
	}

	/**
	 * Is this port a workflow source?
	 */
	public boolean isSource() {
		return (this.getProcessor() instanceof InternalSourcePortHolder);
	}

	/**
	 * Is this port a workflow sink?
	 */
	public boolean isSink() {
		return (this.getProcessor() instanceof InternalSinkPortHolder);
	}

	/**
	 * Get the processor that this port belongs to.
	 */
	public Processor getProcessor() {
		return this.processor;
	}

	/**
	 * Get the name for this port. There is no set method, ports are named at
	 * creation time and the names are immutable from that point onwards. <br>
	 * There is an exception to this rule in the case of workflow source and
	 * sink ports, these can be renamed after their creation.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the name as the toString() implementation
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * Handle model events
	 */
	void fireModelEvent(ScuflModelEvent event) {
		this.processor.fireModelEvent(event);
	}

}
