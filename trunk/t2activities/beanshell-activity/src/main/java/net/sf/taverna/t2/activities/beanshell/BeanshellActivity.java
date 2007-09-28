package net.sf.taverna.t2.activities.beanshell;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;
import bsh.Interpreter;

/**
 * <p>
 * An Activity providing to Beanshell functionality.
 * </p>
 * 
 * @author David Withers
 * @author Stuart Owen
 */
public class BeanshellActivity extends
		AbstractAsynchronousActivity<BeanshellActivityConfigurationBean> {

	private BeanshellActivityConfigurationBean configurationBean;

	@Override
	protected ActivityPortBuilder getPortBuilder() {
		return ActivityPortBuilderImpl.getInstance();
	} 

	@Override
	public void configure(BeanshellActivityConfigurationBean configurationBeans)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBeans;
		configurePorts(configurationBeans);
	}

	@Override
	public BeanshellActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				DataFacade dataManager = new DataFacade(callback
						.getLocalDataManager());
				Interpreter interpreter = new Interpreter();
				// interpreter.setClassLoader(classLoader);

				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

				try {
					// set inputs
					for (String inputName : data.keySet()) {
						Object input = dataManager.resolve(data.get(inputName));
						interpreter.set(inputName, input);
					}
					// run
					interpreter.eval(configurationBean.getScript());
					// get and clear outputs
					for (ActivityOutputPortDefinitionBean outputBean : configurationBean.getOutputPortDefinitions()) {
						Object value = interpreter.get(outputBean.getName());
						if (value != null) {
							dataManager.register(value);
						}
						// interpreter.unset(output);
					}
					// clear inputs
					// inputs = workflowInputMap.keySet().iterator();
					// while (inputs.hasNext()) {
					// String inputname = (String) inputs.next();
					// interpreter.unset(inputname);
					// }
					callback.receiveResult(outputData, new int[0]);
				} catch (Exception ex) {
					callback.fail("", ex);
				}
			}
		});

	}
}
