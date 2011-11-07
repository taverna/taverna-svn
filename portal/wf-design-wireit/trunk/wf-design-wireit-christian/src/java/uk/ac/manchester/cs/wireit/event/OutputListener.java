/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.event;

import uk.ac.manchester.cs.wireit.module.WireItRunException;

/**
 *
 * @author Christian
 */
public interface OutputListener {
    
    public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException;
}
