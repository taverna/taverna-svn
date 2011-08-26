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
     * Return the spec element for a given ProcessorFactory.
     * In reality each XML handler will be given only a particular
     * subclass of the ProcessorFactory to deal with so
     * you can reasonably cast it to your specific implementation
     * straight off to get factory specific data out.
     */
    public Element elementForFactory(ProcessorFactory pf);

    /**
     * Create a new factory that will produces processors of the
     * supplied spec when it's invoked
     */
    public ProcessorFactory getFactory(Element specElement);

    /**
     * Create a new processor from the given chunk of XML
     */
     public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException;
    
}

   
