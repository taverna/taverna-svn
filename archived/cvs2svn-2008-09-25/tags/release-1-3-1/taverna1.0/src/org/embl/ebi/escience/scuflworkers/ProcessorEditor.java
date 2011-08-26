/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;
import java.awt.event.ActionListener;
import org.embl.ebi.escience.scufl.Processor;

import java.lang.String;



/**
 * Classes implementing this perform in place editing of 
 * a particular processor instance. For example, the string
 * constant processor has an editor that allows the user to
 * change the value of the string it emits.
 * @author Tom Oinn
 */
public interface ProcessorEditor {

    /** 
     * Return an action listener to edit the processor when
     * invoked from the context menu.
     */
    public ActionListener getListener(Processor theProcessor);

    /**
     * Get the menu text for this editor component, this will
     * appear in the context menu for the processor in the
     * scufl model explorer view.
     */
    public String getEditorDescription();

}
