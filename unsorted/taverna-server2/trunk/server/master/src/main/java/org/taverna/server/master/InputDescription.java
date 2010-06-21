package org.taverna.server.master;

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
 * A description of the inputs to a workflow.
 * 
 * @author Donal Fellows
 */
@XmlRootElement
@XmlType(name="InputDescription")
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
	public List<Port> port;

	public InputDescription() {
	}

	InputDescription(TavernaRun r) {
		baclavaFile = r.getInputBaclavaFile();
		if (baclavaFile == null) {
			List<Input> is = r.getInputs();
			port = new ArrayList<Port>(is.size());
			for (Input i : is)
				port.add(new Port(i));
		}
	}

	/**
	 * The type of a single port description.
	 * 
	 * @author Donal Fellows
	 */
	@XmlType(name="PortDescription")
	public static class Port {
		/**
		 * The name of this port.
		 */
		@XmlAttribute(required = true)
		public String name;
		/**
		 * The file assigned to this port.
		 */
		@XmlAttribute
		public String file;
		/**
		 * The value assigned to this port.
		 */
		@XmlValue
		public String value;

		public Port() {
		}

		Port(Input i) {
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
