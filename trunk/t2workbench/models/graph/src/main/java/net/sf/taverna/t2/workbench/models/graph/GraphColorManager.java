package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Color;

import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Manages the colour of elements in a graph.
 * 
 * @author David Withers
 * @author Start Owen
 */
public class GraphColorManager {
	
	private static Color[] subGraphFillColors = new Color[] {Color.decode("#ffffff"),
			Color.decode("#f0f8ff"), Color.decode("#faebd7"), Color.decode("#f5f5dc")};
	
	/**
	 * Returns the colour associated with the Activity.
	 * 
	 * For unknown activities Color.WHITE is returned.
	 * 
	 * @return the colour associated with the Activity
	 */
	public static Color getFillColor(Activity<?> activity) {
		Color colour = ColourManager.getInstance().getPreferredColour(activity.getClass().getName());
		return colour;
	}
	
	public static Color getSubGraphFillColor(int depth) {
		return subGraphFillColors[depth % subGraphFillColors.length];
	}
	

}
