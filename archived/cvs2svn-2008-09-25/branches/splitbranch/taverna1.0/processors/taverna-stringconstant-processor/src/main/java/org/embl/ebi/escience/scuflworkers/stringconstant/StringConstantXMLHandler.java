package org.embl.ebi.escience.scuflworkers.stringconstant;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;



/**
 * Handles XML store and load for the string constant processor
 * @author Tom Oinn
 */
public class StringConstantXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	StringConstantProcessor scp = (StringConstantProcessor)p;
	Element spec = new Element("stringconstant",XScufl.XScuflNS);
	spec.setText(scp.getStringValue());
	return spec;
    }
    
    public Element elementForFactory(ProcessorFactory pf) {
	Element e = new Element("stringconstant",XScufl.XScuflNS);
	if (((StringConstantProcessorFactory)pf).getValue() != null) {
	    e.setText(((StringConstantProcessorFactory)pf).getValue());
	}
	return e;
    }

    public ProcessorFactory getFactory(Element specElement) {
	return new StringConstantProcessorFactory(specElement.getText());
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element stringconstant = processorNode.getChild("stringconstant",XScufl.XScuflNS);
	String value = stringconstant.getText();
	return new StringConstantProcessor(model, name, value);
    }

}
