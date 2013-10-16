package net.sf.taverna.t2.component.ui.serviceprovider;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.component.ComponentActivity;

public class ComponentServiceIcon implements ActivityIconSPI {

	private static Icon icon;

	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof ComponentActivity)
			return DEFAULT_ICON + 1;
		return NO_ICON;
	}

	public Icon getIcon(Activity<?> activity) {
		return getIcon();
	}

	public static Icon getIcon() {
		if (icon == null)
			icon = new ImageIcon(
					ComponentServiceIcon.class.getResource("/brick.png"));
		return icon;
	}

}
