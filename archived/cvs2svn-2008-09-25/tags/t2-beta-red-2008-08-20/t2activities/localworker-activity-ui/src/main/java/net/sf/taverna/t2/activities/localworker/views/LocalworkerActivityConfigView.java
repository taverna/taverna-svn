package net.sf.taverna.t2.activities.localworker.views;

import javax.help.CSH;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellConfigView;

public class LocalworkerActivityConfigView extends BeanshellConfigView{

	public LocalworkerActivityConfigView(BeanshellActivity activity) {
		super(activity);
		initLocalworker();
	}

	private void initLocalworker() {
		CSH
		.setHelpIDString(
				this,
		"net.sf.taverna.t2.activities.localworker.views.LocalworkerActivityConfigView");
	}

}
