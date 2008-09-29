/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.spi;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * Implementing classes are capable of storing a collection
 * of DataThing objects held in a result map. The simplest
 * current implementation of this will create a directory
 * structure on disk to hold the results and populate it, others
 * could generate Excel or populate some kind of SRB system,
 * not that we're using the SRB at all, heaven forbid. Obviously
 * I meant the MIR. Yes. That would be it.
 * @author Tom Oinn
 */
public interface ResultMapSaveSPI {

    /**
     * Get an icon to be used for display purposes in the
     * result browser interface. If this returns null then
     * a default icon will be used
     */
    public Icon getIcon();

    /**
     * Get a description of the storage method, can be arbitrarily
     * long. Please feel free to put as much detail as possible
     * in here.
     */
    public String getDescription();
    
    /**
     * Get a short display name for the storage method, should
     * be a sensible length for appearance in a context menu
     * so 'Save to Excel' and similar rather than anything
     * more florid.
     */
    public String getName();

    /**
     * Get an action listener which will be bound to the appropriate
     * UI component used to trigger the save operation. We should
     * possibly be using the Swing Action support for this but this
     * should work okay for now. The Map passed into this method
     * contains the String -> DataThing name to value pairs returned
     * by the current set of results. The actual listener may well wish
     * to display some kind of dialog, for example in the case of an Excel
     * export plugin it would be reasonable to give the user some choice
     * over where the results would be inserted into the sheet, and
     * also where the generated file would be stored.<p>
     * The parent parameter is optional and may be set to null, if not
     * it is assumed to be the parent component in the UI which caused
     * this action to be created, this allows save dialogs etc to be
     * placed correctly.
     */
    public ActionListener getListener(Map resultMap, JComponent parent);
    
}
