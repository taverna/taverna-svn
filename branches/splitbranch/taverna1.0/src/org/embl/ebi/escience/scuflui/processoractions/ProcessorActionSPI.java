/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.processoractions;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scufl.Processor;

/**
 * SPI for actions capable of operating on a Processor object in the
 * workflow. These can then be gathered and presented in the appropriate
 * context menus within the workbench to allow arbitrary operations
 * such as processor configuration, metadata handling etc.
 * @author Tom Oinn
 */
public interface ProcessorActionSPI {

    /**
     * Return a short description of the action to be used
     * as the text in a context menu
     */
    public String getDescription();

    /**
     * Can this action handle the specified processor? Return true
     * if this action is applicable.
     */
    public boolean canHandle(Processor processor);

    /**
     * Return an ActionListener to be attached to whatever component
     * is being generated. In the case of a context menu this will
     * be a JMenuItem but it's best not to assume anything, it could
     * be any kind of component. This method is passed the Processor
     * object concerned, remember that the SPI mechanism creates a 
     * single instance of each implementing class so you should be
     * careful about keeping state in the implementation (by 'be careful'
     * I really mean 'don't').
     */
    public ActionListener getListener(Processor processor);
    
    /**
     * Return an Icon to represent this action, return null if you
     * want to use the default icon (boring)
     */
    public ImageIcon getIcon();

}
