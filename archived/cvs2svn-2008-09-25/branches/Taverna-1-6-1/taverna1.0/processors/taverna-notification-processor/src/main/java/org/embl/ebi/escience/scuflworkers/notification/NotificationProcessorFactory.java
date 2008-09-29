/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package org.embl.ebi.escience.scuflworkers.notification;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author Justin Ferris
 * @author Ananth Krishna
 */
public class NotificationProcessorFactory extends ProcessorFactory {
    
    public NotificationProcessorFactory() {
	setName("Notification Processor");
    }
    
    public String getProcessorDescription() {
        return "A Notification Processor";
    }

    public Class getProcessorClass() {
        return NotificationProcessor.class;
    }

}
