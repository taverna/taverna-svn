package net.sf.taverna.t2.workbench.ui.zaria;

import java.io.InputStream;
import javax.swing.ImageIcon;

import org.jdom.Element;

/**
 * SPI representing UI perspectives
 * @author Stuart Owen
 *
 */

public interface PerspectiveSPI {
	
	/**
	 * 
	 * @return the input stream to the layout XML
	 */
	public InputStream getLayoutInputStream();
	
	/**
	 * 
	 * @return the icon image for the toolbar button
	 */
	public ImageIcon getButtonIcon();
	
	/**
	 * 
	 * @return the text for the perspective
	 */
	public String getText();
	
	/**
	 * Store internally any changes to the layout xml
	 */
	public void update(Element layoutElement);
	
	/**
	 * Provides a hint for the position of perspective in the toolbar and menu.
	 * The lower the value the earlier it will appear in the list.
	 * 
	 * Custom plugins are recommended to start with a value > 100 (allowing for a whopping 100 built in plugins!)
	 */
	public int positionHint();
	
	/**
	 * returns true if the perspective is set to be visible
	 * @return boolean
	 */
	public boolean isVisible();
	
	/**
	 * sets whether the perspective should be visible or not.
	 *
	 */
	public void setVisible(boolean visible);
		
	
		
	
}
