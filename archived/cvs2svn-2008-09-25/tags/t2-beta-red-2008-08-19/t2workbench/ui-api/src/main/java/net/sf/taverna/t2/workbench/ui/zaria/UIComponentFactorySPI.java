package net.sf.taverna.t2.workbench.ui.zaria;

import javax.swing.ImageIcon;

/**
 * Implementations can construct UI components to be inserted into containers
 * within the workbench. These components may or may not bind to any given model
 * object, the previous approach of forcing all UI components to be model
 * listeners wasn't really very bright.
 * <p>
 * This class is intended to allow minimal information for building menus and
 * the like without having to construct potentially heavy swing objects every
 * time.
 * 
 * @author Tom Oinn
 */
public interface UIComponentFactorySPI {

	/**
	 * Get the preferred name of this component, for titles in windows etc.
	 */
	public String getName();

	/**
	 * Get an icon to be used in window decorations for this component.
	 */
	public ImageIcon getIcon();

	/**
	 * Construct a JComponent from this factory, cast as a UIComponent but must
	 * also implement JComponent (if anyone knows how to define this sensibly
	 * I'm all ears...)
	 */
	public UIComponentSPI getComponent();

}
