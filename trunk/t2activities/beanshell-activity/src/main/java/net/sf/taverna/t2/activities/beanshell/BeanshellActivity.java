package net.sf.taverna.t2.activities.beanshell;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManagerException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * <p>
 * An Activity providing Beanshell functionality. 
 * </p>
 * 
 * @author David Withers
 * @author Stuart Owen
 */
public class BeanshellActivity extends
		AbstractAsynchronousActivity<BeanshellActivityConfigurationBean> {

	private BeanshellActivityConfigurationBean configurationBean;

	private Interpreter interpreter;

	public BeanshellActivity() {
		// TODO do we need an interpreter per activity or per execution? 
		interpreter = new Interpreter();
		// TODO decide how to get the beanshell's class loader
		// interpreter.setClassLoader(classLoader);
	}

	@Override
	protected ActivityPortBuilder getPortBuilder() {
		//FIXME: remove this dependency on the maelstrom-impl. This is currently the only link between the 2. 
		//There are easy ways to do this, but non of them particularly elegant. 
		//Passing the builder to configure involves passing it around all over the place.
		return ActivityPortBuilderImpl.getInstance();
	} 

	@Override
	public void configure(BeanshellActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configurePorts(configurationBean);
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
				DataFacade dataFacade = new DataFacade(callback
						.getLocalDataManager());

				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

				try {
					synchronized (interpreter) {
						// set inputs
						for (String inputName : data.keySet()) {
							Object input = dataFacade.resolve(data
									.get(inputName));
							interpreter.set(inputName, input);
						}
						// run
						interpreter.eval(configurationBean.getScript());
						// get and clear outputs
						for (ActivityOutputPortDefinitionBean outputBean : configurationBean
								.getOutputPortDefinitions()) {
							String name = outputBean.getName();
							Object value = interpreter
									.get(name);
							if (value != null) {
								outputData.put(name, dataFacade.register(value));
							}
							interpreter.unset(name);
						}
						// clear inputs
						for (String inputName : data.keySet()) {
							interpreter.unset(inputName);
						}
					}
					callback.receiveResult(outputData, new int[0]);
				} catch (EvalError e) {
					callback.fail("Error evaluating the beanshell script", e);
				} catch (DataManagerException e) {
					callback.fail("Error accessing beanshell input/output data", e);
				} catch (NotFoundException e) {
					callback.fail("Error accessing beanshell input/output data", e);
				} catch (IOException e) {
					callback.fail("Error accessing beanshell input/output data", e);
				}
			}
		});

	}
}
