/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.SetOnlineException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.jdom.Document;
import org.jdom.Element;

/**
 * A processor containing a full ScuflModel instance. Ports on the processor are
 * directly copied in terms of names and types from the input and output ports
 * of the underlying ScuflModel object.
 * 
 * @author Tom Oinn
 */
public class WorkflowProcessor extends Processor implements java.io.Serializable {

	private ScuflModel theModel = null;

	private String definitionURL = null;

	/**
	 * Go offline
	 */
	public void setOffline() {
		try {
			this.theModel.setOffline(true);
			System.out.println("Set nested processor to offline mode");
		} catch (SetOnlineException soe) {
			soe.printStackTrace();
		}
	}

	/**
	 * Go online
	 */
	public void setOnline() {
		try {
			this.theModel.setOffline(false);
			System.out.println("Set nested processor to online mode");
		} catch (SetOnlineException soe) {
			soe.printStackTrace();
		}
	}

	public int getMaximumWorkers() {
		return 10;
	}

	/**
	 * Construct a new processor with the given model to bind to, name and URL
	 * of a workflow description to contain.
	 */
	public WorkflowProcessor(ScuflModel model, String name, String definitionURL) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, name);
		this.definitionURL = definitionURL;
		try {
			// Create a new model instance
			this.theModel = new ScuflModel();
			try {
				this.theModel.setOffline(model.isOffline());
			} catch (SetOnlineException soe) {
				//
			}
			// Populate from the definition URL
			XScuflParser.populate(new URL(definitionURL).openStream(), theModel, null);
			buildPorts();
			createListener();
		} catch (MalformedURLException mue) {
			throw new ProcessorCreationException("The supplied definition URL was malformed, specified as '"
					+ definitionURL + "'");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProcessorCreationException("The workflow processor '" + name + "' caused an exception :\n"
					+ e.getMessage() + "\n during creation. The exception had type :\n" + e.getClass().toString());
		}
	}

	/**
	 * Construct a new processor from the supplied JDOM element, this element
	 * being the 'scufl' top level workflow element from an inline nested
	 * workflow declaration
	 */
	public WorkflowProcessor(ScuflModel model, String name, Element scuflElement) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, name);
		// Have to clone the element because it already has a parent
		// so will not work with a new document instance
		try {
			Document doc = new Document((Element) scuflElement.clone());
			this.theModel = new ScuflModel();
			try {
				if (model != null) {
					this.theModel.setOffline(model.isOffline());
				}
			} catch (SetOnlineException soe) {
				//
			}
			XScuflParser.populate(doc, theModel, null);
			setDescription(theModel.getDescription().getText());
			buildPorts();
			createListener();
		} catch (Exception e) {
			throw new ProcessorCreationException("The workflow processor '" + name + "' caused an exception :\n"
					+ e.getMessage() + "\n during creation. The exception had type :\n" + e.getClass().toString());
		}
	}

	/**
	 * Construct a new processor with a blank internal workflow
	 */
	public WorkflowProcessor(ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, name);
		this.theModel = new ScuflModel();
		try {
			if (model != null) {
				this.theModel.setOffline(model.isOffline());
			}
		} catch (SetOnlineException soe) {
			//
		}
		createListener();
	}

	/**
	 * Attach a listener to the contained workflow instance which will set the
	 * location string to null, indicating that the original workflow (if any)
	 * has been changed, effectively performing a copy on write style caching.
	 */
	private void createListener() {
		this.theModel.addListener(new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
				WorkflowProcessor.this.definitionURL = null;
				try {
					if (buildPorts() == false) {
						// Only throw a new event up if nothing has changed in
						// the port
						// list, if something has been changed then the parent
						// workflow
						// will already have been kicked by the port creation or
						// destruction
						fireModelEvent(new ScuflModelEvent(WorkflowProcessor.this, "Underlying workflow changed"));
					}
				} catch (PortCreationException pce) {
					//
				} catch (DuplicatePortNameException dpne) {
					//
				}
			}
		});
	}

	/**
	 * Inspect the current workflow model and create any ports required, returns
	 * true if anything was changed, false otherwise.
	 */
	private boolean buildPorts() throws PortCreationException, DuplicatePortNameException {
		boolean changed = false;
		// Iterate over the workflow sinks to get the output ports
		Port[] outputs = this.theModel.getWorkflowSinkPorts();
		for (int i = 0; i < outputs.length; i++) {
			// Create a new output port if it doesn't already exist
			try {
				locatePort(outputs[i].getName()).setSyntacticType(outputs[i].getSyntacticType());
			} catch (UnknownPortException upe) {
				Port newPort = new OutputPort(this, outputs[i].getName());
				newPort.setSyntacticType(outputs[i].getSyntacticType());
				// newPort.setSemanticType(outputs[i].getSemanticType());
				this.addPort(newPort);
				changed = true;
			}
		}
		// Iterate over workflow sources to get the input ports
		Port[] inputs = this.theModel.getWorkflowSourcePorts();
		for (int i = 0; i < inputs.length; i++) {
			// Create a new input port if it doesn't already exist
			try {
				locatePort(inputs[i].getName()).setSyntacticType(inputs[i].getSyntacticType());
			} catch (UnknownPortException upe) {
				Port newPort = new InputPort(this, inputs[i].getName());
				newPort.setSyntacticType(inputs[i].getSyntacticType());
				// newPort.setSemanticType(inputs[i].getSemanticType());
				this.addPort(newPort);
				changed = true;
			}
		}
		// Now check to see if there were any ports we previously had but that
		// have since been removed. As we definitely added any ports that we
		// didn't
		// already have this should be easy.
		Port[] ourInputPorts = getInputPorts();
		for (int i = 0; i < ourInputPorts.length; i++) {
			// Try to find this in the nested model
			String portName = ourInputPorts[i].getName();
			try {
				this.theModel.getWorkflowSourceProcessor().locatePort(portName);
			} catch (UnknownPortException upe) {
				removePort(ourInputPorts[i]);
				changed = true;
			}
		}
		Port[] ourOutputPorts = getOutputPorts();
		for (int i = 0; i < ourOutputPorts.length; i++) {
			// Try to find this in the nested model
			String portName = ourOutputPorts[i].getName();
			try {
				this.theModel.getWorkflowSinkProcessor().locatePort(portName);
			} catch (UnknownPortException upe) {
				removePort(ourOutputPorts[i]);
				changed = true;
			}
		}
		return changed;
	}

	public ScuflModel getInternalModel() {
		return this.theModel;
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		String definitionURL2 = getDefinitionURL();
		if (definitionURL2!=null){
			props.put("XScufl URL", definitionURL2);
		}
		return props;
	}

	public String getDefinitionURL() {
		return this.definitionURL;
	}
}
