package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Manages the colour of elements in a graph.
 * 
 * @author David Withers
 */
public class GraphColorManager {

	private static Map<String, Color> activityColours = new HashMap<String, Color>();
	
	private static Color[] subGraphFillColors = new Color[] {Color.decode("#ffffff"),
			Color.decode("#f0f8ff"), Color.decode("#faebd7"), Color.decode("#f5f5dc")};
	
	static {
		activityColours.put("net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity", Color.decode("#98fb98"));//palegreen
		activityColours.put("net.sf.taverna.t2.activities.beanshell.BeanshellActivity", Color.decode("#deb887"));//burlywood2
		activityColours.put("net.sf.taverna.t2.activities.biomart.BiomartActivity", Color.decode("#d1eeee"));//lightcyan2
		activityColours.put("net.sf.taverna.t2.activities.biomoby.BiomobyActivity", Color.decode("#ffb90f"));//darkgoldenrod1
		activityColours.put("net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivity", Color.decode("#ffd700"));//gold
		activityColours.put("net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivity", Color.decode("#ffffff"));//white
		activityColours.put("net.sf.taverna.t2.activities.dataflow.DataflowActivity", Color.decode("#ffc0cb"));//pink
		activityColours.put("net.sf.taverna.t2.activities.rshell.RshellActivity", Color.decode("#fafad2"));//lightgoldenrodyellow
		activityColours.put("net.sf.taverna.t2.activities.soaplab.SoaplabActivity", Color.decode("#fafad2"));//lightgoldenrodyellow
		activityColours.put("net.sf.taverna.t2.activities.stringconstant.StringConstantActivity", Color.decode("#b0c4de"));//lightsteelblue
		activityColours.put("net.sf.taverna.t2.activities.wsdl.WSDLActivity", Color.decode("#a2cd5a"));//darkolivegreen3
	}
	
	/**
	 * Returns the colour associated with the Activity.
	 * 
	 * For unknown activities Color.WHITE is returned.
	 * 
	 * @return the colour associated with the Activity
	 */
	public static Color getFillColor(Activity<?> activity) {
		//TODO colours should be discovered
		Color color = activityColours.get(activity.getClass().getName());
		return color == null ? Color.WHITE : color;
	}
	
	public static Color getSubGraphFillColor(int depth) {
		return subGraphFillColors[depth % subGraphFillColors.length];
	}
	

}
