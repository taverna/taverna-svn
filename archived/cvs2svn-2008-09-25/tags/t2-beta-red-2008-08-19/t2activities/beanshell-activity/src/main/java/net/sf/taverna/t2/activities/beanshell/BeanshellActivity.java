package net.sf.taverna.t2.activities.beanshell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
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
	public void configure(BeanshellActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configurePorts(configurationBean);
		if (configurationBean.getDependencies().size() > 0) {
			List<Artifact> dependencies = new ArrayList<Artifact>();
			for (String dependency : configurationBean.getDependencies()) {
				String[] parts = dependency.split(":");
				if (parts.length == 3) {
					dependencies.add(new BasicArtifact(parts[0], parts[1],
							parts[2]));
				}
			}
			interpreter.setClassLoader(new BeanshellClassloader(dependencies));
		}
	}

	@Override
	public BeanshellActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}
	
	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				try {
					synchronized (interpreter) {
						// set inputs
						for (String inputName : data.keySet()) {
							ActivityInputPort inputPort = getInputPort(inputName);
							Object input = referenceService.renderIdentifier(data.get(inputName),
									inputPort.getTranslatedElementClass(), callback.getContext());
							inputName = sanatisePortName(inputName);
							interpreter.set(inputName, input);
						}
						// run
						interpreter.eval(configurationBean.getScript());
						// get and clear outputs
						for (OutputPort outputPort : getOutputPorts()) {
							String name = outputPort.getName();
							Object value = interpreter.get(name);
							if (value != null) {
								outputData.put(name, referenceService.register(value,
										outputPort.getDepth(), true, callback.getContext()));
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
				} catch (ReferenceServiceException e) {
					callback.fail(
							"Error accessing beanshell input/output data", e);
				}
			}
			
			/**
			 * Removes any invalid characters from the port name.
			 * For example, xml-text would become xmltext.
			 * 
			 * 
			 * @param name
			 * @return
			 */
			private String sanatisePortName(String name) {
				String result=name;
				if (Pattern.matches("\\w++", name) == false) {
					result="";
					for (char c : name.toCharArray()) {
						if (Character.isLetterOrDigit(c) || c=='_') {
							result+=c;
						}
					}
				}
				return result;
			}
		});

	}
}
