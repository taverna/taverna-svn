package net.sourceforge.taverna.scuflworkers.bsf;

import java.util.Iterator;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

import net.sourceforge.taverna.scuflworkers.bsf.*;
import org.jdom.Element;

/**
 * Handles XML store and load for the bsf processor
 * 
 * @author Tom Oinn
 */
public class BSFXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
        BSFProcessor bp = (BSFProcessor) p;
        Element spec = new Element("beanshell", XScufl.XScuflNS);
        // Script element
        Element script = new Element("scriptvalue", XScufl.XScuflNS);
        script.setText(bp.getScript());
        spec.addContent(script);
        // Input list
        Element inputList = new Element("beanshellinputlist", XScufl.XScuflNS);
        InputPort[] inputs = bp.getInputPorts();
        for (int i = 0; i < inputs.length; i++) {
            Element inputElement = new Element("beanshellinput",
                    XScufl.XScuflNS);
            inputElement.setText(inputs[i].getName());
            if (inputs[i].getSyntacticType() != null)
                inputElement.setAttribute("syntactictype", inputs[i]
                        .getSyntacticType(), XScufl.XScuflNS);
            inputList.addContent(inputElement);
        }
        spec.addContent(inputList);
        // Output list
        Element outputList = new Element("beanshelloutputlist", XScufl.XScuflNS);
        OutputPort[] outputs = bp.getOutputPorts();
        for (int i = 0; i < outputs.length; i++) {
            Element outputElement = new Element("beanshelloutput",
                    XScufl.XScuflNS);
            outputElement.setText(outputs[i].getName());
            if (outputs[i].getSyntacticType() != null)
                outputElement.setAttribute("syntactictype", outputs[i]
                        .getSyntacticType(), XScufl.XScuflNS);
            outputList.addContent(outputElement);
        }
        spec.addContent(outputList);
        return spec;
    }

    public Element elementForFactory(ProcessorFactory pf) {
        BSFProcessorFactory bpf = (BSFProcessorFactory) pf;
        if (bpf.getPrototype() != null) {
            return elementForProcessor(bpf.getPrototype());
        } else {
            Element spec = new Element("beanshell", XScufl.XScuflNS);
            return spec;
        }
    }

    public ProcessorFactory getFactory(Element specElement) {
        Element processorNode = new Element("processor");
        Element spec = (Element) specElement.clone();
        spec.detach();
        processorNode.addContent(spec);
        BSFProcessor bp = null;
        try {
            bp = (BSFProcessor) loadProcessorFromXML(processorNode, null, "foo");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (bp != null) {
            return new BSFProcessorFactory(bp);
        } else {
            return new BSFProcessorFactory();
        }
    }

    public Processor loadProcessorFromXML(Element processorNode,
            ScuflModel model, String name) throws ProcessorCreationException,
            DuplicateProcessorNameException, XScuflFormatException {
        BSFProcessor bp = new BSFProcessor(model, name, "",
                new String[0], new String[0]);
        Element beanshell = processorNode
                .getChild("bsfscript", XScufl.XScuflNS);
        Element scriptElement = beanshell.getChild("scriptvalue",
                XScufl.XScuflNS);
        if (scriptElement != null) {
            String script = scriptElement.getTextTrim();
            bp.setScript(script);
        }
        // Handle inputs
        Element inputList = beanshell.getChild("beanshellinputlist",
                XScufl.XScuflNS);
        if (inputList != null) {
            for (Iterator i = inputList.getChildren().iterator(); i.hasNext();) {
                Element inputElement = (Element) i.next();
                String inputName = inputElement.getTextTrim();
                String syntacticType = inputElement.getAttributeValue(
                        "syntactictype", XScufl.XScuflNS);
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
                XScufl.XScuflNS);
        if (outputList != null) {
            for (Iterator i = outputList.getChildren().iterator(); i.hasNext();) {
                Element outputElement = (Element) i.next();
                String outputName = outputElement.getTextTrim();
                String syntacticType = outputElement.getAttributeValue(
                        "syntactictype", XScufl.XScuflNS);
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
        //return new BeanshellProcessor(model, name, script, new String[0], new
        // String[0]);
        return bp;
    }

}