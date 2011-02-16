package uk.ac.manchester.cs.img.esc.ui.serviceprovider;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import uk.ac.manchester.cs.img.esc.EscActivity;

public class EscServiceIcon implements ActivityIconSPI {

	private static final Color COLOUR = Color.decode("#fb6600");

	static {
		ColourManager.getInstance().setPreferredColour(
				"uk.ac.manchester.cs.img.esc.EscActivity", COLOUR);
	}
	private static Icon icon;

	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof EscActivity) {
			return DEFAULT_ICON;
		}
		return NO_ICON;
	}

	public Icon getIcon(Activity<?> activity) {
		return getIcon();
	}
	
	public static Icon getIcon() {
		if (icon == null) {
			icon = new ImageIcon(EscServiceIcon.class.getResource("/esc.png"));
		}
		return icon;
	}

}
