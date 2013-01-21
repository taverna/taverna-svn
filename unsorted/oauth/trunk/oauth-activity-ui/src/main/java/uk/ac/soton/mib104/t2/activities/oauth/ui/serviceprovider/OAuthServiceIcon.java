package uk.ac.soton.mib104.t2.activities.oauth.ui.serviceprovider;

import java.awt.Color;

import javax.swing.Icon;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import uk.ac.soton.mib104.t2.activities.oauth.OAuth10aRESTActivity;
import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivity;
import uk.ac.soton.mib104.t2.activities.oauth.util.IconManager;

public class OAuthServiceIcon implements ActivityIconSPI {

	static {
		final ColourManager colourManager = ColourManager.getInstance();
		
		if (colourManager != null) {
			colourManager.setPreferredColour(OAuthActivity.class.getCanonicalName(), Color.decode("#CCCCCC"));
			colourManager.setPreferredColour(OAuth10aRESTActivity.class.getCanonicalName(), Color.decode("#7AAFFF"));
		}
	}

	public int canProvideIconScore(final Activity<?> activity) {
		if ((activity != null) && ((activity instanceof OAuthActivity) || (activity instanceof OAuth10aRESTActivity))) {
			return DEFAULT_ICON;
		} else {
			return NO_ICON;	
		}
	}

	public Icon getIcon(final Activity<?> activity) {
		if (activity instanceof OAuth10aRESTActivity) {
			return IconManager.getSmallRESTIcon();
		} else {
			return IconManager.getSmallIcon();
		}
	}

}
