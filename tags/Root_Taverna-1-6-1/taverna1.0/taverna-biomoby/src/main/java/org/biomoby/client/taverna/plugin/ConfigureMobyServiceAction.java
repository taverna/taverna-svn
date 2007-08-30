/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;

/**
 * 
 * @author Eddie An action that for BioMobyProcessors
 */
public class ConfigureMobyServiceAction extends AbstractProcessorAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction#getComponent(org.embl.ebi.escience.scufl.Processor)
	 */

	public JComponent getComponent(Processor processor) {
		// variables i need
		BiomobyProcessor bp = (BiomobyProcessor) processor;

		@SuppressWarnings("unused") String serviceName = bp.getMobyService().getName();
		@SuppressWarnings("unused") String description = bp.getDescription();
		return bp.getParameterTable().scrollable();
	}

	public void frameClosing() {
		super.frameClosing();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#canHandle(org.embl.ebi.escience.scufl.Processor)
	 */
	public boolean canHandle(Processor processor) {
		if (processor instanceof BiomobyProcessor && (((BiomobyProcessor) processor).getMobyService() != null)) {
			if (((BiomobyProcessor) processor).containsSecondaries())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getDescription()
	 */
	public String getDescription() {
		return "Configure Moby Service";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getIcon()
	 */
	public ImageIcon getIcon() {
		Class cls = this.getClass();
		URL url = cls.getClassLoader().getResource(
				"org/biomoby/client/taverna/plugin/moby_small.gif");
		return new ImageIcon(url);
	}

	/**
	 * returns the frame size as a dimension for the content pane housing this
	 * action
	 */
	public Dimension getFrameSize() {
		return new Dimension(450, 450);
	}

	/**
	 * Return an Icon to represent this action
	 * 
	 * @param loc
	 *            the location of the image to use as an icon
	 */
	public ImageIcon getIcon(String loc) {
		Class cls = this.getClass();
		URL url = cls.getClassLoader().getResource(loc);
		return new ImageIcon(url);
	}
}