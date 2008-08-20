package net.sf.taverna.t2.partition.ui;

import java.awt.Color;
import java.awt.Component;

/**
 * A column used in the tree part of the table tree node renderer
 * 
 * @author Tom Oinn
 * 
 */
public interface TableTreeNodeColumn {

	/**
	 * Get a string to use as the header text
	 * 
	 * @return
	 */
	public String getShortName();

	/**
	 * Get a descriptive string for tooltips etc.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Given a node value render the appropriate property for this column
	 * 
	 * @param value
	 * @return
	 */
	public Component getCellRenderer(Object value);

	/**
	 * Return the width in pixels for this column
	 * 
	 * @return
	 */
	public int getColumnWidth();

	/**
	 * Get a header colour - the actual column colour will be a stripe of the
	 * result of applying the lighter operator twice and once to this colour.
	 */
	public Color getColour();
	
}
