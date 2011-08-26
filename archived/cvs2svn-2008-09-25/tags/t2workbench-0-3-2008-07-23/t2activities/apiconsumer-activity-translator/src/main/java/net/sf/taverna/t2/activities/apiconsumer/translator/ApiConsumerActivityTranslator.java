package net.sf.taverna.t2.activities.apiconsumer.translator;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivityConfigBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;

public class ApiConsumerActivityTranslator extends AbstractActivityTranslator<ApiConsumerActivityConfigBean>{

	@Override
	protected ApiConsumerActivityConfigBean createConfigType(Processor proc)
			throws ActivityTranslationException {
		return null;
	}

	@Override
	protected Activity<ApiConsumerActivityConfigBean> createUnconfiguredActivity() {
		return new ApiConsumerActivity();
	}

	public boolean canHandle(Processor processor) {
		//forced to false as the activity is not yet implemented.
		return false;
		//return (processor!=null && processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.apiconsumer.ApiConsumerProcessor"));
	}

}
