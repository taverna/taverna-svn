package ${packageName};

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManagerException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * An Activity providing ${artifactId} functionality.
 * 
 */
public class ${artifactId}Activity extends
		AbstractAsynchronousActivity<${artifactId}ActivityConfigurationBean> {

	private ${artifactId}ActivityConfigurationBean configurationBean;

	public ${artifactId}Activity() {
	}

	@Override
	public void configure(${artifactId}ActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configurePorts(configurationBean);
	}

	@Override
	public ${artifactId}ActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}
	

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				DataFacade dataFacade = new DataFacade(callback.getContext().getDataManager());

				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

				try {
					//resolve inputs
					Object exampleInput = dataFacade.resolve(data.get("example_input"), String.class);
					
					//run the activity
					String exampleOutput = exampleInput + "_example";
					
					//register outputs
					outputData.put("example_output", dataFacade.register(exampleOutput));

					//send result to the callback
					callback.receiveResult(outputData, new int[0]);
				} catch (DataManagerException e) {
					callback.fail("Error accessing input/output data", e);
				} catch (NotFoundException e) {
					callback.fail("Error accessing input/output data", e);
				}
			}
			
		});

	}
}
