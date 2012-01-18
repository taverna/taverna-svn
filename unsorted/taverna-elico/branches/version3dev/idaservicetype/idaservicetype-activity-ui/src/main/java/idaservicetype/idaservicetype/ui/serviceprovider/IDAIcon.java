package idaservicetype.idaservicetype.ui.serviceprovider;

import idaservicetype.idaservicetype.IDAActivity;
import idaservicetype.idaservicetype.IDAActivityConfigurationBean;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class IDAIcon implements ActivityIconSPI {
	private Icon icon;
	@Override
	public int canProvideIconScore(Activity<?> activity) {
	    if (activity instanceof IDAActivity) {
	        IDAActivity exampleActivity = (IDAActivity) activity;
	        IDAActivityConfigurationBean configuration = exampleActivity.getConfiguration();
	       
	            return DEFAULT_ICON + 100;
	       
	    }
	    return NO_ICON;

	}

	@Override
	public Icon getIcon(Activity<?> activity) {
	    if (icon == null) {
	        icon = new ImageIcon(ExampleServiceIcon.class.getResource("/IDAicon.png"));
	    }
	    return icon;

	}

}
