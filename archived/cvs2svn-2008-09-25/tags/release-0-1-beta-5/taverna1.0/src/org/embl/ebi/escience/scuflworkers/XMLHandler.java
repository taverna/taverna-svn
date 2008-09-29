package org.embl.ebi.escience.scuflworkers;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;

// JDOM Imports
import org.jdom.Element;

import java.lang.String;



/**
 * Specifies the two methods that individual worker handlers
 * must implement to allow store / load of processor objects
 * to / from XML syntax.
 * @author Tom Oinn
 */
public interface XMLHandler {
    
    /**
     * Return the spec element, that is to say the processor
     * specific portion of the processor element. For example,
     * the soaplab implementation of this method returns the
     * element rooted at the 'soaplabwsdl' element.
     */
    public Element elementForProcessor(Processor p);

    /**
     * Create a new processor from the given chunk of XML
     */
     public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException;
    
}

   
