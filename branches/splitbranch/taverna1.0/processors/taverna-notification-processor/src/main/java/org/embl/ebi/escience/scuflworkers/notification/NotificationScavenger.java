/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */

package org.embl.ebi.escience.scuflworkers.notification;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * Scavenger for Processor for publishing notifications to the myGrid notification
 * service.
 *
 * @author Justin Ferris
 * @author Ananth Krishna
 */
public class NotificationScavenger extends Scavenger {
    public NotificationScavenger() throws ScavengerCreationException {
        super(new NotificationProcessorFactory());
    }
}