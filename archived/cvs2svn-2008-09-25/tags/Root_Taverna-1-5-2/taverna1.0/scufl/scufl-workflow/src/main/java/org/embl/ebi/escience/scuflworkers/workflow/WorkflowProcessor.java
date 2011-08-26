/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
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
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessor;
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
@SuppressWarnings("serial")
public class WorkflowProcessor extends Processor implements ScuflWorkflowProcessor, Serializable {

	private static Logger logger = Logger.getLogger(WorkflowProcessor.class);
	
	private ScuflModel theInternalModel = null;

	private String definitionURL = null;
	
	private ScuflModelEventListener eventListener = null;

	/**
	 * Go offline
	 */
	public void setOffline() {
		try {
			this.theInternalModel.setOffline(true);
			logger.info("Set nested processor offline");
		} catch (SetOnlineException soe) {
			logger.warn("Could not set nested processor offline", soe);			
		}
	}

	/**
	 * Go online
	 */
	public void setOnline() {
		try {
			this.theInternalModel.setOffline(false);
			logger.info("Set nested processor online");			
		} catch (SetOnlineException soe) {
			logger.warn("Could not set nested processor online", soe);			
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
			this.theInternalModel = new ScuflModel();
			try {
				this.theInternalModel.setOffline(model.isOffline());
			} catch (SetOnlineException soe) {
				//
			}
			// Populate from the definition URL
			XScuflParser.populate(new URL(definitionURL).openStream(), theInternalModel, null);
			buildPorts();
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
			this.theInternalModel = new ScuflModel();
			try {
				if (model != null) {
					this.theInternalModel.setOffline(model.isOffline());
				}
			} catch (SetOnlineException soe) {
				//
			}
			XScuflParser.populate(doc, theInternalModel, null);
			setDescription(theInternalModel.getDescription().getText());
			buildPorts();
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
		this.theInternalModel = new ScuflModel();
		try {
			if (model != null) {
				this.theInternalModel.setOffline(model.isOffline());
			}
		} catch (SetOnlineException soe) {
			//
		}
	}

	/**
	 * Attach a listener to the contained workflow instance which will set the
	 * location string to null, indicating that the original workflow (if any)
	 * has been changed, effectively performing a copy on write style caching.
	 */
	private void createListener() {
		if (eventListener==null) {
			eventListener=new ScuflModelEventListener() {
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
			};
		}
		this.theInternalModel.addListener(eventListener);
	}

	/**
	 * Inspect the current workflow model and create any ports required, returns
	 * true if anything was changed, false otherwise.
	 */
	private boolean buildPorts() throws PortCreationException, DuplicatePortNameException {
		boolean changed = false;
		// Iterate over the workflow sinks to get the output ports
		Port[] outputs = this.theInternalModel.getWorkflowSinkPorts();
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
		Port[] inputs = this.theInternalModel.getWorkflowSourcePorts();
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
				this.theInternalModel.getWorkflowSourceProcessor().locatePort(portName);
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
				this.theInternalModel.getWorkflowSinkProcessor().locatePort(portName);
			} catch (UnknownPortException upe) {
				removePort(ourOutputPorts[i]);
				changed = true;
			}
		}
		return changed;
	}
	
	
	/**
	 * Removes the ScuflModelEventListener from the internal ScuflModel
	 */
	public void removeInternalModelEventListener() {
		this.theInternalModel.removeListener(eventListener);
	}

	/**
	 * Returns a ScuflModel that is being listened to by the processor
	 * So that any internal changes are automatically reflected in the processor
	 * 
	 * The client is responsible for calling ScuflModel.removeListeners once the model
	 * is finished with.
	 * @return
	 */
	public ScuflModel getInternalModelForEditing() {
		createListener();
		return this.theInternalModel;
	}
	
	public ScuflModel getInternalModel() {
		return this.theInternalModel;
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		String definitionURL2 = getDefinitionURL();
		if (definitionURL2 != null) {
			props.put("XScufl URL", definitionURL2);
		}
		return props;
	}

	public String getDefinitionURL() {
		return this.definitionURL;
	}
}
