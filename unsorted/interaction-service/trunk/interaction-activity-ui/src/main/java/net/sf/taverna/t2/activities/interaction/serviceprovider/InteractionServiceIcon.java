package net.sf.taverna.t2.activities.interaction.serviceprovider;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class InteractionServiceIcon implements ActivityIconSPI {

	private static final Color PROCESSOR_COLOUR = Color.decode("#6AEB9F");

	static {
		// set colour for Interaction services in the workflow diagram
		ColourManager.getInstance().setPreferredColour(
				InteractionActivity.class.getCanonicalName(), PROCESSOR_COLOUR);
	}

	private static Icon icon;

	@Override
	public int canProvideIconScore(final Activity<?> activity) {
		if (activity instanceof InteractionActivity) {
			return DEFAULT_ICON + 1;
		}
		return NO_ICON;
	}

	@Override
	public Icon getIcon(final Activity<?> activity) {
		return getIcon();
	}

	public static Icon getIcon() {
		if (icon == null) {
			icon = new ImageIcon(
					InteractionServiceIcon.class
							.getResource("/interactionIcon.png"));
		}
		return icon;
	}

}
