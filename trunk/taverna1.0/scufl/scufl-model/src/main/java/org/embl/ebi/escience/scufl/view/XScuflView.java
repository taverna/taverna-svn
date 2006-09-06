/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.AnnotationTemplate;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Represents a ScuflModel instance as an XScufl document.
 * <p>
 * The view registers with the model to always provide updated XML representations. 
 * <p>
 * Note that when you are finished using the view, you have to
 * manually remove the listener from the model as:
 * <pre>
 *   XScuflView view = new XScuflView(model);
 *   ..
 *   model.removeListener(view);
 * </pre>
 * If you don't do this, the NotifyThread will be kept alive by the
 * XScuflView, and the XScuflView by the list of listeners in 
 * NotifyThread. Indirectly that would mean even 
 * the ScuflModel is kept alive forever.
 * <p>
 * Use the static methods <code>getDocument(ScuflModel model)</code>
 * and <code>getXMLText(ScuflModel model)</code> to avoid constructing 
 * the view and having to remember to remove the listener from the
 * ScuflModel. 
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 * 
 */
public class XScuflView implements ScuflModelEventListener,
		java.io.Serializable {

	private ScuflModel model = null;

	private boolean cacheValid = false;

	private String cachedRepresentation = null;

	private Document cachedDocument = null;

	
	/**
	 * Construct the view and bind to the given model.
	 */
	public XScuflView(ScuflModel model) {
		// Reference to the model that this is a view on
		this.model = model;
		// Cached copy doesn't exist so set validity to false
		this.cacheValid = false;
		// Be informed of events corresponding to changes in the model
		model.addListener(this);
	}

	/**
	 * Get the XML Document from this view.
	 * <p>
	 * This is probably preferable over getXMLText() if you 
	 * are passing on the XML to another method expecting either
	 * Document or String.
	 * 
	 */
	public Document getDocument() {
		synchronized (this) {
			if (!cacheValid) {
				updateCachedView();
			}
			return this.cachedDocument;
		}
	}
	
	/**
	 * Get the XML Document for the model.
	 * <p>
	 * Use this static version of getXMLText() if you are only
	 * getting the XML once, and don't expect the model to change. 
	 * <p>
	 * This is probably preferable over getXMLText(model) if you 
	 * are passing on the XML to another method expecting either
	 * Document or String. 
	 * 
	 */
	public static Document getDocument(ScuflModel model) {
		XScuflView view = new XScuflView(model);
		Document doc = view.getDocument();
		model.removeListener(view);
		return doc;
	}
	

	/**
	 * Get the XML String from this view
	 */
	public String getXMLText() {
		synchronized (this) {
			if (!cacheValid) {
				updateCachedView();
			}
			return this.cachedRepresentation;
		}
	}
	/**
	 * Get the XML representation as String for this model. 
	 * Use this static version of getXMLText() if you are only
	 * getting the XML once, and don't expect the model to change.  
	 *  
	 */
	public static String getXMLText(ScuflModel model) {
		XScuflView view = new XScuflView(model);
		String xml = view.getXMLText();
		model.removeListener(view);
		return xml;
	}

	/**
	 * Invalidate cache on changed model (but don't recalculate the XML now)
	 */
	public void receiveModelEvent(ScuflModelEvent event) {
		// Invalidate cache, this will
		// force a recalculation next time
		// the view is queried.
		synchronized (this) {
			this.cacheValid = false;
		}
	}

	/**
	 * Update or create the cached view, this consists of building the XML
	 * document from the model and creating a textual version of it.
	 */
	private void updateCachedView() {
		// Create the XML document
		Element root = new Element("scufl", scuflNS());
		root.setAttribute("version", "0.2");
		root.setAttribute("log", "" + model.getLogLevel());		

		WorkflowDescription wd = model.getDescription();
		root.addContent(WorkflowDescription.getElement(wd));

		// Create elements corresponding to processors
		Processor[] processors = model.getProcessors();
		for (int i = 0; i < processors.length; i++) {
			Element processor = new Element("processor", scuflNS());
			processor.setAttribute("name", processors[i].getName());
			if (processors[i].getWorkers() != processors[i].getDefaultWorkers()) {
				processor.setAttribute("workers", ""
						+ processors[i].getWorkers());
			}
			if (processors[i].isBoring()) {
				processor.setAttribute("boring", "true");
			}
			// Only set the log level if it is zero or higher, negative values
			// implicitly mean 'inherit from model'
			if (processors[i].getRealLogLevel() > -1) {
				processor.setAttribute("log", "" + processors[i].getLogLevel());
			}
			// Set the description if it isn't the empty string
			String description = processors[i].getDescription();
			if (description.equals("") == false) {
				Element de = new Element("description", scuflNS());
				de.setText(description);
				processor.addContent(de);
			}
			// Set any default input values
			boolean addedDefault = false;
			Element defaultSet = new Element("defaults", scuflNS());
			InputPort[] ip = processors[i].getInputPorts();
			for (int j = 0; j < ip.length; j++) {
				if (ip[j].hasDefaultValue()) {
					String portName = ip[j].getName();
					String defaultValue = ip[j].getDefaultValue();
					Element dv = new Element("default", scuflNS());
					dv.setAttribute("name", portName);
					dv.setText(defaultValue);
					defaultSet.addContent(dv);
					addedDefault = true;
				}
			}
			if (addedDefault) {
				processor.addContent(defaultSet);
			}

			// Define the actual processor content
			Element spec = ProcessorHelper.elementForProcessor(processors[i]);
			processor.addContent(spec);
			// Set the merge modes on the ports that have modes other than
			// InputPort.NDSELECT,
			// currently only applied to InputPort.MERGE
			InputPort[] inputs = processors[i].getInputPorts();
			for (int j = 0; j < inputs.length; j++) {
				if (inputs[j].getMergeMode() != InputPort.NDSELECT) {
					Element mergeModeElement = new Element("mergemode",
							XScufl.XScuflNS);
					mergeModeElement.setAttribute("input", inputs[j].getName());
					mergeModeElement.setAttribute("mode", "merge");
					processor.addContent(mergeModeElement);
				}
			}
			// Do the iteration strategy if it exists
			IterationStrategy iterationStrategy = processors[i]
					.getIterationStrategy();
			if (iterationStrategy != null
					&& processors[i].getInputPorts().length > 0) {
				processor.addContent(iterationStrategy.getElement());
			}
			// Do the templates
			AnnotationTemplate[] templates = processors[i]
					.getAnnotationTemplates();
			for (int j = 0; j < templates.length; j++) {
				processor.addContent(templates[j].getElement());
			}
			// Do the alternates
			AlternateProcessor[] ap = processors[i].getAlternatesArray();
			for (int j = 0; j < ap.length; j++) {
				Element alternateElement = new Element("alternate",
						XScufl.XScuflNS);
				Processor alternateProcessor = ap[j].getProcessor();
				// Populate the processor spec part of the alternate
				alternateElement.addContent(ProcessorHelper
						.elementForProcessor(alternateProcessor));
				// Populate the output mapping part
				for (Iterator ii = ap[j].getOutputMapping().keySet().iterator(); ii
						.hasNext();) {
					String key = (String) ii.next();
					String value = (String) ap[j].getOutputMapping().get(key);
					Element mappingElement = new Element("outputmap",
							XScufl.XScuflNS);
					mappingElement.setAttribute("key", key);
					mappingElement.setAttribute("value", value);
					alternateElement.addContent(mappingElement);
				}
				// .. and the input mapping
				for (Iterator ii = ap[j].getInputMapping().keySet().iterator(); ii
						.hasNext();) {
					String key = (String) ii.next();
					String value = (String) ap[j].getInputMapping().get(key);
					Element mappingElement = new Element("inputmap",
							XScufl.XScuflNS);
					mappingElement.setAttribute("key", key);
					mappingElement.setAttribute("value", value);
					alternateElement.addContent(mappingElement);
				}
				processor.addContent(alternateElement);
			}
			root.addContent(processor);
		}

		// Create elements corresponding to data constraints
		DataConstraint[] dataconstraints = model.getDataConstraints();
		for (int i = 0; i < dataconstraints.length; i++) {

			DataConstraint dc = dataconstraints[i];
			String sourceProcessorName = dc.getSource().getProcessor()
					.getName();
			String sourcePortName = dc.getSource().getName();
			String sinkProcessorName = dc.getSink().getProcessor().getName();
			String sinkPortName = dc.getSink().getName();

			Element link = new Element("link", scuflNS());
			if (dc.getSink().getProcessor() != model.getWorkflowSinkProcessor()) {
				sinkPortName = sinkProcessorName + ":" + sinkPortName;
			}
			if (dc.getSource().getProcessor() != model
					.getWorkflowSourceProcessor()) {
				sourcePortName = sourceProcessorName + ":" + sourcePortName;
			}
			link.setAttribute("source", sourcePortName);
			link.setAttribute("sink", sinkPortName);

			Element inputNode = new Element("input", scuflNS());
			inputNode.setText(sinkPortName);

			Element outputNode = new Element("output", scuflNS());
			outputNode.setText(sourcePortName);

			// link.addContent(inputNode);
			// link.addContent(outputNode);
			root.addContent(link);
		}

		// Create elements for external ports
		Port[] sources = model.getWorkflowSourceProcessor().getPorts();
		for (int i = 0; i < sources.length; i++) {
			Element sourceElement = new Element("source", scuflNS());
			// sourceElement.setText(sources[i].getName());
			sourceElement.setAttribute("name", sources[i].getName());
			// Find all attached ports and add their MIME types to a temp list
			List knownMIMEs = new ArrayList();
			for (int j = 0; j < dataconstraints.length; j++) {
				DataConstraint dc = dataconstraints[j];
				if (dc.getSource().getProcessor() == model
						.getWorkflowSourceProcessor()) {
					SemanticMarkup sinkMarkup = dc.getSink().getMetadata();
					String[] types = sinkMarkup.getMIMETypes();
					for (int k = 0; k < types.length; k++) {
						knownMIMEs.add(types[k]);
					}
				}
			}
			Element metadataElement = sources[i].getMetadata()
					.getConfigurationElement(knownMIMEs);
			if (metadataElement.getChildren().isEmpty() == false) {
				sourceElement.addContent(metadataElement);
			}
			root.addContent(sourceElement);
		}
		InputPort[] sinks = model.getWorkflowSinkProcessor().getInputPorts();
		for (int i = 0; i < sinks.length; i++) {
			Element sinkElement = new Element("sink", scuflNS());
			if (sinks[i].getMergeMode() != InputPort.NDSELECT) {
				if (sinks[i].getMergeMode() == InputPort.MERGE) {
					sinkElement.setAttribute("mode", "merge");
				}
			}
			// sinkElement.setText(sinks[i].getName());
			sinkElement.setAttribute("name", sinks[i].getName());
			List knownMIMEs = new ArrayList();
			for (int j = 0; j < dataconstraints.length; j++) {
				DataConstraint dc = dataconstraints[j];
				if (dc.getSink().getProcessor() == model
						.getWorkflowSinkProcessor()) {
					SemanticMarkup sinkMarkup = dc.getSource().getMetadata();
					String[] types = sinkMarkup.getMIMETypes();
					for (int k = 0; k < types.length; k++) {
						knownMIMEs.add(types[k]);
					}
				}
			}
			Element metadataElement = sinks[i].getMetadata()
					.getConfigurationElement(knownMIMEs);
			if (metadataElement.getChildren().isEmpty() == false) {
				sinkElement.addContent(metadataElement);
			}
			root.addContent(sinkElement);
		}

		// Create elements corresponding to external port definitions
		// DEPRECATED
		/**
		 * Port[] externalPorts = model.getExternalPorts(); for (int i = 0; i <
		 * externalPorts.length; i++) { Element external = new
		 * Element("external",scuflNS());
		 * external.setText(externalPorts[i].getProcessor().getName()+":"+externalPorts[i].getName());
		 * root.addContent(external); }
		 */

		// Create elements corresponding to concurrency constraints
		ConcurrencyConstraint[] constraints = model.getConcurrencyConstraints();
		for (int i = 0; i < constraints.length; i++) {
			Element coordination = new Element("coordination", scuflNS());
			coordination.setAttribute("name", constraints[i].getName());
			root.addContent(coordination);
			Element condition = new Element("condition", scuflNS());
			Element action = new Element("action", scuflNS());
			coordination.addContent(condition);
			coordination.addContent(action);

			// Define the condition
			// <condition>
			// <target>ControllingProcessor</target>
			// <state>COMPLETED</state>
			// </condition>
			Element state = new Element("state", scuflNS());
			state.setText(ConcurrencyConstraint
					.statusCodeToString(constraints[i]
							.getControllerStateGuard()));
			condition.addContent(state);
			Element ctarget = new Element("target", scuflNS());
			ctarget.setText(constraints[i].getControllingProcessor().getName());
			condition.addContent(ctarget);

			// Define the action
			// <action>
			// <target>targetProcessor</target>
			// <statechange>
			// <from>SCHEDULED</from>
			// <to>RUNNING</to>
			// </statechange>
			// </action>
			Element target = new Element("target", scuflNS());
			target.setText(constraints[i].getTargetProcessor().getName());
			action.addContent(target);
			Element statechange = new Element("statechange", scuflNS());
			action.addContent(statechange);
			Element from = new Element("from", scuflNS());
			Element to = new Element("to", scuflNS());
			from.setText(ConcurrencyConstraint
					.statusCodeToString(constraints[i].getTargetStateFrom()));
			to.setText(ConcurrencyConstraint.statusCodeToString(constraints[i]
					.getTargetStateTo()));
			statechange.addContent(from);
			statechange.addContent(to);
		}

		// Finished, publish it
		this.cachedDocument = new Document(root);
		// Generate the textual version and cache it.
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		this.cachedRepresentation = xo.outputString(this.cachedDocument);
		// Cache is now valid.
		this.cacheValid = true;
	}

	/**
	 * The namespace for the generated nodes, references the scufl.XScufl class
	 */
	private Namespace scuflNS() {
		return XScufl.XScuflNS;
	}

}
