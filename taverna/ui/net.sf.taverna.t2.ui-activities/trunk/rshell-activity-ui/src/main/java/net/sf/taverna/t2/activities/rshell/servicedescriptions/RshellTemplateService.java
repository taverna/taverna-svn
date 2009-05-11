package net.sf.taverna.t2.activities.rshell.servicedescriptions;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.rshell.query.RshellActivityItem;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

public class RshellTemplateService extends AbstractTemplateService<RshellActivityConfigurationBean>{

	private static final String RSHELL = "Rshell";
	
	@Override
	public Class<RshellActivity> getActivityClass() {
		return RshellActivity.class;
	}

	@Override
	public RshellActivityConfigurationBean getActivityConfiguration() {
		return new RshellActivityConfigurationBean();
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(RshellActivityItem.class.getResource("/rshell.png"));
	}

	public String getName() {
		return RSHELL;
	}
	
	@Override
	public String getDescription() {
		return "A service that allows the calling of R scripts on an R server";	
	}
	
	public static ServiceDescription getServiceDescription() {
		RshellTemplateService rts = new RshellTemplateService();
		return rts.templateService;
	}
}
