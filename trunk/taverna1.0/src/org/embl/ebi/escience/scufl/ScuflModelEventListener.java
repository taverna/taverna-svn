/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Implementors can respond to ScuflModelEvent messages,
 * and may register themselves with a ScuflModel.
 * @author Tom Oinn
 */
public interface ScuflModelEventListener {

    public void receiveModelEvent(ScuflModelEvent event);

}
