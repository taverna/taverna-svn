package net.sf.taverna.t2.activities.biomoby.translator;

import net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;

import org.embl.ebi.escience.scufl.Processor;

public class BiomobyObjectActivityTranslator extends AbstractActivityTranslator<BiomobyObjectActivityConfigurationBean> {

	@Override
	protected BiomobyObjectActivityConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		return new BiomobyObjectActivityConfigurationBean();
	}

	@Override
	protected BiomobyObjectActivity createUnconfiguredActivity() {
		return new BiomobyObjectActivity();
	}

	public boolean canHandle(Processor processor) {
		return processor!=null && processor.getClass().getName().equals("org.biomoby.client.taverna.plugin.BiomobyObjectProcessor");
	}

}
