/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * APIConsumer factory
 * @author Tom Oinn
 */
public class APIConsumerProcessorFactory extends ProcessorFactory {
    
    APIConsumerDefinition definition;

    /**    public APIConsumerProcessorFactory(Element e) {
	   String methodName = e.getAttributeValue("name","No method!");
	   setName(methodName);
	   Element descriptionElement = e.getChild("Description");
	   if (descriptionElement != null) {
	   setDescription(descriptionElement.getTextTrim());
	   }
	   }
    */
    
    public APIConsumerProcessorFactory(APIConsumerDefinition def) {
	this.definition = def;
	setDescription(def.description);
	setName(def.methodName);
    }

    public String getProcessorDescription() {
	return "Generic Java API consumer";
    }

    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerProcessor.class;
    }

}
