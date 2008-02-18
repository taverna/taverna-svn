package net.sf.taverna.t2.activities.biomoby.translator;

import net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivity;
import net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;

import org.embl.ebi.escience.scufl.Processor;

public class MobyParseDatatypeActivityTranslator extends
		AbstractActivityTranslator<MobyParseDatatypeActivityConfigurationBean> {

	@Override
	protected MobyParseDatatypeActivityConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		return new MobyParseDatatypeActivityConfigurationBean();
	}

	@Override
	protected MobyParseDatatypeActivity createUnconfiguredActivity() {
		return new MobyParseDatatypeActivity();
	}

	public boolean canHandle(Processor processor) {
		return processor != null && processor.getClass().getName().equals("org.biomoby.client.taverna.plugin.MobyParseDatatypeProcessor");
	}

}
