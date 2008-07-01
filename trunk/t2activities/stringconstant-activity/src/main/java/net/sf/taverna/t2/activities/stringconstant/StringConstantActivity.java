package net.sf.taverna.t2.activities.stringconstant;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * <p>
 * An Activity that holds a constant string value. It is automatically configured to have no input ports
 * and only one output port named <em>value</em>.<br>
 *
 * @author Stuart Owen
 *
 */
public class StringConstantActivity extends AbstractAsynchronousActivity<StringConstantConfigurationBean>{
	private String value;
	private StringConstantConfigurationBean config=null;
	@Override
	public void configure(StringConstantConfigurationBean conf)
			throws ActivityConfigurationException {
		this.config=conf;
		this.value=conf.getValue();
		addOutput("value", 0);
	}

	public String getStringValue() {
		return value;
	}
	
	@Override
	public StringConstantConfigurationBean getConfiguration() {
		return config;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();
				try {
					T2Reference id = referenceService.register(value, 0, true, callback.getContext());
					Map<String,T2Reference> outputData = new HashMap<String, T2Reference>();
					outputData.put("value", id);
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					callback.fail(e.getMessage(),e);
				}
			}
			
		});
		
	}

}
