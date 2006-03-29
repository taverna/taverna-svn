/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.processoractions;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * A simple abstract implementation of the ProcessorActionSPI
 * which allows the extending class to simply specify a JComponent
 * to be created in a standard UIUtils frame.
 * @author Tom Oinn
 */
public abstract class AbstractProcessorAction implements ProcessorActionSPI {
    
    public final ActionListener getListener(Processor processor) {
	final Processor theProcessor = processor;
	// Create a simple JPanel with the specified component in
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Dimension loc = AbstractProcessorAction.this.getFrameLocation();
		    Dimension size = AbstractProcessorAction.this.getFrameSize();
		    Component c = getComponent(theProcessor);
		    ScuflUIComponent frame = new SimpleFrame(c, theProcessor);
		    UIUtils.createFrame((ScuflModel)null, frame, (int)loc.getWidth(), (int)loc.getHeight(), (int)size.getWidth(), (int)size.getHeight());
		}
	    };
    }
    
    private class SimpleFrame extends JPanel implements ScuflUIComponent {
	Processor processor = null;
	public SimpleFrame(Component c, Processor p) {
	    super(new BorderLayout());
	    add(c, BorderLayout.CENTER);
	    this.processor = p;
	    //setPreferredSize(c.getPreferredSize());
	    //setMinimumSize(c.getMinimumSize());
	    //setMaximumSize(c.getMaximumSize());
	    //setPreferredSize(new Dimension(0,0));
	}
	public void attachToModel(ScuflModel model) {
	    //
	}
	public void detachFromModel() {
	    AbstractProcessorAction.this.frameClosing();
	}
	public ImageIcon getIcon() {
	    return AbstractProcessorAction.this.getIcon();
	}
	public String getName() {
	    return AbstractProcessorAction.this.getDescription();
	}
    }

    /**
     * Implement this to define the component which will be created
     * in the new UIUtils frame when the menu item, button or whatever
     * is activated
     */
    public abstract JComponent getComponent(Processor processor);
    
    /**
     * Where should the frame open?
     */
    public Dimension getFrameLocation() {
	return new Dimension(100,100);
    }

    /**
     * How big should it be?
     */
    public Dimension getFrameSize() {
	return new Dimension(250,250);
    }

    /**
     * Add any actions to be performed on window close to here
     */
    public void frameClosing() {
	//
    }	

}
