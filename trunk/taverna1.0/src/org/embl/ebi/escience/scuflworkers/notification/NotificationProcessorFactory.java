/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package org.embl.ebi.escience.scuflworkers.notification;

import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scufl.*;

/**
 * @author Justin Ferris
 * @author Ananth Krishna
 */
public class NotificationProcessorFactory extends ProcessorFactory {
    /**
       public Processor createProcessor(String name, ScuflModel model)
       throws ProcessorCreationException,
       DuplicateProcessorNameException {
       NotificationProcessor processor = new NotificationProcessor(model, name);
       if(model != null) {
       model.addProcessor(processor);
       }
       return processor;
       }
    */

    public String getProcessorDescription() {
        return "A Notification Processor";
    }

    public Class getProcessorClass() {
        return NotificationProcessor.class;
    }

    public String toString() {
        return "Notification Processor";
    }
}
