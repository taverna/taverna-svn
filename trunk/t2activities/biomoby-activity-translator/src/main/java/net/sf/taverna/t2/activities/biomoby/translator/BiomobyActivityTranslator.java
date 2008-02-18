package net.sf.taverna.t2.activities.biomoby.translator;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;

import org.embl.ebi.escience.scufl.Processor;

public class BiomobyActivityTranslator extends AbstractActivityTranslator<BiomobyActivityConfigurationBean> {

	@Override
	protected BiomobyActivityConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		return new BiomobyActivityConfigurationBean();
	}

	@Override
	protected BiomobyActivity createUnconfiguredActivity() {
		return new BiomobyActivity();
	}

	public boolean canHandle(Processor processor) {
		return processor != null && processor.getClass().getName().equals("org.biomoby.client.taverna.plugin.BiomobyProcessor");
	}

}
