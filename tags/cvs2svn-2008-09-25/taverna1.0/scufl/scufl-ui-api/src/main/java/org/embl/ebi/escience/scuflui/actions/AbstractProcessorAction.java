/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.ProcessorActionSPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

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
		    UIComponentSPI frame = new SimpleFrame(c, theProcessor);
		    UIUtils.createFrame((ScuflModel)null, frame, (int)loc.getWidth(), (int)loc.getHeight(), (int)size.getWidth(), (int)size.getHeight());
		}
	    };
    }
    
    private class SimpleFrame extends JPanel implements UIComponentSPI {
	Processor processor = null;
	public SimpleFrame(Component c, Processor p) {
	    super(new BorderLayout());
	    add(c, BorderLayout.CENTER);
	    this.processor = p;
	}
	public void onDisplay() {
	    //
	}
	public void onDispose() {
	    AbstractProcessorAction.this.frameClosing();
	}
	public ImageIcon getIcon() {
	    return AbstractProcessorAction.this.getIcon();
	}
	public String getName() {
		try
		{
			return AbstractProcessorAction.this.getDescription();
		}
		catch(NullPointerException ex)
		{
			return "";
		}
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
