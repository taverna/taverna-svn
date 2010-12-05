package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.serviceprovider;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class RapidMinerIcon implements ActivityIconSPI {

	private static Icon icon;
	
	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof RapidMinerExampleActivity) {
			
	            return DEFAULT_ICON + 100;
	        
	    }
	    return NO_ICON;

	}

	public Icon getIcon(Activity<?> arg0) {
		if (icon == null) {
	        icon = new ImageIcon(RapidMinerIcon.class.getResource("/rapidMiner.png"));
	    }
	    return icon;
	    
	}
	public static Icon getIcon() {
		if (icon == null) {
	        icon = new ImageIcon(RapidMinerIcon.class.getResource("/rapidMiner.png"));
	    }
	    return icon;
	    
	}


}
