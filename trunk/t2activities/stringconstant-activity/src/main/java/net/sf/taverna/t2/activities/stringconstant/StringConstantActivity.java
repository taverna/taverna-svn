package net.sf.taverna.t2.activities.stringconstant;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
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
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				DataFacade dataFacade=new DataFacade(callback.getContext().getDataManager());
				try {
					EntityIdentifier id=dataFacade.register(value);
					Map<String,EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();
					outputData.put("value", id);
					callback.receiveResult(outputData, new int[0]);
				} catch (EmptyListException e) {
					callback.fail(e.getMessage(),e);
				} catch (MalformedListException e) {
					callback.fail(e.getMessage(),e);
				} catch (UnsupportedObjectTypeException e) {
					callback.fail(e.getMessage(),e);
				} 
			}
			
		});
		
	}

}
