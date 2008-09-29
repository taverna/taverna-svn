/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.spi;

import javax.swing.ImageIcon;

/**
 * Interface for any UI component to be used within the workbench
 * @author Tom Oinn
 */
public interface UIComponentSPI {

	/**
	 * Get the preferred name of this component, for titles in windows etc.
	 */
	public String getName();

	/**
	 * Get an icon to be used in window decorations for this component.
	 */
	public ImageIcon getIcon();

	/**
	 * Called when the component is displayed in the UI
	 */
	public void onDisplay();
	
	/**
	 * Called after the component has been removed from the UI
	 */
	public void onDispose();
	
}
