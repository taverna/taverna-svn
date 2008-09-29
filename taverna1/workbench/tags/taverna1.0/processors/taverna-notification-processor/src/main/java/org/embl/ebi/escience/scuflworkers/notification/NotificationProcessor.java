/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */

package org.embl.ebi.escience.scuflworkers.notification;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * A Processor for publishing notifications to the myGrid notification service.
 * 
 * @author Justin Ferris
 * @author Ananth Krishna
 */
public class NotificationProcessor extends Processor {

	public NotificationProcessor(ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, name);
		try {
			Port inputPort1 = new InputPort(this, "publishMessage");
			Port inputPort2 = new InputPort(this, "Topic");
			addPort(inputPort1);
			addPort(inputPort2);
		} catch (Exception e) {
			throw new ProcessorCreationException("Couldn't create NotificationProcessor");
		}
	}

	public Properties getProperties() {
		Properties props = new Properties();
		return props;
	}
}
