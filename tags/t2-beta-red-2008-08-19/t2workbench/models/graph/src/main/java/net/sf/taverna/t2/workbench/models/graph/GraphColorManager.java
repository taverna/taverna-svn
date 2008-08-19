package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Color;

import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.HostInstitution;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Manages the colour of elements in a graph.
 * 
 * @author David Withers
 * @author Start Owen
 */
public class GraphColorManager {

	private static Color[] subGraphFillColors = new Color[] {
			Color.decode("#f7f7f7"), Color.decode("#f0f8ff"),
			Color.decode("#faebd7"), Color.decode("#f5f5dc") };

	/**
	 * Returns the colour associated with the Activity.
	 * 
	 * For unknown activities Color.WHITE is returned.
	 * 
	 * For {@link LocalworkerActivity} which have been user configured use the
	 * BeanshellActivity colour
	 * 
	 * @return the colour associated with the Activity
	 */
	public static Color getFillColor(Activity<?> activity) {
		if (activity instanceof LocalworkerActivity) {
			if (checkAnnotations((LocalworkerActivity) activity)) {
				Color colour = ColourManager
						.getInstance()
						.getPreferredColour(
								"net.sf.taverna.t2.activities.beanshell.BeanshellActivity");
				return colour;

			}
		}
		Color colour = ColourManager.getInstance().getPreferredColour(
				activity.getClass().getName());
		return colour;
	}

	private static boolean checkAnnotations(LocalworkerActivity activity) {
		for (AnnotationChain chain : activity.getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				Object detail = assertion.getDetail();
				System.out.println(detail.getClass().getName());
				if (detail instanceof HostInstitution) {
					// this is a user defined localworker so use the beanshell
					// colour!
					return true;
				}
			}
		}
		return false;
	}

	public static Color getSubGraphFillColor(int depth) {
		return subGraphFillColors[depth % subGraphFillColors.length];
	}

}
