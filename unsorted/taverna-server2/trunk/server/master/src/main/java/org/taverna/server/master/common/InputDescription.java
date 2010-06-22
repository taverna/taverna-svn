package org.taverna.server.master.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.taverna.server.master.interfaces.Input;
import org.taverna.server.master.interfaces.TavernaRun;

/**
 * A description of the inputs to a workflow, described using JAXB.
 * 
 * @author Donal Fellows
 */
@XmlRootElement
@XmlType(name = "InputDescription")
public class InputDescription {
	/**
	 * The Baclava file handling the description of the elements. May be
	 * omitted/<tt>null</tt>.
	 */
	@XmlElement(required = false)
	public String baclavaFile;
	/**
	 * The port/value assignment.
	 */
	@XmlElement(nillable = false)
	public List<Port> port = new ArrayList<Port>();

	public InputDescription() {
	}

	public InputDescription(TavernaRun r) {
		baclavaFile = r.getInputBaclavaFile();
		if (baclavaFile == null)
			for (Input i : r.getInputs())
				port.add(new Port(i));
	}

	/**
	 * The type of a single port description.
	 * 
	 * @author Donal Fellows
	 */
	@XmlType(name = "PortDescription")
	public static class Port {
		/**
		 * The name of this port.
		 */
		@XmlAttribute(required = true)
		public String name;
		/**
		 * The file assigned to this port.
		 */
		@XmlAttribute(required = false)
		public String file;
		/**
		 * The value assigned to this port.
		 */
		@XmlValue
		public String value;

		public Port() {
		}

		public Port(Input i) {
			name = i.getName();
			if (i.getFile() != null) {
				file = i.getFile();
				value = "";
			} else {
				file = null;
				value = i.getValue();
			}
		}
	}
}
