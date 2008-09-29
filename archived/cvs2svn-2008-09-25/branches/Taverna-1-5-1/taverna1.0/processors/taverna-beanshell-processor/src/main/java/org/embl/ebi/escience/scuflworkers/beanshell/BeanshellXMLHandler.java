package org.embl.ebi.escience.scuflworkers.beanshell;

import static org.embl.ebi.escience.scufl.XScufl.XScuflNS;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.embl.ebi.escience.scuflworkers.dependency.DependencyXMLHandler;
import org.jdom.Element;

/**
 * Handles XML store and load for the beanshell processor
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class BeanshellXMLHandler implements XMLHandler {

	private static Logger logger = Logger.getLogger(BeanshellXMLHandler.class);
	
	public Element elementForProcessor(Processor p) {
		BeanshellProcessor bp = (BeanshellProcessor) p;
		Element spec = new Element("beanshell", XScuflNS);
		// Script element
		Element script = new Element("scriptvalue", XScuflNS);
		script.setText(bp.getScript());
		spec.addContent(script);
		// Input list
		Element inputList = new Element("beanshellinputlist", XScuflNS);
		InputPort[] inputs = bp.getInputPorts();
		for (int i = 0; i < inputs.length; i++) {
			Element inputElement = new Element("beanshellinput",
					XScuflNS);
			inputElement.setText(inputs[i].getName());
			if (inputs[i].getSyntacticType() != null)
				inputElement.setAttribute("syntactictype", inputs[i]
						.getSyntacticType(), XScuflNS);
			inputList.addContent(inputElement);
		}
		spec.addContent(inputList);
		// Output list
		Element outputList = new Element("beanshelloutputlist", XScuflNS);
		OutputPort[] outputs = bp.getOutputPorts();
		for (int i = 0; i < outputs.length; i++) {
			Element outputElement = new Element("beanshelloutput",
					XScuflNS);
			outputElement.setText(outputs[i].getName());
			if (outputs[i].getSyntacticType() != null)
				outputElement.setAttribute("syntactictype", outputs[i]
						.getSyntacticType(), XScuflNS);
			outputList.addContent(outputElement);
		}
		spec.addContent(outputList);
		spec.addContent(DependencyXMLHandler.saveDependencies(bp));
		return spec;
	}


	public Element elementForFactory(ProcessorFactory pf) {
		BeanshellProcessorFactory bpf = (BeanshellProcessorFactory) pf;
		if (bpf.getPrototype() != null) {
			return elementForProcessor(bpf.getPrototype());
		} else {
			Element spec = new Element("beanshell", XScuflNS);
			return spec;
		}
	}

	public ProcessorFactory getFactory(Element specElement) {
		Element processorNode = new Element("processor");
		Element spec = (Element) specElement.clone();
		spec.detach();
		processorNode.addContent(spec);
		BeanshellProcessor bp = null;
		try {
			bp = (BeanshellProcessor) loadProcessorFromXML(processorNode, null,
					"foo");
		} catch (Exception ex) {
			logger.error("Could not load from XML", ex);
		}
		if (bp != null) {
			return new BeanshellProcessorFactory(bp);
		} else {
			return new BeanshellProcessorFactory();
		}
	}

	public Processor loadProcessorFromXML(Element processorNode,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		BeanshellProcessor bp = new BeanshellProcessor(model, name, "",
				new String[0], new String[0]);
		Element beanshell = processorNode
				.getChild("beanshell", XScuflNS);
		Element scriptElement = beanshell.getChild("scriptvalue",
				XScuflNS);
		if (scriptElement != null) {
			String script = scriptElement.getTextTrim();
			bp.setScript(script);
		}
		// Handle inputs
		Element inputList = beanshell.getChild("beanshellinputlist",
				XScuflNS);
		if (inputList != null) {
			for (Iterator i = inputList.getChildren().iterator(); i.hasNext();) {
				Element inputElement = (Element) i.next();
				String inputName = inputElement.getTextTrim();
				String syntacticType = inputElement.getAttributeValue(
						"syntactictype", XScuflNS);
				try {
					InputPort p = new InputPort(bp, inputName);
					if (syntacticType != null)
						p.setSyntacticType(syntacticType);
					bp.addPort(p);
				} catch (PortCreationException pce) {
					throw new ProcessorCreationException(
							"Unable to create port! " + pce.getMessage());
				} catch (DuplicatePortNameException dpne) {
					throw new ProcessorCreationException(
							"Unable to create port! " + dpne.getMessage());
				}
			}
		}
		// Handle outputs
		Element outputList = beanshell.getChild("beanshelloutputlist",
				XScuflNS);
		if (outputList != null) {
			for (Iterator i = outputList.getChildren().iterator(); i.hasNext();) {
				Element outputElement = (Element) i.next();
				String outputName = outputElement.getTextTrim();
				String syntacticType = outputElement.getAttributeValue(
						"syntactictype", XScuflNS);
				try {
					OutputPort p = new OutputPort(bp, outputName);
					if (syntacticType != null)
						p.setSyntacticType(syntacticType);
					bp.addPort(p);
				} catch (PortCreationException pce) {
					throw new ProcessorCreationException(
							"Unable to create port! " + pce.getMessage());
				} catch (DuplicatePortNameException dpne) {
					throw new ProcessorCreationException(
							"Unable to create port! " + dpne.getMessage());
				}
			}
		}
		DependencyXMLHandler.loadDependencies(bp, beanshell);
		
		// return new BeanshellProcessor(model, name, script, new String[0], new
		// String[0]);
		return bp;
	}
	

}
