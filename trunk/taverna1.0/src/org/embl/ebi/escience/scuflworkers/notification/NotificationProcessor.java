/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */

package org.embl.ebi.escience.scuflworkers.notification;

import java.util.Properties;

import org.embl.ebi.escience.scufl.*;

/**
 * A Processor for publishing notifications to the myGrid notification
 * service.
 *
 * @author Justin Ferris
 * @author Ananth Krishna
 */
public class NotificationProcessor extends Processor {
    private String prediction;

    public NotificationProcessor(ScuflModel model, String name) throws ProcessorCreationException,                                                                     DuplicateProcessorNameException {
        super(model, name);
        try {
            Port inputPort = new InputPort(this, "publishMessage");
            addPort(inputPort);
        }
        catch (Exception e) {
            throw new ProcessorCreationException("Couldn't create NotificationProcessor");
        }
    }

    public Properties getProperties() {
        Properties props = new Properties();
        return props;
    }
}