package net.sf.taverna.t2.cyclone.translators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;

import bsh.Interpreter;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;

//FIXME: this doesn't belong in this package. It should be moved to a separate module
/**
 * <p>
 * A semi-dummy Activity relating to Beanshell functionality. Eventually this
 * class will not exist as part of t2core but will be part of a Beanshell
 * activity artifact in its own right.
 * </p>
 * 
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
					for (String output : configurationBean.getOutputPortNames()) {
						Object value = interpreter.get(output);
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
